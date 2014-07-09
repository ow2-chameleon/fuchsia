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

package tuwien.auto.calimero.knxnetip.util;

import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.exception.KNXFormatException;
import tuwien.auto.calimero.exception.KNXIllegalArgumentException;
import tuwien.auto.calimero.knxnetip.KNXnetIPTunnel;

/**
 * Connection response data used for KNX tunneling connection.
 * <p>
 * 
 * @author B. Malinowsky
 */
public class TunnelCRD extends CRD
{
	/**
	 * Creates a new CRD for tunnel connection type out of a byte array.
	 * <p>
	 * The CRD structure has a length of 4 bytes.<br>
	 * 
	 * @param data byte array containing a CRD structure,
	 *        <code>data.length - offset = 4</code>
	 * @param offset start offset of CRD in <code>data</code>
	 * @throws KNXFormatException if no CRD found or invalid structure
	 */
	public TunnelCRD(byte[] data, int offset) throws KNXFormatException
	{
		super(data, offset);
		if (getConnectionType() != KNXnetIPTunnel.TUNNEL_CONNECTION)
			throw new KNXFormatException("not a tunneling CRD", getConnectionType());
		if (getStructLength() != 4)
			throw new KNXFormatException("wrong length for tunneling CRD");
	}

	/**
	 * Creates a new CRD for tunnel connection type containing the given address
	 * information.
	 * <p>
	 * 
	 * @param addr individual address assigned to the tunneling connection
	 */
	public TunnelCRD(IndividualAddress addr)
	{
		super(KNXnetIPTunnel.TUNNEL_CONNECTION, addr.toByteArray());
	}

	/**
	 * Creates a CRD for tunnel connection type containing optional data.
	 * <p>
	 * Note, the optional data field contains the assigned individual address.<br>
	 * 
	 * @param optionalData byte array containing tunneling host protocol data, this
	 *        information is located starting at offset 2 in the CRD structure,
	 *        <code>optionalData.length</code> = 2
	 */
	TunnelCRD(byte[] optionalData)
	{
		super(KNXnetIPTunnel.TUNNEL_CONNECTION, (byte[]) optionalData.clone());
		if (getStructLength() != 4)
			throw new KNXIllegalArgumentException("wrong length for tunneling CRD");
	}

	/**
	 * Returns the assigned address for the tunneling connection.
	 * <p>
	 * 
	 * @return individual address as {@link IndividualAddress}
	 */
	public final IndividualAddress getAssignedAddress()
	{
		return new IndividualAddress(opt);
	}

	/**
	 * Returns a textual representation of this tunnel CRD.
	 * <p>
	 * 
	 * @return a string representation of the object
	 */
	public String toString()
	{
		return "tunneling CRD, assigned address " + getAssignedAddress();
	}
}
