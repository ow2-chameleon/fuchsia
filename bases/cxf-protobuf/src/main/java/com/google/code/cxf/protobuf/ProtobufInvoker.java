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

package com.google.code.cxf.protobuf;

import org.apache.cxf.message.Exchange;
import org.apache.cxf.service.invoker.Invoker;

import com.google.protobuf.Message;

/**
 * Simple invoker for protobuf service beans.
 * 
 * @author Gyorgy Orban
 */
public class ProtobufInvoker implements Invoker {
    private RpcDispatcher rpcDispatcher;

    private Object serviceBean;

    /**
     * 
     */
    public ProtobufInvoker() {
    }

    /**
     * @param rpcDispatcher
     * @param serviceBean
     */
    public ProtobufInvoker(RpcDispatcher rpcDispatcher, Object serviceBean) {
        super();
        this.rpcDispatcher = rpcDispatcher;
        this.serviceBean = serviceBean;

        if (rpcDispatcher == null && !(serviceBean instanceof ProtobufMessageHandler)) {
            throw new IllegalStateException(
                    "Either an RpcDispatcher must be specified or the serviceBean must be an instance of ProtobufMessageHandler.");
        }
    }

    /**
     * @see org.apache.cxf.service.invoker.Invoker#invoke(org.apache.cxf.message.Exchange, java.lang.Object)
     */
    public Object invoke(Exchange exchange, Object o) {
        Message message = exchange.getInMessage().getContent(Message.class);

        if (rpcDispatcher != null) { // we do RPC
            return rpcDispatcher.dispatchMessage(message, serviceBean);
        } else if (serviceBean instanceof ProtobufMessageHandler) { // just handle the protobuf message, no RPC
            return ((ProtobufMessageHandler) serviceBean).handleMessage(message);
        } else {
            throw new IllegalStateException("Unknown service bean type, unable to invoke service.");
        }
    }

    public RpcDispatcher getRpcDispatcher() {
        return rpcDispatcher;
    }

    public void setRpcDispatcher(RpcDispatcher rpcDispatcher) {
        this.rpcDispatcher = rpcDispatcher;
    }

    public Object getServiceBean() {
        return serviceBean;
    }

    public void setServiceBean(Object serviceBean) {
        this.serviceBean = serviceBean;
    }

}
