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

package tuwien.auto.calimero.link.event;

import tuwien.auto.calimero.FrameEvent;
import tuwien.auto.calimero.cemi.CEMI;
import tuwien.auto.calimero.link.KNXNetworkMonitor;
import tuwien.auto.calimero.link.medium.RawFrame;

/**
 * Informs about a new monitor indication received from the KNX network and contains the
 * received indication frame.
 * <p>
 * The source of the event is a {@link KNXNetworkMonitor}.
 * 
 * @author B. Malinowsky
 */
public class MonitorFrameEvent extends FrameEvent
{
	// ??? ctor which takes error information if raw frame creation failed

	private static final long serialVersionUID = 1L;

	private final RawFrame raw;

	/**
	 * Creates a new monitor frame event with the indication frame.
	 * 
	 * @param source the network monitor which received the frame
	 * @param frame monitor indication frame encapsulated in cEMI type
	 */
	public MonitorFrameEvent(Object source, CEMI frame)
	{
		super(source, frame);
		raw = null;
	}

	/**
	 * Creates a new monitor frame event with the indication frame and the decoded raw
	 * frame.
	 * 
	 * @param source the network monitor which received the frame
	 * @param frame monitor indication frame encapsulated in cEMI type
	 * @param rawFrame the decoded raw frame on medium encapsulated in type RawFrame, use
	 *        <code>null</code> if no decoded raw frame is available
	 */
	public MonitorFrameEvent(Object source, CEMI frame, RawFrame rawFrame)
	{
		super(source, frame);
		raw = rawFrame;
	}

	/**
	 * Returns the decoded raw frame on medium.
	 * <p>
	 * 
	 * @return the frame of type RawFrame or <code>null</code> on no decoded raw frame
	 */
	public final RawFrame getRawFrame()
	{
		return raw;
	}
}
