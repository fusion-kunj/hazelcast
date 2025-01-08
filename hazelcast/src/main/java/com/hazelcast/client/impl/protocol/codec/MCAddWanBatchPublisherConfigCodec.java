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
 * Add a new WAN batch publisher configuration
 */
@SuppressWarnings("unused")
@Generated("3883ec4430298aca336117f473b9ce97")
public final class MCAddWanBatchPublisherConfigCodec {
    //hex: 0x201500
    public static final int REQUEST_MESSAGE_TYPE = 2102528;
    //hex: 0x201501
    public static final int RESPONSE_MESSAGE_TYPE = 2102529;
    private static final int REQUEST_QUEUE_CAPACITY_FIELD_OFFSET = PARTITION_ID_FIELD_OFFSET + INT_SIZE_IN_BYTES;
    private static final int REQUEST_BATCH_SIZE_FIELD_OFFSET = REQUEST_QUEUE_CAPACITY_FIELD_OFFSET + INT_SIZE_IN_BYTES;
    private static final int REQUEST_BATCH_MAX_DELAY_MILLIS_FIELD_OFFSET = REQUEST_BATCH_SIZE_FIELD_OFFSET + INT_SIZE_IN_BYTES;
    private static final int REQUEST_RESPONSE_TIMEOUT_MILLIS_FIELD_OFFSET = REQUEST_BATCH_MAX_DELAY_MILLIS_FIELD_OFFSET + INT_SIZE_IN_BYTES;
    private static final int REQUEST_ACK_TYPE_FIELD_OFFSET = REQUEST_RESPONSE_TIMEOUT_MILLIS_FIELD_OFFSET + INT_SIZE_IN_BYTES;
    private static final int REQUEST_QUEUE_FULL_BEHAVIOR_FIELD_OFFSET = REQUEST_ACK_TYPE_FIELD_OFFSET + INT_SIZE_IN_BYTES;
    private static final int REQUEST_CONSISTENCY_CHECK_STRATEGY_FIELD_OFFSET = REQUEST_QUEUE_FULL_BEHAVIOR_FIELD_OFFSET + INT_SIZE_IN_BYTES;
    private static final int REQUEST_INITIAL_FRAME_SIZE = REQUEST_CONSISTENCY_CHECK_STRATEGY_FIELD_OFFSET + BYTE_SIZE_IN_BYTES;
    private static final int RESPONSE_INITIAL_FRAME_SIZE = RESPONSE_BACKUP_ACKS_FIELD_OFFSET + BYTE_SIZE_IN_BYTES;

    private MCAddWanBatchPublisherConfigCodec() {
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"})
    public static class RequestParameters {

        /**
         * Name of the WAN replication to add
         */
        public java.lang.String name;

        /**
         * Name of the target cluster
         */
        public java.lang.String targetCluster;

        /**
         * - ID used for identifying the publisher in a WanReplicationConfig
         */
        public @Nullable java.lang.String publisherId;

        /**
         * Comma separated list of target cluster members
         */
        public java.lang.String endpoints;

        /**
         * Capacity of the primary and backup queue for WAN replication events
         */
        public int queueCapacity;

        /**
         * The maximum batch size that can be sent to target cluster
         */
        public int batchSize;

        /**
         * The maximum amount of time in milliseconds to wait before sending a batch
         *  of events to target cluster, if `batch size` of events have not arrived
         *  within this duration
         */
        public int batchMaxDelayMillis;

        /**
         * The duration in milliseconds for the wait time before retrying to
         * send the events to target cluster again in case the acknowledgement
         * has not arrived
         */
        public int responseTimeoutMillis;

        /**
         * The strategy for when the target cluster should acknowledge that
         * a WAN event batch has been processed:
         * 0 - ACK_ON_RECEIPT
         * 1 - ACK_ON_OPERATION_COMPLETE
         */
        public int ackType;

        /**
         * Behaviour of this WAN publisher when the WAN queue is full:
         * 0 - DISCARD_AFTER_MUTATION
         * 1 - THROW_EXCEPTION
         * 2 - THROW_EXCEPTION_ONLY_IF_REPLICATION_ACTIVE
         */
        public int queueFullBehavior;

        /**
         * Strategy for checking the consistency of data between wanReplicationResourceName:
         * 0 - NONE
         * 1 - MERKLE_TREES
         */
        public byte consistencyCheckStrategy;

        /**
         * True if the consistencyCheckStrategy is received from the client, false otherwise.
         * If this is false, consistencyCheckStrategy has the default value for its type.
         */
        public boolean isConsistencyCheckStrategyExists;
    }

    public static ClientMessage encodeRequest(java.lang.String name, java.lang.String targetCluster, @Nullable java.lang.String publisherId, java.lang.String endpoints, int queueCapacity, int batchSize, int batchMaxDelayMillis, int responseTimeoutMillis, int ackType, int queueFullBehavior, byte consistencyCheckStrategy) {
        ClientMessage clientMessage = ClientMessage.createForEncode();
        clientMessage.setRetryable(false);
        clientMessage.setOperationName("MC.AddWanBatchPublisherConfig");
        ClientMessage.Frame initialFrame = new ClientMessage.Frame(new byte[REQUEST_INITIAL_FRAME_SIZE], UNFRAGMENTED_MESSAGE);
        encodeInt(initialFrame.content, TYPE_FIELD_OFFSET, REQUEST_MESSAGE_TYPE);
        encodeInt(initialFrame.content, PARTITION_ID_FIELD_OFFSET, -1);
        encodeInt(initialFrame.content, REQUEST_QUEUE_CAPACITY_FIELD_OFFSET, queueCapacity);
        encodeInt(initialFrame.content, REQUEST_BATCH_SIZE_FIELD_OFFSET, batchSize);
        encodeInt(initialFrame.content, REQUEST_BATCH_MAX_DELAY_MILLIS_FIELD_OFFSET, batchMaxDelayMillis);
        encodeInt(initialFrame.content, REQUEST_RESPONSE_TIMEOUT_MILLIS_FIELD_OFFSET, responseTimeoutMillis);
        encodeInt(initialFrame.content, REQUEST_ACK_TYPE_FIELD_OFFSET, ackType);
        encodeInt(initialFrame.content, REQUEST_QUEUE_FULL_BEHAVIOR_FIELD_OFFSET, queueFullBehavior);
        encodeByte(initialFrame.content, REQUEST_CONSISTENCY_CHECK_STRATEGY_FIELD_OFFSET, consistencyCheckStrategy);
        clientMessage.add(initialFrame);
        StringCodec.encode(clientMessage, name);
        StringCodec.encode(clientMessage, targetCluster);
        CodecUtil.encodeNullable(clientMessage, publisherId, StringCodec::encode);
        StringCodec.encode(clientMessage, endpoints);
        return clientMessage;
    }

    public static MCAddWanBatchPublisherConfigCodec.RequestParameters decodeRequest(ClientMessage clientMessage) {
        ClientMessage.ForwardFrameIterator iterator = clientMessage.frameIterator();
        RequestParameters request = new RequestParameters();
        ClientMessage.Frame initialFrame = iterator.next();
        request.queueCapacity = decodeInt(initialFrame.content, REQUEST_QUEUE_CAPACITY_FIELD_OFFSET);
        request.batchSize = decodeInt(initialFrame.content, REQUEST_BATCH_SIZE_FIELD_OFFSET);
        request.batchMaxDelayMillis = decodeInt(initialFrame.content, REQUEST_BATCH_MAX_DELAY_MILLIS_FIELD_OFFSET);
        request.responseTimeoutMillis = decodeInt(initialFrame.content, REQUEST_RESPONSE_TIMEOUT_MILLIS_FIELD_OFFSET);
        request.ackType = decodeInt(initialFrame.content, REQUEST_ACK_TYPE_FIELD_OFFSET);
        request.queueFullBehavior = decodeInt(initialFrame.content, REQUEST_QUEUE_FULL_BEHAVIOR_FIELD_OFFSET);
        if (initialFrame.content.length >= REQUEST_CONSISTENCY_CHECK_STRATEGY_FIELD_OFFSET + BYTE_SIZE_IN_BYTES) {
            request.consistencyCheckStrategy = decodeByte(initialFrame.content, REQUEST_CONSISTENCY_CHECK_STRATEGY_FIELD_OFFSET);
            request.isConsistencyCheckStrategyExists = true;
        } else {
            request.isConsistencyCheckStrategyExists = false;
        }
        request.name = StringCodec.decode(iterator);
        request.targetCluster = StringCodec.decode(iterator);
        request.publisherId = CodecUtil.decodeNullable(iterator, StringCodec::decode);
        request.endpoints = StringCodec.decode(iterator);
        return request;
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings({"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"})
    public static class ResponseParameters {

        /**
         * Returns the IDs for the WAN publishers which were added to the configuration
         */
        public java.util.List<java.lang.String> addedPublisherIds;

        /**
         * Returns the IDs for the WAN publishers which were ignored and not added to
         * the configuration.
         */
        public java.util.List<java.lang.String> ignoredPublisherIds;
    }

    public static ClientMessage encodeResponse(java.util.Collection<java.lang.String> addedPublisherIds, java.util.Collection<java.lang.String> ignoredPublisherIds) {
        ClientMessage clientMessage = ClientMessage.createForEncode();
        ClientMessage.Frame initialFrame = new ClientMessage.Frame(new byte[RESPONSE_INITIAL_FRAME_SIZE], UNFRAGMENTED_MESSAGE);
        encodeInt(initialFrame.content, TYPE_FIELD_OFFSET, RESPONSE_MESSAGE_TYPE);
        clientMessage.add(initialFrame);

        ListMultiFrameCodec.encode(clientMessage, addedPublisherIds, StringCodec::encode);
        ListMultiFrameCodec.encode(clientMessage, ignoredPublisherIds, StringCodec::encode);
        return clientMessage;
    }

    public static MCAddWanBatchPublisherConfigCodec.ResponseParameters decodeResponse(ClientMessage clientMessage) {
        ClientMessage.ForwardFrameIterator iterator = clientMessage.frameIterator();
        ResponseParameters response = new ResponseParameters();
        //empty initial frame
        iterator.next();
        response.addedPublisherIds = ListMultiFrameCodec.decode(iterator, StringCodec::decode);
        response.ignoredPublisherIds = ListMultiFrameCodec.decode(iterator, StringCodec::decode);
        return response;
    }
}
