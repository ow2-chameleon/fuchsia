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

import java.util.Map;
import java.util.concurrent.Executor;

import org.apache.cxf.service.Service;
import org.apache.cxf.service.factory.AbstractServiceFactoryBean;
import org.apache.cxf.service.invoker.Invoker;

/**
 * Service factory bean for protobuf services.
 * 
 * @author Gyorgy Orban
 */
public class ProtobufServiceFactoryBean extends AbstractServiceFactoryBean {

    private Invoker invoker;

    private Executor executor;

    private Map<String, Object> properties;

    private Object serviceBean;

    private RpcDispatcher rpcDispatcher;

    /**
     * @param rpcDispatcher
     * @param serviceBean
     */
    public ProtobufServiceFactoryBean(RpcDispatcher rpcDispatcher, Object serviceBean) {
        super();
        this.rpcDispatcher = rpcDispatcher;
        this.serviceBean = serviceBean;
    }

    public ProtobufServiceFactoryBean() {
    }

    @Override
    public Service create() {
        initializeServiceModel();

        initializeDefaultInterceptors();
        //
        // if (invoker != null) {
        // getService().setInvoker(getInvoker());
        // } else {
        // getService().setInvoker(createInvoker());
        // }

        if (getExecutor() != null) {
            getService().setExecutor(getExecutor());
        }
        if (getDataBinding() != null) {
            getService().setDataBinding(getDataBinding());
        }

        return getService();
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public Invoker getInvoker() {
        return invoker;
    }

    public void setInvoker(Invoker invoker) {
        this.invoker = invoker;
    }

    protected void initializeServiceModel() {
        ProtobufServiceImpl service = new ProtobufServiceImpl(serviceBean);

        setService(service);

        if (properties != null) {
            service.putAll(properties);
        }
    }

    //
    // protected Invoker createInvoker() {
    // return new ProtobufInvoker(rpcDispatcher, serviceBean);
    // }

    public void setServiceBean(Object serviceBean) {
        this.serviceBean = serviceBean;
    }

    public Object getServiceBean() {
        return serviceBean;
    }
}
