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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;

import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.google.protobuf.Descriptors.FieldDescriptor;

/**
 * Simple dispatcher for dispatching protobuf messages to user defined service
 * beans. Dispatching is based on matching the payload field name with the java method name.
 * 
 * @author Gyorgy Orban
 */
public class SimpleRpcDispatcher implements RpcDispatcher {
	private Object service;

	private static final Logger log = LogUtils
			.getL7dLogger(SimpleRpcDispatcher.class);

	private Map<FieldDescriptor, Method> fieldToMethod = new HashMap<FieldDescriptor, Method>();

	/**
     * 
     */
	public SimpleRpcDispatcher() {
	}

	/**
	 * @param service
	 */
	public SimpleRpcDispatcher(Object service) {
		super();
		this.service = service;
	}

	public Message dispatchMessage(Message message, Object serviceBean) {
		FieldDescriptor payloadField = resolvePayloadField(message);
		Method method = resolveTargetMethod(message, payloadField);

		RpcController rpcController = new SimpleRpcController();

		Message response;

		try {
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes.length == 3) {
				ResponseHolder responseHolder = new ResponseHolder();
				method.invoke(serviceBean, rpcController, message
						.getField(payloadField), responseHolder);
				response = responseHolder.response;
			} else if (parameterTypes.length == 2) {
				response = (Message) method.invoke(serviceBean, rpcController,
						message.getField(payloadField));
			} else if (parameterTypes.length == 1) {
				response = (Message) method.invoke(serviceBean, message
						.getField(payloadField));
			} else {
				throw new IllegalStateException(
						"No appropriate handler method found by the name "
								+ payloadField.getName());
			}
		} catch (Exception e) {
			log.throwing(SimpleRpcDispatcher.class.getName(),
					"dispatchMessage", e);
			throw new RuntimeException(
					"Exception occured while invoking method "
							+ payloadField.getName() + " on service bean "
							+ serviceBean, e);
		}

		return response;
	}

	/**
	 * Find out which method to call on the service bean.
	 */
	protected Method resolveTargetMethod(Message message,
			FieldDescriptor payloadField) {
		Method targetMethod = fieldToMethod.get(payloadField);

		if (targetMethod == null) {
			// look up and cache target method; this block is called only once
			// per target method, so synchronized is ok
			synchronized (this) {
				targetMethod = fieldToMethod.get(payloadField);
				if (targetMethod == null) {
					String name = payloadField.getName();
					for (Method method : service.getClass().getMethods()) {
						if (method.getName().equals(name)) {
							try {
								method.setAccessible(true);
							} catch (Exception x) {
								// ignore this
							}

							targetMethod = method;
							fieldToMethod.put(payloadField, targetMethod);
							break;
						}
					}
				}
			}
		}

		if (targetMethod != null) {
			return targetMethod;
		} else {
			throw new RuntimeException("No matching method found by the name '"
					+ payloadField.getName() + "'");
		}
	}

	/**
	 * Find out which field in the incoming message contains the payload that is
	 * delivered to the service method.
	 */
	protected FieldDescriptor resolvePayloadField(Message message) {
		for (FieldDescriptor field : message.getDescriptorForType().getFields()) {
			if (message.hasField(field)) {
				return field;
			}
		}

		throw new RuntimeException("No payload found in message " + message);
	}

	protected static class ResponseHolder implements RpcCallback<Message> {
		private Message response;

		public void run(Message response) {
			this.response = response;
		}
	}

	public static class SimpleRpcController implements RpcController {
		private String errorText;

		private boolean failed;

		public String errorText() {
			return errorText;
		}

		public boolean failed() {
			return failed;
		}

		public boolean isCanceled() {
			return false;
		}

		public void notifyOnCancel(RpcCallback<Object> callback) {
			throw new RuntimeException("Not supported for SimpleRpcDispatcher.");
		}

		public void reset() {
			throw new RuntimeException("Not supported for SimpleRpcDispatcher.");
		}

		public void setFailed(String errorText) {
			this.errorText = errorText;
			failed = true;
		}

		public void startCancel() {
			throw new RuntimeException("Not supported for SimpleRpcDispatcher.");
		}

	}

	/**
	 * @return the service
	 */
	public Object getService() {
		return service;
	}

	/**
	 * @param service
	 *            the service to set
	 */
	public void setService(Object service) {
		this.service = service;
	}

}
