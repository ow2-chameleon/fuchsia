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

package tuwien.auto.calimero.link.medium;

import tuwien.auto.calimero.exception.KNXFormatException;

/**
 * Raw acknowledge frame on TP1 communication medium.
 * <p>
 * 
 * @author B. Malinowsky
 */
public class TP1Ack extends RawAckBase
{
	/**
	 * Acknowledge type busy.
	 * <p>
	 * The remote data link layer does not have resources to process the request.
	 * 
	 * @see #getAckType()
	 */
	public static final int BUSY = 0xC0;

	/**
	 * Acknowledge type {@link RawAckBase#NAK} and {@link TP1Ack#BUSY}.
	 * <p>
	 * This acknowledge shall be handled as acknowledge of type busy.
	 * 
	 * @see #getAckType()
	 */
	public static final int NAK_BUSY = 0x00;

	/**
	 * Creates a new TP1 acknowledge frame out of a byte array.
	 * <p>
	 * 
	 * @param data byte array containing the acknowledge frame
	 * @param offset start offset of frame structure in <code>data</code>, offset &gt;=
	 *        0
	 * @throws KNXFormatException if no valid acknowledge frame was found
	 */
	public TP1Ack(byte[] data, int offset) throws KNXFormatException
	{
		final int ctrl = data[offset] & 0xff;
		if ((ctrl & 0x33) != 0x00)
			throw new KNXFormatException("no TP1 ACK frame, invalid control field", ctrl);
		if (ctrl == ACK)
			ack = ACK;
		else if (ctrl == NAK)
			ack = NAK;
		else if (ctrl == BUSY)
			ack = BUSY;
		else if (ctrl == NAK_BUSY)
			ack = NAK_BUSY;
		else
			throw new KNXFormatException("no valid acknowledge code", ctrl);
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.link.medium.RawAckBase#toString()
	 */
	public String toString()
	{
		return ack == BUSY ? "BUSY" : ack == NAK_BUSY ? "NAK-BUSY" : super.toString();
	}
}
