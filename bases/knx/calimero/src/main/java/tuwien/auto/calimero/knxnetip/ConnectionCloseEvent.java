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

package tuwien.auto.calimero.knxnetip;

import tuwien.auto.calimero.CloseEvent;

/**
 * An event providing information regarding the closing of an established KNXnet/IP
 * communication connection.
 * <p>
 */
public class ConnectionCloseEvent extends CloseEvent
{
	/**
	 * Identifier stating the close event was initiated by the client.
	 * <p>
	 */
	public static final int CLIENT = 0;

	/**
	 * Identifier stating the close event was initiated by the remote endpoint/server.
	 * <p>
	 */
	public static final int SERVER = 1;

	/**
	 * Identifier stating the close event was initiated internal (for example due to an
	 * unsupported protocol version).
	 * <p>
	 */
	public static final int INTERNAL = 2;

	private static final long serialVersionUID = 1L;

	private final int initiator;

	/**
	 * Creates a new close event.
	 * <p>
	 * 
	 * @param source the object on which the Event initially occurred
	 * @param initiator initiator of the close event, one of {@link #CLIENT},
	 *        {@link #SERVER} or {@link #INTERNAL}
	 * @param reason brief textual description
	 */
	public ConnectionCloseEvent(Object source, int initiator, String reason)
	{
		super(source, initiator == CLIENT, reason);
		this.initiator = initiator;
	}

	/**
	 * Returns the initiator of the close event, one of {@link #CLIENT}, {@link #SERVER}
	 * or {@link #INTERNAL}.
	 * <p>
	 * 
	 * @return identifier of the initiator
	 */
	public final int getInitiator()
	{
		return initiator;
	}
}
