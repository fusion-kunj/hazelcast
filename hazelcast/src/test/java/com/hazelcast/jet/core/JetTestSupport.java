/*
 * Copyright (c) 2008-2025, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.jet.core;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.impl.connection.tcp.RoutingMode;
import com.hazelcast.client.test.TestHazelcastFactory;
import com.hazelcast.cluster.Address;
import com.hazelcast.cluster.Member;
import com.hazelcast.collection.IList;
import com.hazelcast.config.Config;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.dataconnection.impl.InternalDataConnectionService;
import com.hazelcast.instance.impl.HazelcastInstanceImpl;
import com.hazelcast.internal.cluster.MemberInfo;
import com.hazelcast.internal.cluster.impl.MembersView;
import com.hazelcast.internal.serialization.InternalSerializationService;
import com.hazelcast.internal.serialization.impl.DefaultSerializationServiceBuilder;
import com.hazelcast.jet.JetService;
import com.hazelcast.jet.Job;
import com.hazelcast.jet.function.RunnableEx;
import com.hazelcast.jet.impl.JetServiceBackend;
import com.hazelcast.jet.impl.JobClassLoaderService;
import com.hazelcast.jet.impl.JobExecutionRecord;
import com.hazelcast.jet.impl.JobRepository;
import com.hazelcast.jet.impl.execution.init.ExecutionPlanBuilder;
import com.hazelcast.jet.impl.pipeline.transform.BatchSourceTransform;
import com.hazelcast.jet.impl.util.Util;
import com.hazelcast.jet.pipeline.BatchSource;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.map.IMap;
import com.hazelcast.spi.impl.NodeEngineImpl;
import com.hazelcast.test.Accessors;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.OverridePropertyRule;
import com.hazelcast.version.Version;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.rules.Timeout;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.hazelcast.jet.Util.idToString;
import static com.hazelcast.jet.core.JobStatus.RUNNING;
import static com.hazelcast.jet.impl.JetServiceBackend.SERVICE_NAME;
import static com.hazelcast.jet.impl.util.ExceptionUtil.rethrow;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public abstract class JetTestSupport extends HazelcastTestSupport {

    public static final InternalSerializationService TEST_SS = new DefaultSerializationServiceBuilder().build();

    /**
     * This is needed to finish tests which got stuck in @Before, @BeforeClass, @After or @AfterClass method.
     * Note that this rule applies to entire test execution (all methods with all parameters),
     * not only to individual methods contrary to what might be expected from {@link Timeout} javadoc.
     * This is caused by ordering of standard rules with respect to our custom rules.
     */
    @ClassRule
    public static Timeout globalTimeout = Timeout.seconds(15 * 60);

    @ClassRule
    public static OverridePropertyRule enableJetRule = OverridePropertyRule.set("hz.jet.enabled", "true");

    private static final ILogger SUPPORT_LOGGER = Logger.getLogger(JetTestSupport.class);

    protected ILogger logger = Logger.getLogger(getClass());
    private TestHazelcastFactory instanceFactory;

    @After
    public void shutdownFactory() throws Exception {
        if (instanceFactory != null) {
            Map<Long, String> leakedClassloaders = shutdownJobsAndGetLeakedClassLoaders();

            SUPPORT_LOGGER.info("Terminating instanceFactory in JetTestSupport.@After");
            spawn(() -> instanceFactory.terminateAll())
                    .get(1, TimeUnit.MINUTES);

            if (!leakedClassloaders.isEmpty()) {
                String ids = leakedClassloaders
                        .entrySet().stream()
                        .map(entry -> idToString(entry.getKey()) + "[" + entry.getValue() + "]")
                        .collect(joining(", "));
                fail("There are one or more leaked job classloaders. " +
                        "This is a bug, but it is not necessarily related to this test. " +
                        "The classloader was leaked for the following jobIds: " + ids);
            }

            instanceFactory = null;
        }
    }

    @Nonnull
    private Map<Long, String> shutdownJobsAndGetLeakedClassLoaders() {
        Map<Long, String> leakedClassloaders = new HashMap<>();
        Collection<HazelcastInstance> instances = instanceFactory.getAllHazelcastInstances();
        for (HazelcastInstance instance : instances) {
            if (instance.getConfig().getJetConfig().isEnabled()) {
                // Some tests leave jobs running, which keeps job classloader, shut down all running/starting jobs
                JetService jet = instance.getJet();
                List<Job> jobs = jet.getJobs();
                for (Job job : jobs) {
                    ditchJob(job, instances.toArray(new HazelcastInstance[0]));
                }

                if (instance instanceof HazelcastInstanceImpl hazelcastInstanceImpl) {
                    JobClassLoaderService jobClassLoaderService = hazelcastInstanceImpl.node
                            .getNodeEngine()
                            .<JetServiceBackend>getService(SERVICE_NAME)
                            .getJobClassLoaderService();

                    Map<Long, ?> classLoaders = jobClassLoaderService.getClassLoaders();
                    // The classloader cleanup is done asynchronously in some cases, wait up to 10s
                    for (int i = 0; i < 100 && !classLoaders.isEmpty(); i++) {
                        sleepMillis(100);
                    }
                    for (Entry<Long, ?> entry : classLoaders.entrySet()) {
                        leakedClassloaders.put(entry.getKey(), entry.toString());
                    }
                }
            }
        }
        return leakedClassloaders;
    }

    protected HazelcastInstance createHazelcastClient() {
        return instanceFactory.newHazelcastClient();
    }

    protected HazelcastInstance createHazelcastClient(ClientConfig config) {
        return instanceFactory.newHazelcastClient(config);
    }

    /**
     * Returns config to configure a SINGLE_MEMBER routing client that connects to the
     * given instance only.
     */
    protected ClientConfig configForSingleMemberClientConnectingTo(HazelcastInstance targetInstance) {
        ClientConfig clientConfig = new ClientConfig();
        Member coordinator = targetInstance.getCluster().getLocalMember();
        clientConfig.getNetworkConfig()
                .addAddress(coordinator.getAddress().getHost() + ':' + coordinator.getAddress().getPort())
                .getClusterRoutingConfig().setRoutingMode(RoutingMode.SINGLE_MEMBER);
        return clientConfig;
    }

    protected HazelcastInstance createHazelcastInstance() {
        return createHazelcastInstance(smallInstanceConfig());
    }

    protected HazelcastInstance createHazelcastInstance(Config config) {
        if (instanceFactory == null) {
            instanceFactory = new TestHazelcastFactory();
        }
        return instanceFactory.newHazelcastInstance(config);
    }

    protected HazelcastInstance[] createHazelcastInstances(int nodeCount) {
        return this.createHazelcastInstances(smallInstanceConfig(), nodeCount);
    }

    protected HazelcastInstance[] createHazelcastInstances(Config config, int nodeCount) {
        if (instanceFactory == null) {
            instanceFactory = new TestHazelcastFactory();
        }
        return instanceFactory.newInstances(config, nodeCount);
    }

    protected static <K, V> IMap<K, V> getMap(HazelcastInstance instance) {
        return instance.getMap(randomName());
    }

    protected static <E> IList<E> getList(HazelcastInstance instance) {
        return instance.getList(randomName());
    }

    protected static void appendToFile(File file, String... lines) throws IOException {
        try (var writer = new PrintWriter(new FileOutputStream(file, true))) {
            for (String payload : lines) {
                writer.write(payload + '\n');
            }
        }
    }

    protected static File createTempDirectory() throws IOException {
        Path directory = Files.createTempDirectory("jet-test-temp");
        File file = directory.toFile();
        file.deleteOnExit();
        return file;
    }

    public static Config smallInstanceWithResourceUploadConfig() {
        Config config = smallInstanceConfig();
        config.getJetConfig().setResourceUploadEnabled(true);
        return config;
    }

    public static Config defaultInstanceConfigWithJetEnabled() {
        Config config = new Config();
        config.getJetConfig().setEnabled(true);
        return config;
    }

    public static JetServiceBackend getJetServiceBackend(HazelcastInstance instance) {
        return Accessors.getNodeEngineImpl(instance).getService(SERVICE_NAME);
    }

    public static InternalDataConnectionService getDataConnectionService(HazelcastInstance instance) {
        return Accessors.getNodeEngineImpl(instance).getDataConnectionService();
    }

    public static Map<Address, int[]> getPartitionAssignment(HazelcastInstance instance) {
        NodeEngineImpl nodeEngine = Accessors.getNodeEngineImpl(instance);
        MembersView membersView = Util.getMembersView(nodeEngine);
        Version coordinatorVersion = nodeEngine.getLocalMember().getVersion().asVersion();
        List<MemberInfo> members = membersView.getMembers().stream()
                .filter(m -> m.getVersion().asVersion().equals(coordinatorVersion) && !m.isLiteMember())
                .collect(Collectors.toList());
        return ExecutionPlanBuilder.getPartitionAssignment(nodeEngine, members, false, null, null, null)
                .entrySet()
                .stream()
                .collect(toMap(en -> en.getKey().getAddress(), Map.Entry::getValue));
    }

    public Address getAddressForPartitionId(HazelcastInstance instance, int partitionId) {
        Map<Address, int[]> partitionAssignment = getPartitionAssignment(instance);
        for (Entry<Address, int[]> entry : partitionAssignment.entrySet()) {
            for (int pId : entry.getValue()) {
                if (pId == partitionId) {
                    return entry.getKey();
                }
            }
        }
        throw new AssertionError("Partition " + partitionId + " is not present in cluster.");
    }

    /**
     * Runs the given Runnable in a new thread, if you're not interested in the
     * execution failure, any errors are logged at WARN level.
     */
    public Future<?> spawnSafe(RunnableEx r) {
        return spawn(() -> {
            try {
                r.runEx();
            } catch (Throwable e) {
                SUPPORT_LOGGER.warning("Spawned Runnable failed", e);
            }
        });
    }

    public static Watermark wm(long timestamp) {
        return new Watermark(timestamp);
    }

    public static Watermark wm(long timestamp, byte key) {
        return new Watermark(timestamp, key);
    }

    public void waitForFirstSnapshot(JobRepository jr, long jobId, int timeoutSeconds, boolean allowEmptySnapshot) {
        long[] snapshotId = {-1};
        assertTrueEventually(() -> {
            JobExecutionRecord record = jr.getJobExecutionRecord(jobId);
            assertNotNull("null JobExecutionRecord", record);
            assertTrue("No snapshot produced",
                    record.dataMapIndex() >= 0 && record.snapshotId() >= 0);
            assertTrue("stats are 0",
                    allowEmptySnapshot || requireNonNull(record.snapshotStats()).numBytes() > 0);
            snapshotId[0] = record.snapshotId();
        }, timeoutSeconds);
        SUPPORT_LOGGER.info("First snapshot found (id=" + snapshotId[0] + ")");
    }

    public static void waitForNextSnapshot(HazelcastInstance instance, Job job) {
        JobRepository jobRepository = new JobRepository(instance);
        waitForNextSnapshot(jobRepository, job);
    }

    public static void waitForNextSnapshot(JobRepository repo, Job job) {
        waitForNextSnapshot(repo, job.getId(), 30, false);
    }

    public static void waitForNextSnapshot(JobRepository jr, long jobId, int timeoutSeconds, boolean allowEmptySnapshot) {
        long originalSnapshotId = jr.getJobExecutionRecord(jobId).snapshotId();
        // wait until there is at least one more snapshot
        long[] snapshotId = {-1};
        long start = System.nanoTime();
        assertTrueEventually(() -> {
            JobExecutionRecord record = jr.getJobExecutionRecord(jobId);
            assertNotNull("jobExecutionRecord is null", record);
            snapshotId[0] = record.snapshotId();
            assertTrue("No more snapshots produced in " + timeoutSeconds + " seconds",
                    snapshotId[0] > originalSnapshotId);
            assertTrue("stats are 0",
                    allowEmptySnapshot || requireNonNull(record.snapshotStats()).numBytes() > 0);
        }, timeoutSeconds);
        SUPPORT_LOGGER.info("Next snapshot found after " + NANOSECONDS.toMillis(System.nanoTime() - start) + " ms (id="
                + snapshotId[0] + ", previous id=" + originalSnapshotId + ")");
    }

    /**
     * Clean up the cluster and make it ready to run a next test. If we fail
     * to, shut it down so that next tests don't run on a messed-up cluster.
     *
     * @param instances cluster instances, must contain at least
     *                  one instance
     */
    public void cleanUpCluster(HazelcastInstance... instances) {
        for (Job job : instances[0].getJet().getJobs()) {
            ditchJob(job, instances);
        }
        for (DistributedObject o : instances[0].getDistributedObjects()) {
            o.destroy();
        }
    }

    /**
     * Give this method a job, and it will ensure it's no longer running. It
     * will ignore if it's not running. If the cancellation fails, it will
     * retry.
     */
    public static void ditchJob(@Nonnull Job job, @Nonnull HazelcastInstance... instances) {
        //Cancel the job on cluster members
        ditchJob0(job, instances);

        // Let's wait for the job to be not RUNNING on all the members.
        assertTrueEventually(() -> {

            try {
                assertNotEquals(RUNNING, job.getStatus());
            } catch (Exception e) {
                SUPPORT_LOGGER.severe("Failure to read job status on coordinator: ", e);
            }

            for (HazelcastInstance instance : instances) {
                try {
                    Job instanceJob = instance.getJet().getJob(job.getId());
                    if (instanceJob != null) {
                        assertNotEquals(RUNNING, instanceJob.getStatus());
                    }
                } catch (Exception e) {
                    SUPPORT_LOGGER.severe("Failure to read job status on member: ", e);
                }
            }
        });
    }

    private static void ditchJob0(@Nonnull Job job, @Nonnull HazelcastInstance... instancesToShutDown) {
        int numAttempts;
        for (numAttempts = 0; numAttempts < 10; numAttempts++) {
            JobStatus status = null;
            try {
                status = job.getStatus();
                if (status == JobStatus.FAILED || status == JobStatus.COMPLETED) {
                    return;
                }
            } catch (JobNotFoundException e) {
                SUPPORT_LOGGER.fine("Job " + job.getIdString() + " is gone.");
                return;
            } catch (Exception e) {
                SUPPORT_LOGGER.warning("Failure to read job status: " + e, e);
            }

            Exception cancellationFailure;
            try {
                job.cancel();
                try {
                    job.join();
                } catch (JobNotFoundException e) {
                    SUPPORT_LOGGER.fine("Job " + job.getIdString() + " is gone.");
                    return;
                } catch (CompletionException e) {
                    if (e.getCause() instanceof JobNotFoundException) {
                        SUPPORT_LOGGER.fine("Job " + job.getIdString() + " is gone.");
                        return;
                    }
                    throw rethrow(e.getCause());
                } catch (Exception ignored) {
                    // This can be CancellationException or any other job failure. We don't care,
                    // we're supposed to rid the cluster of the job and that's what we have.
                }
                return;
            } catch (JobNotFoundException e) {
                SUPPORT_LOGGER.fine("Job " + job.getIdString() + " is gone.");
                return;
            } catch (Exception e) {
                cancellationFailure = e;
            }

            sleepMillis(500);
            SUPPORT_LOGGER.warning("Failed to cancel the job and it is " + status + ", retrying. Failure: "
                    + cancellationFailure, cancellationFailure);
        }
        // if we got here, 10 attempts to cancel the job have failed. Cluster is in bad shape probably, shut it down
        try {
            for (HazelcastInstance instance : instancesToShutDown) {
                instance.getLifecycleService().terminate();
            }
        } catch (Exception e) {
            // ignore, proceed to throwing RuntimeException
        }
        throw new RuntimeException(numAttempts + " attempts to cancel the job failed" +
                (instancesToShutDown.length > 0 ? ", shut down the cluster" : ""));
    }

    /**
     * Cancel the job and wait until it cancels using {@link Job#join()},
     * ignoring the {@link CancellationException}.
     */
    public static void cancelAndJoin(@Nonnull Job job) {
        job.cancel();
        assertThatThrownBy(job::join).as("join didn't fail with CancellationException")
                .isInstanceOf(CancellationException.class);
    }

    public static <T> ProcessorMetaSupplier processorFromPipelineSource(BatchSource<T> source) {
        return ((BatchSourceTransform<T>) source).metaSupplier;
    }

    public static Job awaitSingleRunningJob(HazelcastInstance hz) {
        AtomicReference<Job> job = new AtomicReference<>();
        assertTrueEventually(() -> {
            List<Job> jobs = hz.getJet().getJobs().stream().filter(j -> j.getStatus() == RUNNING).toList();
            assertEquals(1, jobs.size());
            job.set(jobs.get(0));
        });
        return job.get();
    }
}
