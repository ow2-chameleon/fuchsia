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
    Copyright (C) 2005 Bernhard Erb
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

package tuwien.auto.calimero.knxnetip.servicetype;

import java.io.ByteArrayOutputStream;

import tuwien.auto.calimero.exception.KNXFormatException;
import tuwien.auto.calimero.exception.KNXIllegalArgumentException;
import tuwien.auto.calimero.knxnetip.util.HPAI;

/**
 * A representation of a KNXnet/IP disconnect request.
 * <p>
 * It is used to request the termination of a established logical connection between a
 * client and server, both control endpoints might send such request.<br>
 * The counterpart sent in reply to this request is a disconnect response.
 * <p>
 * Objects of this type are immutable.
 * 
 * @author B. Malinowsky
 * @author Bernhard Erb
 * @see tuwien.auto.calimero.knxnetip.servicetype.DisconnectResponse
 */
public class DisconnectRequest extends ServiceType
{
	private final short channelid;
	private final HPAI endpt;

	/**
	 * Creates a disconnect request out of a byte array.
	 * <p>
	 * 
	 * @param data byte array containing a disconnect request structure
	 * @param offset start offset of request in <code>data</code>
	 * @throws KNXFormatException if no disconnect request was found or invalid structure
	 */
	public DisconnectRequest(byte[] data, int offset) throws KNXFormatException
	{
		super(KNXnetIPHeader.DISCONNECT_REQ);
		if (data.length - offset < 3)
			throw new KNXFormatException("buffer too short for disconnect request");
		int i = offset;
		channelid = (short) (data[i++] & 0xFF);
		// skip reserved field
		++i;
		endpt = new HPAI(data, i);
	}

	/**
	 * Creates a new disconnect request for the connection identified by the channel ID.
	 * <p>
	 * 
	 * @param channelID communication channel ID uniquely identifying the connection to
	 *        close
	 * @param ctrlEndpoint client control endpoint used for disconnect response
	 */
	public DisconnectRequest(short channelID, HPAI ctrlEndpoint)
	{
		super(KNXnetIPHeader.DISCONNECT_REQ);
		if (channelID < 0 || channelID > 0xFF)
			throw new KNXIllegalArgumentException("channel ID out of range [0..255]");
		channelid = channelID;
		endpt = ctrlEndpoint;
	}

	/**
	 * Returns the communication channel identifier of the connection being closed.
	 * <p>
	 * 
	 * @return The communication channel ID as unsigned byte
	 */
	public final short getChannelID()
	{
		return channelid;
	}

	/**
	 * Returns the client control endpoint.
	 * <p>
	 * 
	 * @return control endpoint in a HPAI
	 */
	public final HPAI getEndpoint()
	{
		return endpt;
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.knxnetip.servicetype.ServiceType#getStructLength()
	 */
	short getStructLength()
	{
		return (short) (endpt.getStructLength() + 2);
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.knxnetip.servicetype.ServiceType#toByteArray
	 *      (java.io.ByteArrayOutputStream)
	 */
	byte[] toByteArray(ByteArrayOutputStream os)
	{
		os.write(channelid);
		os.write(0);
		final byte[] buf = endpt.toByteArray();
		os.write(buf, 0, buf.length);
		return os.toByteArray();
	}
}
