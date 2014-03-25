package org.ow2.chameleon.fuchsia.push.base.subscriber;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Base PUbSubHubbub Publisher
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import org.ow2.chameleon.fuchsia.push.base.hub.exception.SubscriptionException;

import java.util.List;

public interface SubscriberOutput {

    int subscribe(String hub, String topicUrl, String hostname, String verifyToken, String leaseSeconds) throws SubscriptionException;

    int unsubscribe(String hub, String topicUrl, String hostname, String verifyToken) throws SubscriptionException;

    List<String> getApprovedActions();

}
