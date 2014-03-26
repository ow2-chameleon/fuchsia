/**
 * Licensed to the Apache Software Foundation (ASF) under one.
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.google.code.cxf.protobuf.client;

import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.endpoint.EndpointException;

import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcChannel;
import com.google.protobuf.RpcController;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Message.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple RpcChannel implementation that uses CXF for sending and receiving messages.
 *
 * @author Gyorgy Orban
 */
public class SimpleRpcChannel implements RpcChannel {

    private ProtobufClient messageSender;

    private Map<String, FieldDescriptor> fieldNameToDescriptor = new HashMap<String, FieldDescriptor>();

    private Class<? extends Message> wrapperMessage;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * @param messageSender
     * @param wrapperMessage
     * @throws org.apache.cxf.endpoint.EndpointException
     */
    public SimpleRpcChannel(String address, Class<? extends Message> wrapperMessage) throws EndpointException {
        this.messageSender = new ProtobufClient(address, wrapperMessage);
        this.wrapperMessage = wrapperMessage;

        initialize();
    }

    /**
     *
     */
    private void initialize() {
        try {
            Descriptor wrapperDescriptor = (Descriptor) wrapperMessage.getMethod("getDescriptor").invoke(null);

            for (FieldDescriptor fieldDescriptor : wrapperDescriptor.getFields()) {
                fieldNameToDescriptor.put(fieldDescriptor.getName(), fieldDescriptor);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void callMethod(MethodDescriptor method, RpcController controller, Message request,
                           Message responsePrototype, RpcCallback<Message> done) {
        try {
            String fieldName = method.getName();
            Builder builder = (Builder) wrapperMessage.getMethod("newBuilder").invoke(null);

            FieldDescriptor fieldDescriptor = fieldNameToDescriptor.get(fieldName);
            if (fieldDescriptor == null) {
                throw new IllegalStateException(
                        "No field with the name '"
                                + fieldName
                                + "' is defined in wrapper message "
                                + wrapperMessage
                                + ". A corresponding field must exist in the wrapper message for each operation in a service definition.");
            }

            builder.setField(fieldDescriptor, request);
            Message wrapperMessage = builder.build();
            boolean isOneWay = method.getOutputType() == null;

            responsePrototype.getDescriptorForType();

            messageSender.send(wrapperMessage, controller, request, responsePrototype, done, isOneWay);

        } catch (Exception e) {
            log.error("Failed to invoke method.",e);
            throw new RuntimeException(e);
        }
    }
}
