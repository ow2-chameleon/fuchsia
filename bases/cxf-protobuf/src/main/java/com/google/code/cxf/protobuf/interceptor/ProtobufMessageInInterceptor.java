/**
 * Licensed to the Apache Software Foundation (ASF) under one
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

package com.google.code.cxf.protobuf.interceptor;

import java.io.InputStream;

import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

/**
 * Parse incoming protocol buffer message.
 * 
 * @author Gyorgy Orban
 */
public class ProtobufMessageInInterceptor extends AbstractPhaseInterceptor<Message> {

    public static final String IN_MESSAGE_CLASS = ProtobufMessageInInterceptor.class.getName() + ".IN_MESSAGE_CLASS";
    
    /**
     * 
     */
    public ProtobufMessageInInterceptor() {
        super(Phase.UNMARSHAL);
    }

    /**
     */
    public void handleMessage(Message message) throws Fault {
        Endpoint endpoint = message.getExchange().get(Endpoint.class);
        @SuppressWarnings(value = "unchecked")
        Class<? extends com.google.protobuf.Message> messageClass = (Class) message.getExchange().get(IN_MESSAGE_CLASS);

        if (messageClass == null) {
            messageClass = (Class) endpoint
            .get(IN_MESSAGE_CLASS);
        }
        
        InputStream in = message.getContent(InputStream.class);

        try {
            com.google.protobuf.Message m = (com.google.protobuf.Message) messageClass.getMethod("parseFrom",
                    InputStream.class).invoke(null, in);
            message.setContent(com.google.protobuf.Message.class, m);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
