/*
 * #%L
 * OW2 Chameleon - Fuchsia Framework
 * %%
 * Copyright (C) 2009 - 2015 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
/**
 * openHAB, the open Home Automation Bus.
 * Copyright (C) 2010-2012, openHAB.org <admin@openhab.org>
 *
 * See the contributors.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or
 * combining it with Eclipse (or a modified version of that library),
 * containing parts covered by the terms of the Eclipse Public License
 * (EPL), the licensors of this Program grant you additional permission
 * to convey the resulting work.
 */
package org.ow2.chameleon.fuchsia.importer.zwave.internal.commandclass;

import org.ow2.chameleon.fuchsia.importer.zwave.internal.protocol.SerialMessage;
import org.ow2.chameleon.fuchsia.importer.zwave.internal.protocol.ZWaveController;
import org.ow2.chameleon.fuchsia.importer.zwave.internal.protocol.ZWaveEndpoint;
import org.ow2.chameleon.fuchsia.importer.zwave.internal.protocol.ZWaveNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the no operation command class. The No Operation command class is used 
 * to check if a node is reachable by sending a serial message without a command 
 * to the specified node. This can for instance be used to check that a node is 
 * non-responding.
 * 
 * @author Jan-Willem Spuij
 * @since 1.3.0
 */
public class ZWaveNoOperationCommandClass extends ZWaveCommandClass {

	private static final Logger logger = LoggerFactory.getLogger(ZWaveNoOperationCommandClass.class);
	
	/**
	 * Creates a new instance of the ZWaveNoOperationCommandClass class.
	 * @param node the node this command class belongs to
	 * @param controller the controller to use
	 * @param endpoint the endpoint this Command class belongs to
	 */
	public ZWaveNoOperationCommandClass(ZWaveNode node,
			ZWaveController controller, ZWaveEndpoint endpoint) {
		super(node, controller, endpoint);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public CommandClass getCommandClass() {
		return CommandClass.NO_OPERATION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleApplicationCommandRequest(SerialMessage serialMessage,
			int offset, int endpoint) {
		logger.trace("Handle No Operation Request");
		logger.debug(String.format("Received No Operation for Node ID = %d", this.getNode().getNodeId()));
		
		// advance node stage.
		this.getNode().advanceNodeStage();
	}

	/**
	 * Gets a SerialMessage with the No Operation command 
	 * @return the serial message
	 */
	public SerialMessage getNoOperationMessage() {
		logger.debug("Creating new message for application command No Operation for node {}", this.getNode().getNodeId());
		SerialMessage result = new SerialMessage(this.getNode().getNodeId(), SerialMessage.SerialMessageClass.SendData, SerialMessage.SerialMessageType.Request, SerialMessage.SerialMessageClass.SendData, SerialMessage.SerialMessagePriority.Low);
    	byte[] newPayload = { 	(byte) this.getNode().getNodeId(), 
    							1, 
								(byte) getCommandClass().getKey() }; 
    	result.setMessagePayload(newPayload);
    	return result;		
	}

}
