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

package com.hazelcast.client.impl.protocol.codec;

import com.hazelcast.client.impl.protocol.ClientMessage;
import com.hazelcast.client.impl.protocol.Generated;
import com.hazelcast.client.impl.protocol.codec.builtin.*;
import com.hazelcast.client.impl.protocol.codec.custom.*;

import javax.annotation.Nullable;

import static com.hazelcast.client.impl.protocol.ClientMessage.*;
import static com.hazelcast.client.impl.protocol.codec.builtin.FixedSizeTypesCodec.*;

/*
 * This file is auto-generated by the Hazelcast Client Protocol Code Generator.
 * To change this file, edit the templates or the protocol
 * definitions on the https://github.com/hazelcast/hazelcast-client-protocol
 * and regenerate it.
 */

/**
 * Returns statistics of the task
 */
@SuppressWarnings("unused")
@Generated("a02b73163560bc35984d5823d3e36b7c")
public final class ScheduledExecutorGetStatsFromMemberCodec {
    //hex: 0x1A0600
    public static final int REQUEST_MESSAGE_TYPE = 1705472;
    //hex: 0x1A0601
    public static final int RESPONSE_MESSAGE_TYPE = 1705473;
    private static final int REQUEST_MEMBER_UUID_FIELD_OFFSET = PARTITION_ID_FIELD_OFFSET + INT_SIZE_IN_BYTES;
    private static final int REQUEST_INITIAL_FRAME_SIZE = REQUEST_MEMBER_UUID_FIELD_OFFSET + UUID_SIZE_IN_BYTES;
    private static final int RESPONSE_LAST_IDLE_TIME_NANOS_FIELD_OFFSET = RESPONSE_BACKUP_ACKS_FIELD_OFFSET + BYTE_SIZE_IN_BYTES;
    private static final int RESPONSE_TOTAL_IDLE_TIME_NANOS_FIELD_OFFSET = RESPONSE_LAST_IDLE_TIME_NANOS_FIELD_OFFSET + LONG_SIZE_IN_BYTES;
    private static final int RESPONSE_TOTAL_RUNS_FIELD_OFFSET = RESPONSE_TOTAL_IDLE_TIME_NANOS_FIELD_OFFSET + LONG_SIZE_IN_BYTES;
    private static final int RESPONSE_TOTAL_RUN_TIME_NANOS_FIELD_OFFSET = RESPONSE_TOTAL_RUNS_FIELD_OFFSET + LONG_SIZE_IN_BYTES;
    private static final int RESPONSE_LAST_RUN_DURATION_NANOS_FIELD_OFFSET = RESPONSE_TOTAL_RUN_TIME_NANOS_FIELD_OFFSET + LONG_SIZE_IN_BYTES;
    private static final int RESPONSE_INITIAL_FRAME_SIZE = RESPONSE_LAST_RUN_DURATION_NANOS_FIELD_OFFSET + LONG_SIZE_IN_BYTES;

    private ScheduledExecutorGetStatsFromMemberCodec() {
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"})
    public static class RequestParameters {

        /**
         * The name of the scheduler.
         */
        public java.lang.String schedulerName;

        /**
         * The name of the task
         */
        public java.lang.String taskName;

        /**
         * The UUID of the member where the task will get scheduled.
         */
        public java.util.UUID memberUuid;
    }

    public static ClientMessage encodeRequest(java.lang.String schedulerName, java.lang.String taskName, java.util.UUID memberUuid) {
        ClientMessage clientMessage = ClientMessage.createForEncode();
        clientMessage.setRetryable(true);
        clientMessage.setOperationName("ScheduledExecutor.GetStatsFromMember");
        ClientMessage.Frame initialFrame = new ClientMessage.Frame(new byte[REQUEST_INITIAL_FRAME_SIZE], UNFRAGMENTED_MESSAGE);
        encodeInt(initialFrame.content, TYPE_FIELD_OFFSET, REQUEST_MESSAGE_TYPE);
        encodeInt(initialFrame.content, PARTITION_ID_FIELD_OFFSET, -1);
        encodeUUID(initialFrame.content, REQUEST_MEMBER_UUID_FIELD_OFFSET, memberUuid);
        clientMessage.add(initialFrame);
        StringCodec.encode(clientMessage, schedulerName);
        StringCodec.encode(clientMessage, taskName);
        return clientMessage;
    }

    public static ScheduledExecutorGetStatsFromMemberCodec.RequestParameters decodeRequest(ClientMessage clientMessage) {
        ClientMessage.ForwardFrameIterator iterator = clientMessage.frameIterator();
        RequestParameters request = new RequestParameters();
        ClientMessage.Frame initialFrame = iterator.next();
        request.memberUuid = decodeUUID(initialFrame.content, REQUEST_MEMBER_UUID_FIELD_OFFSET);
        request.schedulerName = StringCodec.decode(iterator);
        request.taskName = StringCodec.decode(iterator);
        return request;
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"})
    public static class ResponseParameters {

        /**
         * Last period of time the task was idle, waiting to get scheduled.
         */
        public long lastIdleTimeNanos;

        /**
         * Total amount of time the task was idle, waiting to get scheduled in.
         */
        public long totalIdleTimeNanos;

        /**
         * How many times the task was ran/called.
         */
        public long totalRuns;

        /**
         * The total amount of time the task spent while scheduled in.
         */
        public long totalRunTimeNanos;

        /**
         * The duration of the task's last execution.
         */
        public long lastRunDurationNanos;
    }

    public static ClientMessage encodeResponse(long lastIdleTimeNanos, long totalIdleTimeNanos, long totalRuns, long totalRunTimeNanos, long lastRunDurationNanos) {
        ClientMessage clientMessage = ClientMessage.createForEncode();
        ClientMessage.Frame initialFrame = new ClientMessage.Frame(new byte[RESPONSE_INITIAL_FRAME_SIZE], UNFRAGMENTED_MESSAGE);
        encodeInt(initialFrame.content, TYPE_FIELD_OFFSET, RESPONSE_MESSAGE_TYPE);
        encodeLong(initialFrame.content, RESPONSE_LAST_IDLE_TIME_NANOS_FIELD_OFFSET, lastIdleTimeNanos);
        encodeLong(initialFrame.content, RESPONSE_TOTAL_IDLE_TIME_NANOS_FIELD_OFFSET, totalIdleTimeNanos);
        encodeLong(initialFrame.content, RESPONSE_TOTAL_RUNS_FIELD_OFFSET, totalRuns);
        encodeLong(initialFrame.content, RESPONSE_TOTAL_RUN_TIME_NANOS_FIELD_OFFSET, totalRunTimeNanos);
        encodeLong(initialFrame.content, RESPONSE_LAST_RUN_DURATION_NANOS_FIELD_OFFSET, lastRunDurationNanos);
        clientMessage.add(initialFrame);

        return clientMessage;
    }

    public static ScheduledExecutorGetStatsFromMemberCodec.ResponseParameters decodeResponse(ClientMessage clientMessage) {
        ClientMessage.ForwardFrameIterator iterator = clientMessage.frameIterator();
        ResponseParameters response = new ResponseParameters();
        ClientMessage.Frame initialFrame = iterator.next();
        response.lastIdleTimeNanos = decodeLong(initialFrame.content, RESPONSE_LAST_IDLE_TIME_NANOS_FIELD_OFFSET);
        response.totalIdleTimeNanos = decodeLong(initialFrame.content, RESPONSE_TOTAL_IDLE_TIME_NANOS_FIELD_OFFSET);
        response.totalRuns = decodeLong(initialFrame.content, RESPONSE_TOTAL_RUNS_FIELD_OFFSET);
        response.totalRunTimeNanos = decodeLong(initialFrame.content, RESPONSE_TOTAL_RUN_TIME_NANOS_FIELD_OFFSET);
        response.lastRunDurationNanos = decodeLong(initialFrame.content, RESPONSE_LAST_RUN_DURATION_NANOS_FIELD_OFFSET);
        return response;
    }
}
