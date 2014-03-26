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

package com.google.code.cxf.protobuf.utils;

import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.binding.BindingFactory;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.endpoint.EndpointImpl;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.ServiceImpl;
import org.apache.cxf.service.factory.ServiceConstructionException;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.ConduitInitiatorManager;

import com.google.code.cxf.protobuf.binding.ProtobufBindingFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for building a minimal service model for a protobuf service.
 *
 * @author Gyorgy Orban
 */
public final class ServiceUtils {

    private static Logger log = LoggerFactory.getLogger(ServiceUtils.class);

    private ServiceUtils() {
    }

    public static Service createServiceModel() {
        ServiceInfo serviceInfo = new ServiceInfo();
        // does not make sense for protobuf services
        serviceInfo.setName(new QName("", "protobuf_service_"
                + System.identityHashCode(serviceInfo)));

        InterfaceInfo interfaceInfo = new InterfaceInfo(serviceInfo,
                serviceInfo.getName());
        serviceInfo.setInterface(interfaceInfo);

        Service service = new ServiceImpl(serviceInfo);

        return service;
    }

    public static Endpoint createEndpoint(Bus bus, String address)
            throws EndpointException {
        return createEndpoint(bus, address, createServiceModel());
    }

    public static Endpoint createEndpoint(Bus bus, String address,
                                          Service service) throws EndpointException {
        BindingInfo bindingInfo = createBindingInfo(bus, service,
                ProtobufBindingFactory.PROTOBUF_BINDING_ID);

        return createEndpoint(bus, address, service, bindingInfo);
    }

    public static String getTransportId(Bus bus, String address) {
        ConduitInitiatorManager conduitInitiatorMgr = bus
                .getExtension(ConduitInitiatorManager.class);
        ConduitInitiator conduitInitiator = null;

        if (conduitInitiatorMgr != null) {
            conduitInitiator = conduitInitiatorMgr
                    .getConduitInitiatorForUri(address);
        }
        if (conduitInitiator != null) {
            return conduitInitiator.getTransportIds().get(0);
        } else {
            return null;
        }
    }

    public static Endpoint createEndpoint(Bus bus, String address,
                                          Service service, BindingInfo bindingInfo) throws EndpointException {
        EndpointInfo endpointInfo = createEndpointInfo(bus, service
                .getServiceInfos().get(0), bindingInfo, address);
        EndpointImpl endpoint = new EndpointImpl(bus, service, endpointInfo);

        return endpoint;
    }

    public static EndpointInfo createEndpointInfo(Bus bus, ServiceInfo serviceInfo,
                                                  BindingInfo bindingInfo, String address) {
        String transportURI = getTransportId(bus, address);
        EndpointInfo endpointInfo = new EndpointInfo(serviceInfo, transportURI);

        if (address != null) {
            endpointInfo.setName(new QName(address));
            endpointInfo.setAddress(address);
        }

        System.out.println("seting binding info:" + bindingInfo);
        endpointInfo.setBinding(bindingInfo);

        return endpointInfo;
    }

    public static BindingInfo createBindingInfo(Bus bus, Service service,
                                                String bindingURI) {
        try {
            BindingFactoryManager mgr = bus
                    .getExtension(BindingFactoryManager.class);

            BindingFactory bindingFactory = mgr.getBindingFactory(bindingURI);
            if (bindingFactory != null) {
                BindingInfo bindingInfo = bindingFactory.createBindingInfo(
                        service, bindingURI, null);
                service.getServiceInfos().get(0).addBinding(bindingInfo);

                return bindingInfo;
            } else {
                return null;
            }
        } catch (BusException x) {
            log.error("Failed to access CXF bus",x);
            return null;
        } catch (Exception x) {
            throw new ServiceConstructionException(x);
        }
    }

}
