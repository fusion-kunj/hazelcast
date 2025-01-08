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
import com.hazelcast.logging.Logger;

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
 * Adds a CP Group view listener to a connection.
 */
@SuppressWarnings("unused")
@Generated("c5c2971f33bbf3aee2d21564f9729404")
public final class ClientAddCPGroupViewListenerCodec {
    //hex: 0x001700
    public static final int REQUEST_MESSAGE_TYPE = 5888;
    //hex: 0x001701
    public static final int RESPONSE_MESSAGE_TYPE = 5889;
    private static final int REQUEST_INITIAL_FRAME_SIZE = PARTITION_ID_FIELD_OFFSET + INT_SIZE_IN_BYTES;
    private static final int RESPONSE_INITIAL_FRAME_SIZE = RESPONSE_BACKUP_ACKS_FIELD_OFFSET + BYTE_SIZE_IN_BYTES;
    private static final int EVENT_GROUPS_VIEW_VERSION_FIELD_OFFSET = PARTITION_ID_FIELD_OFFSET + INT_SIZE_IN_BYTES;
    private static final int EVENT_GROUPS_VIEW_INITIAL_FRAME_SIZE = EVENT_GROUPS_VIEW_VERSION_FIELD_OFFSET + LONG_SIZE_IN_BYTES;
    //hex: 0x001702
    private static final int EVENT_GROUPS_VIEW_MESSAGE_TYPE = 5890;

    private ClientAddCPGroupViewListenerCodec() {
    }

    public static ClientMessage encodeRequest() {
        ClientMessage clientMessage = ClientMessage.createForEncode();
        clientMessage.setRetryable(false);
        clientMessage.setOperationName("Client.AddCPGroupViewListener");
        ClientMessage.Frame initialFrame = new ClientMessage.Frame(new byte[REQUEST_INITIAL_FRAME_SIZE], UNFRAGMENTED_MESSAGE);
        encodeInt(initialFrame.content, TYPE_FIELD_OFFSET, REQUEST_MESSAGE_TYPE);
        encodeInt(initialFrame.content, PARTITION_ID_FIELD_OFFSET, -1);
        clientMessage.add(initialFrame);
        return clientMessage;
    }

    public static ClientMessage encodeResponse() {
        ClientMessage clientMessage = ClientMessage.createForEncode();
        ClientMessage.Frame initialFrame = new ClientMessage.Frame(new byte[RESPONSE_INITIAL_FRAME_SIZE], UNFRAGMENTED_MESSAGE);
        encodeInt(initialFrame.content, TYPE_FIELD_OFFSET, RESPONSE_MESSAGE_TYPE);
        clientMessage.add(initialFrame);

        return clientMessage;
    }

    public static ClientMessage encodeGroupsViewEvent(long version, java.util.Collection<com.hazelcast.cp.internal.RaftGroupInfo> groupsInfo, java.util.Collection<java.util.Map.Entry<java.util.UUID, java.util.UUID>> cpToApUuids) {
        ClientMessage clientMessage = ClientMessage.createForEncode();
        ClientMessage.Frame initialFrame = new ClientMessage.Frame(new byte[EVENT_GROUPS_VIEW_INITIAL_FRAME_SIZE], UNFRAGMENTED_MESSAGE);
        initialFrame.flags |= ClientMessage.IS_EVENT_FLAG;
        encodeInt(initialFrame.content, TYPE_FIELD_OFFSET, EVENT_GROUPS_VIEW_MESSAGE_TYPE);
        encodeInt(initialFrame.content, PARTITION_ID_FIELD_OFFSET, -1);
        encodeLong(initialFrame.content, EVENT_GROUPS_VIEW_VERSION_FIELD_OFFSET, version);
        clientMessage.add(initialFrame);

        ListMultiFrameCodec.encode(clientMessage, groupsInfo, RaftGroupInfoCodec::encode);
        EntryListUUIDUUIDCodec.encode(clientMessage, cpToApUuids);
        return clientMessage;
    }

    public abstract static class AbstractEventHandler {

        public void handle(ClientMessage clientMessage) {
            int messageType = clientMessage.getMessageType();
            ClientMessage.ForwardFrameIterator iterator = clientMessage.frameIterator();
            if (messageType == EVENT_GROUPS_VIEW_MESSAGE_TYPE) {
                ClientMessage.Frame initialFrame = iterator.next();
                long version = decodeLong(initialFrame.content, EVENT_GROUPS_VIEW_VERSION_FIELD_OFFSET);
                java.util.Collection<com.hazelcast.cp.internal.RaftGroupInfo> groupsInfo = ListMultiFrameCodec.decode(iterator, RaftGroupInfoCodec::decode);
                java.util.Collection<java.util.Map.Entry<java.util.UUID, java.util.UUID>> cpToApUuids = EntryListUUIDUUIDCodec.decode(iterator);
                handleGroupsViewEvent(version, groupsInfo, cpToApUuids);
                return;
            }
            Logger.getLogger(super.getClass()).finest("Unknown message type received on event handler :" + messageType);
        }

        /**
         * @param version The version number for this group view
         * @param groupsInfo List of RaftGroupInfo objects containing group IDs, leader, and follower information
         * @param cpToApUuids Mapping of CP UUIDs to AP UUIDs, for use on the client
         */
        public abstract void handleGroupsViewEvent(long version, java.util.Collection<com.hazelcast.cp.internal.RaftGroupInfo> groupsInfo, java.util.Collection<java.util.Map.Entry<java.util.UUID, java.util.UUID>> cpToApUuids);
    }
}
