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

import com.google.code.cxf.protobuf.interceptor.ProtobufMessageInInterceptor;
import com.google.code.cxf.protobuf.interceptor.ProtobufMessageOutInterceptor;
import com.google.code.cxf.protobuf.utils.ExchangeUtils;
import com.google.code.cxf.protobuf.utils.ServiceUtils;
import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.binding.Binding;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.endpoint.ConduitSelector;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.endpoint.UpfrontConduitSelector;
import org.apache.cxf.interceptor.*;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.phase.PhaseChainCache;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.phase.PhaseManager;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.transport.MessageObserver;
import org.apache.cxf.workqueue.SynchronousExecutor;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

/**
 * Simple CXF client for sending protobuf messages.
 */
public class ProtobufClient extends AbstractBasicInterceptorProvider implements
        MessageObserver, InterceptorProvider {

    private static final Logger log = LogUtils
            .getL7dLogger(ProtobufClient.class);

    private String address;

    private Bus bus;

    private InterceptorChain outgoingInterceptorChain;

    private PhaseChainCache outboundChainCache;

    private PhaseChainCache inboundChainCache;

    private Executor incomingExecutor;

    private ClientOutFaultObserver outFaultObserver;

    private Endpoint endpoint;

    /**
     *
     */
    public ProtobufClient() {
    }

    /**
     * @param address
     * @param wrapperMessage
     * @throws org.apache.cxf.endpoint.EndpointException
     */
    public ProtobufClient(String address, Class<? extends Message> messageClass)
            throws EndpointException {
        super();
        this.address = address;
        this.endpoint = ServiceUtils.createEndpoint(getBus(), address);
        endpoint.put(Message.class.getName(), messageClass);

        getOutInterceptors().add(new ProtobufMessageOutInterceptor());
        getInInterceptors().add(new ProtobufMessageInInterceptor());
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    public void sendMessage(Message wrapperMessage, RpcController controller,
                            Message request) {
        send(wrapperMessage, controller, request, null, null, true);
    }

    public void sendRequest(Message wrapperMessage, RpcController controller,
                            Message request, Message responsePrototype,
                            RpcCallback<Message> done) {
        send(wrapperMessage, controller, request, responsePrototype, done,
                false);
    }

    protected final Exchange send(Message wrapperMessage,
                                  RpcController controller, Message request,
                                  Message responsePrototype, RpcCallback<Message> done,
                                  final boolean isOneWay) {
        initialize();

        Exchange exchange = new ExchangeImpl();
        org.apache.cxf.message.Message message = new MessageImpl();

        try {
            exchange.put(RpcController.class, controller);

            exchange.put(RpcCallback.class, done);

            exchange.put(ProtobufMessageInInterceptor.IN_MESSAGE_CLASS,
                    responsePrototype.getClass());

            exchange.setOutMessage(message);

            exchange.setOneWay(isOneWay);

            prepareConduitSelector(message);

            InterceptorChain chain = setupInterceptorChain(exchange);

            message.setInterceptorChain(chain);

            message.setContent(Object.class, wrapperMessage);

            chain.doIntercept(message);

        } catch (Exception exception) {
            exchange.put(Exception.class, exception);
            log.throwing(ProtobufClient.class.getName(), "send", exception);
        } finally {

            if (isOneWay) {
                ExchangeUtils.setExchangeFinished(exchange);
            }

            Exception exception = ExchangeUtils.getException(exchange);
            if (exception != null) {
                if (exception instanceof RuntimeException) {
                    throw (RuntimeException) exception;
                } else {
                    throw new RuntimeException(exception);
                }
            }
        }

        return exchange;
    }

    protected ConduitSelector prepareConduitSelector(
            org.apache.cxf.message.Message message) {
        ConduitSelector conduitSelector = new UpfrontConduitSelector();

        conduitSelector.setEndpoint(endpoint);

        setExchangeProperties(message.getExchange(), endpoint);

        conduitSelector.prepare(message);
        message.getExchange().put(ConduitSelector.class, conduitSelector);

        return conduitSelector;

    }

    protected void setExchangeProperties(Exchange exchange, Endpoint ep) {
        if (ep != null) {
            exchange.put(Endpoint.class, ep);
            exchange.put(Service.class, ep.getService());
            if (ep.getEndpointInfo().getService() != null) {
                exchange.put(ServiceInfo.class, ep.getEndpointInfo()
                        .getService());
                exchange.put(InterfaceInfo.class, ep.getEndpointInfo()
                        .getService().getInterface());
            }
            exchange.put(Binding.class, ep.getBinding());
            exchange.put(BindingInfo.class, ep.getEndpointInfo().getBinding());
        }

        exchange.put(MessageObserver.class, this);
        exchange.put(Bus.class, getBus());
    }

    /**
     * @return the bus
     */
    public Bus getBus() {
        if (bus == null) {
            bus = BusFactory.getThreadDefaultBus();
        }

        return bus;
    }

    /**
     * @param bus the bus to set
     */
    public void setBus(Bus bus) {
        this.bus = bus;
    }

    public void onMessage(final org.apache.cxf.message.Message inMessage) {
        incomingExecutor.execute(new Runnable() {

            public void run() {
                org.apache.cxf.message.Message message = inMessage;
                Exchange exchange = inMessage.getExchange();

                try {
                    Endpoint endpoint = message.getExchange().get(
                            Endpoint.class);
                    if (endpoint == null) {

                        endpoint = getConduitSelector(message).getEndpoint();
                        message.getExchange().put(Endpoint.class, endpoint);
                    }
                    message = endpoint.getBinding().createMessage(message);

                    message.put(org.apache.cxf.message.Message.REQUESTOR_ROLE,
                            Boolean.TRUE);
                    message.put(org.apache.cxf.message.Message.INBOUND_MESSAGE,
                            Boolean.TRUE);

                    Throwable exception = ExchangeUtils.getException(inMessage
                            .getExchange());
                    if (exception == null) {
                        PhaseManager pm = bus.getExtension(PhaseManager.class);

                        @SuppressWarnings("unchecked")
                        List<Interceptor<? extends org.apache.cxf.message.Message>> i1 = bus.getInInterceptors();
                        @SuppressWarnings("unchecked")
                        List<Interceptor<? extends org.apache.cxf.message.Message>> i2 = endpoint.getInInterceptors();
                        @SuppressWarnings("unchecked")
                        List<Interceptor<? extends org.apache.cxf.message.Message>> i3 = getInInterceptors();
                        @SuppressWarnings("unchecked")
                        List<Interceptor<? extends org.apache.cxf.message.Message>> i4 = endpoint.getBinding()
                                .getInInterceptors();

                        PhaseInterceptorChain chain = inboundChainCache.get(pm
                                .getInPhases(), i1, i2, i3, i4);
                        message.setInterceptorChain(chain);

                        chain.setFaultObserver(outFaultObserver);

                        String startingAfterInterceptorID = (String) message
                                .get(PhaseInterceptorChain.STARTING_AFTER_INTERCEPTOR_ID);
                        String startingInterceptorID = (String) message
                                .get(PhaseInterceptorChain.STARTING_AT_INTERCEPTOR_ID);
                        if (startingAfterInterceptorID != null) {
                            chain.doInterceptStartingAfter(message,
                                    startingAfterInterceptorID);
                        } else if (startingInterceptorID != null) {
                            chain.doInterceptStartingAt(message,
                                    startingInterceptorID);
                        } else {
                            chain.doIntercept(message);
                        }
                    } else {

                    }
                } finally {
                    exchange = message.getExchange();

                    @SuppressWarnings(value = "unchecked")
                    RpcCallback<Message> rpcCallback = exchange
                            .get(RpcCallback.class);
                    if (rpcCallback != null) {
                        rpcCallback.run(message.getContent(Message.class));
                        if (ExchangeUtils.isExchangeFinished(exchange)) {
                            try {
                                ExchangeUtils.closeConduit(exchange);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }
        });
    }

    protected InterceptorChain setupInterceptorChain(Exchange exchange) {
        if (outgoingInterceptorChain != null) {
            return outgoingInterceptorChain;
        }

        Endpoint endpoint = getEndpoint(exchange);

        PhaseManager pm = bus.getExtension(PhaseManager.class);
        @SuppressWarnings("unchecked")
        List<Interceptor<? extends org.apache.cxf.message.Message>> i1 = bus.getOutInterceptors();
        @SuppressWarnings("unchecked")
        List<Interceptor<? extends org.apache.cxf.message.Message>> i2 = endpoint.getOutInterceptors();
        @SuppressWarnings("unchecked")
        List<Interceptor<? extends org.apache.cxf.message.Message>> i3 = getOutInterceptors();
        @SuppressWarnings("unchecked")
        List<Interceptor<? extends org.apache.cxf.message.Message>> i4 = endpoint.getBinding().getOutInterceptors();

        PhaseInterceptorChain phaseInterceptorChain = outboundChainCache.get(pm
                .getOutPhases(), i1, i2, i3, i4);
        return phaseInterceptorChain;
    }

    protected Endpoint getEndpoint(Exchange exchange) {
        return exchange.get(Endpoint.class);
    }

    protected ConduitSelector getConduitSelector(
            org.apache.cxf.message.Message message) {
        return message.getExchange().get(ConduitSelector.class);
    }

    protected void initialize() {
        if (outFaultObserver == null) {
            outFaultObserver = new ClientOutFaultObserver(getBus());
        }

        if (incomingExecutor == null) {
            incomingExecutor = SynchronousExecutor.getInstance();
        }

        if (inboundChainCache == null) {
            inboundChainCache = new PhaseChainCache();
        }

        if (outboundChainCache == null) {
            outboundChainCache = new PhaseChainCache();
        }

    }

}
