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

package tuwien.auto.calimero.knxnetip.servicetype;

import java.io.ByteArrayOutputStream;

import tuwien.auto.calimero.exception.KNXFormatException;

/**
 * Little helpers to handle KNXnet/IP packets and service types.
 * <p>
 * 
 * @author B. Malinowsky
 */
public final class PacketHelper
{
	private PacketHelper()
	{}

	/**
	 * Creates a packet with a KNXnet/IP message header v1.0, containing the specified
	 * service <code>type</code>, and generates the corresponding byte representation
	 * of this structure.
	 * 
	 * @param type service type to pack
	 * @return the packet as byte array
	 */
	public static byte[] toPacket(ServiceType type)
	{
		final KNXnetIPHeader h = new KNXnetIPHeader(type.svcType, type.getStructLength());
		final ByteArrayOutputStream os = new ByteArrayOutputStream(h.getTotalLength());
		os.write(h.toByteArray(), 0, h.getStructLength());
		return type.toByteArray(os);
	}

	/**
	 * Creates a new service request using the <code>data</code> byte array and
	 * information from the KNXnet/IP header.
	 * <p>
	 * 
	 * @param h KNXnet/IP header associated with <code>data</code>
	 * @param data byte array containing the data following the KNXnet/IP header in the
	 *        message structure
	 * @param offset offset into <code>data</code> pointing at the begin of usable data
	 * @return the new service request
	 * @throws KNXFormatException on failure creating request, for example if data buffer
	 *         is too short for the request, on unsupported service type or connection
	 *         header structure
	 */
	public static ServiceRequest getServiceRequest(KNXnetIPHeader h, byte[] data,
		int offset) throws KNXFormatException
	{
		return new ServiceRequest(h.getServiceType(), data, offset, h.getTotalLength()
			- h.getStructLength());
	}
	
	/**
	 * Internal use only.
	 * <p>
	 * Creates a new service request using the <code>data</code> byte array and
	 * information from the KNXnet/IP header, but leaves out the cEMI part.<br>
	 * This helper will not try to create the cEMI structure contained in the data part,
	 * i.e. the returned service request is incomplete and {@link ServiceRequest#getCEMI()}
	 * returns <code>null</code>. The service request must not be used for the creation
	 * of KNXnet/IP packets.
	 * 
	 * @param h KNXnet/IP header associated with <code>data</code>
	 * @param data byte array containing the data following the KNXnet/IP header in the
	 *        message structure
	 * @param offset offset into <code>data</code> pointing at the begin of usable data
	 * @return the new empty service request
	 * @throws KNXFormatException if the data buffer is too short for the request or
	 *         unsupported connection header structure
	 */
	public static ServiceRequest getEmptyServiceRequest(KNXnetIPHeader h, byte[] data,
		int offset) throws KNXFormatException
	{
		return new ServiceRequest(h.getServiceType(), data, offset, h.getTotalLength()
			- h.getStructLength(), null);
	}
}
