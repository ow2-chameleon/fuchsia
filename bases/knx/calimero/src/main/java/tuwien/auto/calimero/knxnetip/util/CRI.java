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

import java.io.ByteArrayOutputStream;

import tuwien.auto.calimero.DataUnitBuilder;
import tuwien.auto.calimero.exception.KNXFormatException;
import tuwien.auto.calimero.exception.KNXIllegalArgumentException;
import tuwien.auto.calimero.knxnetip.KNXnetIPDevMgmt;
import tuwien.auto.calimero.knxnetip.KNXnetIPTunnel;
import tuwien.auto.calimero.log.LogManager;
import tuwien.auto.calimero.log.LogService;

/**
 * Package private container class acting as common base for connection request
 * information (CRI) and connection response data (CRD).<br>
 * It represents either a CRI or a CRD structure.
 */
class CRBase
{
	private static final LogService logger =
		LogManager.getManager().getLogService("KNXnet/IP service");

	byte[] opt;
	private final short connType;
	private final short length;

	/**
	 * Creates a new CR out of a byte array.
	 * <p>
	 * 
	 * @param data byte array containing a CRI or CRD structure
	 * @param offset start offset
	 * @throws KNXFormatException on invalid structure
	 */
	CRBase(byte[] data, int offset) throws KNXFormatException
	{
		int i = offset;
		length = (short) (data[i++] & 0xFF);
		connType = (short) (data[i++] & 0xFF);
		if (length > data.length - offset)
			throw new KNXFormatException("structure length bigger than buffer", length);
		opt = new byte[length - 2];
		for (int k = 0; k < length - 2; ++i, ++k)
			opt[k] = data[i];
	}

	/**
	 * Creates a new CR for the given connection type.
	 * <p>
	 * The array of <code>optionalData</code> is not copied for internal storage. No
	 * additional checks regarding content are done.
	 * 
	 * @param connectionType connection type the CR is used for
	 * @param optionalData byte array containing optional host protocol independent and
	 *        dependent data, this information is located starting at offset 2 in the CR
	 *        structure, <code>optionalData.length</code> < 254
	 */
	CRBase(short connectionType, byte[] optionalData)
	{
		if (connectionType < 0 || connectionType > 0xff)
			throw new KNXIllegalArgumentException("connection type out of range [0..255]");
		length = (short) (2 + optionalData.length);
		if (length > 0xff)
			throw new KNXIllegalArgumentException("optional data exceeds maximum length");
		connType = connectionType;
		opt = optionalData;
	}

	// returns a CRI or CRD depending on request
	static CRBase create(boolean request, byte[] data, int offset)
		throws KNXFormatException
	{
		if (data.length - offset < 2)
			throw new KNXFormatException("buffer too short for "
				+ (request ? "CRI" : "CRD"));
		final int type = data[offset + 1] & 0xff;
		if (type == KNXnetIPTunnel.TUNNEL_CONNECTION)
			return request ? (CRBase) new TunnelCRI(data, offset) : new TunnelCRD(data,
				offset);
		if (type != KNXnetIPDevMgmt.DEVICE_MGMT_CONNECTION)
			logger.warn("can't deduce specific CR from connection type - create default");
		return request ? (CRBase) new CRI(data, offset) : new CRD(data, offset);
	}

	// returns a CRI or CRD depending on request
	static CRBase create(boolean request, short type, byte[] data)
	{
		final byte[] opt = data != null ? data : new byte[0];
		if (type == KNXnetIPTunnel.TUNNEL_CONNECTION)
			return request ? (CRBase) new TunnelCRI(opt) : new TunnelCRD(opt);
		if (type != KNXnetIPDevMgmt.DEVICE_MGMT_CONNECTION)
			logger.warn("can't deduce specific CR from connection type - create default");
		return request ? (CRBase) new CRI(type, (byte[]) opt.clone()) : new CRD(type,
			(byte[]) opt.clone());
	}

	/**
	 * Returns the used connection type code.
	 * <p>
	 * 
	 * @return connection type as unsigned byte
	 */
	public final short getConnectionType()
	{
		return connType;
	}

	/**
	 * Returns a copy of the optional data field.
	 * <p>
	 * Optional data starts at offset 2 in the CR structure.
	 * 
	 * @return byte array with optional data
	 */
	public final byte[] getOptionalData()
	{
		return (byte[]) opt.clone();
	}

	/**
	 * Returns the structure length of this CR in bytes.
	 * <p>
	 * 
	 * @return structure length as unsigned byte
	 */
	public final short getStructLength()
	{
		return length;
	}

	/**
	 * Returns a textual representation of the connection type, length and optional data.
	 * <p>
	 * 
	 * @return a string representation of this object
	 */
	public String toString()
	{
		return "connection type " + connType + " length " + length + " data "
			+ (opt.length == 0 ? "-" : DataUnitBuilder.toHex(opt, " "));
	}
	
	/**
	 * Returns the byte representation of the whole CR structure.
	 * <p>
	 * 
	 * @return byte array containing structure
	 */
	public byte[] toByteArray()
	{
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(length);
		os.write(connType);
		os.write(opt, 0, opt.length);
		return os.toByteArray();
	}
}

/**
 * Immutable container for a connection request information (CRI).
 * <p>
 * The CRI structure is used for the additional information in a connection request.<br>
 * It is built up of host protocol independent data and host protocol dependent data, both
 * optional. Refer to the available subtypes for more specific type information.
 * <p>
 * For now, a plain CRI is returned for management connections, since this connection type
 * doesn't require any additional host protocol data.
 * <p>
 * Factory methods are provided for creation of CRI objects.
 * 
 * @author B. Malinowsky
 * @see tuwien.auto.calimero.knxnetip.servicetype.ConnectRequest
 */
public class CRI extends CRBase
{
	/**
	 * Creates a new CRI out of a byte array.
	 * <p>
	 * 
	 * @param data byte array containing a CRI structure
	 * @param offset start offset of CRI in <code>data</code>
	 * @throws KNXFormatException if no CRI found or invalid structure
	 */
	protected CRI(byte[] data, int offset) throws KNXFormatException
	{
		super(data, offset);
	}

	/**
	 * Creates a new CRI for the given connection type.
	 * <p>
	 * The array of <code>optionalData</code> is not copied for internal storage. No
	 * additional checks regarding content are done.
	 * 
	 * @param connectionType connection type the CRI is used for (e.g. tunnel connection)
	 * @param optionalData byte array containing optional host protocol independent and
	 *        dependent data, this information is located starting at offset 2 in the CRI
	 *        structure, <code>optionalData.length</code> < 254
	 */
	protected CRI(short connectionType, byte[] optionalData)
	{
		super(connectionType, optionalData);
	}

	/**
	 * Creates a new CRI out of a byte array.
	 * <p>
	 * If possible, a matching, more specific, CRI subtype is returned. Note, that CRIs
	 * for specific communication types might expect certain characteristics on
	 * <code>data</code> (regarding contained data).<br>
	 * 
	 * @param data byte array containing the CRI structure
	 * @param offset start offset of CRI in <code>data</code>
	 * @return the new CRI object
	 * @throws KNXFormatException if no CRI found or invalid structure
	 */
	public static CRI createRequest(byte[] data, int offset) throws KNXFormatException
	{
		return (CRI) create(true, data, offset);
	}

	/**
	 * Creates a CRI for the given connection type.
	 * <p>
	 * If possible, a matching, more specific, CRI subtype is returned. Note, that CRIs
	 * for specific communication types might expect certain characteristics on
	 * <code>optionalData</code> (regarding length and/or content).<br>
	 * 
	 * @param connectionType connection type this CRI is used for (e.g. tunnel connection)
	 * @param optionalData byte array containing optional host protocol independent and
	 *        dependent data, this information is located starting at offset 2 in the CRI
	 *        structure, <code>optionalData.length</code> < 254, may be
	 *        <code>null</code> for no optional data
	 * @return the new CRI object
	 */
	public static CRI createRequest(short connectionType, byte[] optionalData)
	{
		return (CRI) create(true, connectionType, optionalData);
	}
}
