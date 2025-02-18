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

package com.hazelcast.internal.util.phonehome;

import com.hazelcast.client.impl.ClientEndpointStatisticsManagerImpl;
import com.hazelcast.client.impl.ClientEngineImpl;
import com.hazelcast.client.impl.clientside.ClientTestUtil;
import com.hazelcast.client.impl.protocol.ClientMessage;
import com.hazelcast.client.impl.protocol.codec.ClientAuthenticationCodec;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.EndpointQualifier;
import com.hazelcast.instance.impl.Node;
import com.hazelcast.test.Accessors;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.hazelcast.internal.nio.Protocols.CLIENT_BINARY;

public class TestUtil {

    public static final String CONNECTIONS_OPENED_SUFFIX = "co";
    public static final String CONNECTIONS_CLOSED_SUFFIX = "cc";
    public static final String TOTAL_CONNECTION_DURATION_SUFFIX = "tcd";
    public static final String CLIENT_VERSIONS_SUFFIX = "cv";
    public static final String CLIENT_VERSIONS_SEPARATOR = ",";

    public static class DummyClientFactory {
        private final Set<DummyClient> clients = Collections.newSetFromMap(new ConcurrentHashMap<>());

        public DummyClient newClient(String clientType, String clientVersion) {
            DummyClient client = new DummyClient(clientType, clientVersion);
            clients.add(client);
            return client;
        }

        public void terminateAll() {
            for (DummyClient client : clients) {
                client.shutdown();
            }
            clients.clear();
        }
    }

    public static class DummyClient {
        private final String clientType;
        private final String clientVersion;
        private final UUID uuid;
        private final Set<DummyConnection> connections = Collections.newSetFromMap(new ConcurrentHashMap<>());

        public DummyClient(String clientType, String clientVersion) {
            this.clientType = clientType;
            this.clientVersion = clientVersion;
            this.uuid = UUID.randomUUID();
        }

        public DummyConnection connectTo(Node node) throws IOException {
            String clusterName = node.getConfig().getClusterName();
            ClientMessage request = ClientAuthenticationCodec.encodeRequest(
                    clusterName, null, null, uuid,
                    clientType, (byte) 1, clientVersion, uuid.toString(), Collections.emptyList(), (byte) 1, false);
            InetSocketAddress address = node.getLocalMember().getSocketAddress(EndpointQualifier.CLIENT);
            DummyConnection connection = new DummyConnection(address.getAddress(), address.getPort());
            connections.add(connection);
            connection.authenticate(request);
            return connection;
        }

        public void shutdown() {
            for (DummyConnection connection : connections) {
                try {
                    connection.close();
                } catch (IOException ignored) {
                }
            }
            connections.clear();
        }
    }

    public static class DummyConnection {
        private final Socket socket;

        public DummyConnection(InetAddress address, int port) throws IOException {
            socket = new Socket(address, port);
        }

        public void close() throws IOException {
            socket.close();
        }

        private void authenticate(ClientMessage authenticationRequest) throws IOException {
            OutputStream os = socket.getOutputStream();
            InputStream is = socket.getInputStream();
            os.write(CLIENT_BINARY.getBytes(StandardCharsets.UTF_8));
            ClientTestUtil.writeClientMessage(os, authenticationRequest);
            ClientTestUtil.readResponse(is);
        }
    }

    public enum ClientPrefix {
        CPP("ccpp"),
        CSHARP("cdn"),
        JAVA("cjv"),
        NODEJS("cnjs"),
        PYTHON("cpy"),
        GO("cgo"),
        CLC("ccl");

        private final String prefix;

        ClientPrefix(String prefix) {
            this.prefix = prefix;
        }

        String getPrefix() {
            return prefix;
        }
    }

    public static Map<String, String> getParameters(Node node) {
        return new PhoneHome(node).phoneHome(true);
    }

    public static Node getNode(HazelcastInstance instance) {
        Node node = Accessors.getNode(instance);
        ((ClientEngineImpl) node.getClientEngine()).setEndpointStatisticsManager(new ClientEndpointStatisticsManagerImpl());
        return node;
    }

}
