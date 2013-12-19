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

package com.google.code.cxf.protobuf.binding;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.binding.AbstractBaseBindingFactory;
import org.apache.cxf.binding.AbstractBindingFactory;
import org.apache.cxf.binding.Binding;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingInfo;

import com.google.code.cxf.protobuf.interceptor.ProtobufMessageInInterceptor;
import com.google.code.cxf.protobuf.interceptor.ProtobufMessageOutInterceptor;
import org.apache.cxf.service.model.ServiceInfo;

import javax.xml.namespace.QName;

/**
 * Binding factory for protobuf bindings. Adds interceptors for parsing and
 * writing out protobuf messages to the CXF stream.
 * 
 * @author Gyorgy Orban
 */
public class ProtobufBindingFactory extends AbstractBaseBindingFactory { //AbstractBindingFactory

	public static final String PROTOBUF_BINDING_ID = "http://apache.org/cxf/binding/protobuf";
    private Bus cxfbus;

	public ProtobufBindingFactory(Bus bus) {

        this.cxfbus=bus;

        super.setBus(bus);

	}

	public Binding createBinding(BindingInfo bi) {
		ProtobufBinding binding = new ProtobufBinding(bi);

		binding.getInInterceptors().add(new ProtobufMessageInInterceptor());

		binding.getOutInterceptors().add(new ProtobufMessageOutInterceptor());

		return binding;
	}

	/*
	 * The concept of BindingInfo can not be applied to protocol buffers. Here
	 * we create BindingInfo merely to make this compatible with CXF framework.
	 */
	public BindingInfo createBindingInfo(Service service, String namespace,
			Object obj) {

        ServiceInfo si=new ServiceInfo();
        si.setTargetNamespace(ProtobufBindingFactory.PROTOBUF_BINDING_ID);

		BindingInfo info = new BindingInfo(si,
				ProtobufBindingFactory.PROTOBUF_BINDING_ID);

		return info;
	}

    @Override
    public Bus getBus() {
        if(cxfbus==null)
            cxfbus = BusFactory.getThreadDefaultBus();

        return cxfbus;
    }
}