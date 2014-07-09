/*
 * #%L
 * OW2 Chameleon - Fuchsia Framework
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
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
/*
    Calimero - A library for KNX network access
    Copyright (C) 2006-2008 B. Malinowsky

    This program is free software; you can redistribute it and/or 
    modify it under the terms of the GNU General Public License 
    as published by the Free Software Foundation; either version 2 
    of the License, or at your option any later version. 
 
    This program is distributed in the hope that it will be useful, 
    but WITHOUT ANY WARRANTY; without even the implied warranty of 
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
    GNU General Public License for more details. 
 
    You should have received a copy of the GNU General Public License 
    along with this program; if not, write to the Free Software 
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA. 
 
    Linking this library statically or dynamically with other modules is 
    making a combined work based on this library. Thus, the terms and 
    conditions of the GNU General Public License cover the whole 
    combination. 
 
    As a special exception, the copyright holders of this library give you 
    permission to link this library with independent modules to produce an 
    executable, regardless of the license terms of these independent 
    modules, and to copy and distribute the resulting executable under terms 
    of your choice, provided that you also meet, for each linked independent 
    module, the terms and conditions of the license of that module. An 
    independent module is a module which is not derived from or based on 
    this library. If you modify this library, you may extend this exception 
    to your version of the library, but you are not obligated to do so. If 
    you do not wish to do so, delete this exception statement from your 
    version. 
*/

package tuwien.auto.calimero.process;

import java.util.EventObject;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.IndividualAddress;

/**
 * Contains information about a process message event.
 * <p>
 * 
 * @author B. Malinowsky
 * @see ProcessCommunicator
 */
public class ProcessEvent extends EventObject
{
	private static final long serialVersionUID = 1L;

	private final IndividualAddress src;
	private final GroupAddress dst;
	// We provide the ASDU only to avoid the need of masking out the service code in
	// the user application (the service code is implicitly known through the context
	// of the called method anyway).
	// Nevertheless, if the service code should be of interest at some time, we will
	// just add a getter method for it to this event.
	private final byte[] asdu;

	/**
	 * Creates a new process event with the KNX message source address, destination
	 * address and ASDU.
	 * <p>
	 * 
	 * @param source the receiving process communicator
	 * @param src KNX source individual address of message
	 * @param dst KNX destination address of message
	 * @param asdu byte array with application layer service data unit (ASDU), no copy is
	 *        created
	 */
	public ProcessEvent(Object source, IndividualAddress src, GroupAddress dst,
		byte[] asdu)
	{
		super(source);
		this.src = src;
		this.dst = dst;
		this.asdu = asdu;
	}

	/**
	 * Returns the KNX individual source address.
	 * <p>
	 * 
	 * @return address as IndividualAddress
	 */
	public final IndividualAddress getSourceAddr()
	{
		return src;
	}

	/**
	 * Returns the KNX destination group address.
	 * <p>
	 * 
	 * @return address as GroupAddress
	 */
	public final GroupAddress getDestination()
	{
		return dst;
	}

	/**
	 * Returns the application layer service data unit (ASDU).
	 * <p>
	 * 
	 * @return copy of ASDU as byte array
	 */
	public final byte[] getASDU()
	{
		return (byte[]) asdu.clone();
	}
}
