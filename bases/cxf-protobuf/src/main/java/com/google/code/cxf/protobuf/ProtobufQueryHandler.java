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

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.cxf.Bus;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.http.UrlUtilities;
import org.apache.cxf.transports.http.QueryHandlerRegistry;
import org.apache.cxf.transports.http.StemMatchingQueryHandler;

import com.google.code.cxf.protobuf.utils.ProtoGenerator;
import com.google.protobuf.Message;
import com.google.protobuf.Descriptors.Descriptor;

/**
 * Query handler for ?proto requests.
 * 
 * @author Gyorgy Orban
 */
public class ProtobufQueryHandler implements StemMatchingQueryHandler {
	protected Bus bus;

	public boolean isRecognizedQuery(String baseUri, String ctx,
			EndpointInfo endpointInfo, boolean contextMatchExact) {
		if (baseUri != null
				&& (baseUri.contains("?") && (baseUri.toLowerCase()
						.contains("proto")))) {

			int idx = baseUri.indexOf("?");
			Map<String, String> map = UrlUtilities.parseQueryString(baseUri
					.substring(idx + 1));
			if (map.containsKey("proto")) {
				if (contextMatchExact) {
					return endpointInfo.getAddress().contains(ctx);
				} else {
					// contextMatchStrategy will be "stem"
					return endpointInfo.getAddress().contains(
							UrlUtilities.getStem(baseUri.substring(0, idx)));
				}
			}
		}
		return false;
	}

	/**
	 * 
	 * @see org.apache.cxf.transports.http.QueryHandler#getResponseContentType(java.lang.String,
	 *      java.lang.String)
	 */
	public String getResponseContentType(String fullQueryString, String ctx) {
		return "text/plain";
	}

	/**
	 * 
	 * @see org.apache.cxf.transports.http.QueryHandler#isRecognizedQuery(java.lang.String,
	 *      java.lang.String, org.apache.cxf.service.model.EndpointInfo)
	 */
	public boolean isRecognizedQuery(String fullQueryString, String ctx,
			EndpointInfo endpoint) {
		return isRecognizedQuery(fullQueryString, ctx, endpoint, false);
	}

	/**
	 * 
	 * @see org.apache.cxf.transports.http.QueryHandler#writeResponse(java.lang.String,
	 *      java.lang.String, org.apache.cxf.service.model.EndpointInfo,
	 *      java.io.OutputStream)
	 */
	@SuppressWarnings("unchecked")
	public void writeResponse(String fullQueryString, String ctx,
			EndpointInfo endpoint, OutputStream os) {
		try {
			Class<? extends Message> messageClass = endpoint.getProperty(
					ProtobufServerFactoryBean.PROTOBUF_MESSAGE_CLASS,
					Class.class);
			PrintStream out = new PrintStream(os);
			Descriptor wrapperMessage = ((Descriptor) messageClass.getMethod(
					"getDescriptor").invoke(null));
			new ProtoGenerator().generateProtoFromDescriptor(wrapperMessage
					.getFile(), out, wrapperMessage);
			out.flush();
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	@PostConstruct
	void registerWithQueryHandlerRegistry() {
		QueryHandlerRegistry queryHandlerRegistry = bus
				.getExtension(QueryHandlerRegistry.class);
		queryHandlerRegistry.registerHandler(new ProtobufQueryHandler());
	}

	public void setBus(Bus bus) {
		this.bus = bus;
	}

	public Bus getBus() {
		return bus;
	}
}