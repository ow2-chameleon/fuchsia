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

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.cxf.BusException;
import org.apache.cxf.binding.BindingConfiguration;
import org.apache.cxf.binding.BindingFactory;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.endpoint.AbstractEndpointFactory;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.endpoint.EndpointImpl;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.endpoint.ServerImpl;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.factory.ServiceConstructionException;
import org.apache.cxf.service.invoker.Invoker;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.DestinationFactoryManager;

import com.google.code.cxf.protobuf.binding.ProtobufBindingFactory;
import com.google.code.cxf.protobuf.interceptor.ProtobufMessageInInterceptor;
import com.google.protobuf.Message;

/**
 * Server factory for protobuf endpoints.
 * 
 * @author Gyorgy Orban
 * 
 */
public class ProtobufServerFactoryBean extends AbstractEndpointFactory {
    private Server server;

    private Invoker invoker;

    private RpcDispatcher rpcDispatcher;

    private ProtobufServiceFactoryBean serviceFactory;

    private Object serviceBean;

    private Class<? extends Message> messageClass;

    public static String PROTOBUF_MESSAGE_CLASS = ProtobufServerFactoryBean.class.getName() + "."
            + "PROTOBUF_MESSAGE_CLASS";

    /**
     * 
     */
    public ProtobufServerFactoryBean() {
        this(new ProtobufServiceFactoryBean());
    }

    public ProtobufServerFactoryBean(ProtobufServiceFactoryBean serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    protected void initializeDispatcher() {
        try {
            // set default dispatcher
            if (rpcDispatcher == null && !(serviceBean instanceof ProtobufMessageHandler)) {
                rpcDispatcher = new SimpleRpcDispatcher(serviceBean);
            }
        } catch (Exception x) {
            throw new RuntimeException(x);
        }
    }

    @PostConstruct
    public Server create() {
        try {
            initializeDispatcher();

            Endpoint ep = createEndpoint();
            server = new ServerImpl(getBus(), ep, getDestinationFactory(), getBindingFactory());

            if (invoker == null) {
                ep.getService().setInvoker(createInvoker());
            } else {
                ep.getService().setInvoker(invoker);
            }

            server.start();
        } catch (EndpointException e) {
            throw new ServiceConstructionException(e);
        } catch (BusException e) {
            throw new ServiceConstructionException(e);
        } catch (IOException e) {
            throw new ServiceConstructionException(e);
        }

        applyFeatures();
        return server;
    }

    protected void applyFeatures() {
        if (getFeatures() != null) {
            for (AbstractFeature feature : getFeatures()) {
                feature.initialize(server, getBus());
            }
        }
    }

    protected Endpoint createEndpoint() throws BusException, EndpointException {
        serviceFactory.setServiceBean(serviceBean);

        Service service = serviceFactory.getService();

        if (service == null) {
            service = serviceFactory.create();
        }

        EndpointInfo ei = createEndpointInfo();
        Endpoint ep = new EndpointImpl(getBus(), getServiceFactory().getService(), ei);

        if (properties != null) {
            ep.putAll(properties);
        }

        if (getInInterceptors() != null) {
            ep.getInInterceptors().addAll(getInInterceptors());
        }
        if (getOutInterceptors() != null) {
            ep.getOutInterceptors().addAll(getOutInterceptors());
        }
        if (getInFaultInterceptors() != null) {
            ep.getInFaultInterceptors().addAll(getInFaultInterceptors());
        }
        if (getOutFaultInterceptors() != null) {
            ep.getOutFaultInterceptors().addAll(getOutFaultInterceptors());
        }

        ep.put(ProtobufMessageInInterceptor.IN_MESSAGE_CLASS, messageClass);

        return ep;
    }

    /*
     * EndpointInfo contains information form WSDL's physical part such as endpoint address, binding, transport etc. For
     * JAX-RS based EndpointInfo, as there is no WSDL, these information are set manually, eg, default transport is
     * http, binding is JAX-RS binding, endpoint address is from server mainline.
     */
    protected EndpointInfo createEndpointInfo() throws BusException {
        String transportId = getTransportId();
        if (transportId == null && getAddress() != null) {
            DestinationFactory df = getDestinationFactory();
            if (df == null) {
                DestinationFactoryManager dfm = getBus().getExtension(DestinationFactoryManager.class);
                df = dfm.getDestinationFactoryForUri(getAddress());
            }

            if (df != null) {
                transportId = df.getTransportIds().get(0);
            }
        }

        // default to http transport
        if (transportId == null) {
            transportId = "http://schemas.xmlsoap.org/wsdl/soap/http";
        }

        setTransportId(transportId);

        EndpointInfo ei = new EndpointInfo();
        ei.setTransportId(transportId);
        ei.setName(serviceFactory.getService().getName());
        ei.setAddress(getAddress());
        ei.setProperty(PROTOBUF_MESSAGE_CLASS, messageClass);

        BindingInfo bindingInfo = createBindingInfo();
        ei.setBinding(bindingInfo);

        return ei;
    }

    protected BindingInfo createBindingInfo() {
        BindingFactoryManager mgr = getBus().getExtension(BindingFactoryManager.class);
        String binding = getBindingId();
        BindingConfiguration bindingConfig = getBindingConfig();

        if (binding == null && bindingConfig != null) {
            binding = bindingConfig.getBindingId();
        }

        if (binding == null) {
            binding = ProtobufBindingFactory.PROTOBUF_BINDING_ID;
        }

        try {
            BindingFactory bindingFactory = mgr.getBindingFactory(binding);
            setBindingFactory(bindingFactory);
            return bindingFactory.createBindingInfo(serviceFactory.getService(), binding, bindingConfig);
        } catch (BusException ex) {
            ex.printStackTrace();
            // do nothing
        }
        return null;
    }

    protected Invoker createInvoker() {
        return new ProtobufInvoker(rpcDispatcher, serviceBean);
    }

    /**
     * @param serviceBean
     *            the serviceBean to set
     */
    public void setServiceBean(Object serviceBean) {
        this.serviceBean = serviceBean;
    }

    /**
     * @return the serviceBean
     */
    public Object getServiceBean() {
        return serviceBean;
    }

    /**
     * @return the serviceFactory
     */
    public ProtobufServiceFactoryBean getServiceFactory() {
        return serviceFactory;
    }

    /**
     * @param serviceFactory
     *            the serviceFactory to set
     */
    public void setServiceFactory(ProtobufServiceFactoryBean serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    /**
     * @param rpcDispatcher
     *            the rpcDispatcher to set
     */
    public void setRpcDispatcher(RpcDispatcher rpcDispatcher) {
        this.rpcDispatcher = rpcDispatcher;
    }

    /**
     * @return the rpcDispatcher
     */
    public RpcDispatcher getRpcDispatcher() {
        return rpcDispatcher;
    }

    /**
     * @param messageClass
     *            the messageClass to set
     */
    public void setMessageClass(Class<? extends Message> messageClass) {
        this.messageClass = messageClass;
    }

    /**
     * @return the messageClass
     */
    public Class<? extends Message> getMessageClass() {
        return messageClass;
    }
    
    public Server getServer() {
		return server;
	}
}