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

package tuwien.auto.calimero;

import tuwien.auto.calimero.exception.KNXFormatException;
import tuwien.auto.calimero.exception.KNXIllegalArgumentException;
import tuwien.auto.calimero.xml.KNXMLException;
import tuwien.auto.calimero.xml.XMLReader;

/**
 * Represents an immutable KNX individual address.
 * <p>
 * An individual address is built up from 3 levels: area.line.device (4.4.8 bits) with
 * separator of type '.' or '/'.<br>
 * The sometimes used term "zone" is synonymous with "area".
 */
public class IndividualAddress extends KNXAddress
{
	static final String ATTR_IND = "individual";

	/**
	 * Creates a KNX individual address from a 16 Bit address value.
	 * <p>
	 * 
	 * @param address the address value in the range 0 <= value <= 0xFFFF
	 */
	public IndividualAddress(int address)
	{
		super(address);
	}

	/**
	 * Creates a KNX individual address from the 3-level notation area-, line- and
	 * device-address.
	 * <p>
	 * 
	 * @param area area address value, in the range 0 <= value <= 0xF
	 * @param line line address value, in the range 0 <= value <= 0xF
	 * @param device device address value, in the range 0 <= value <= 0xFF
	 */
	public IndividualAddress(int area, int line, int device)
	{
		init(area, line, device);
	}

	/**
	 * Creates a KNX individual address from a byte array value.
	 * <p>
	 * The address is read out of the first 2 byte fields, while the address array itself
	 * might be longer. The content of <code>address</code> is not modified.
	 * 
	 * @param address the address byte array in big-endian format, with address.length > 1
	 */
	public IndividualAddress(byte[] address)
	{
		super(address);
	}

	/**
	 * Creates a KNX individual address from a string <code>address</code>
	 * representation.
	 * <p>
	 * The address consists of 3 levels (see class header specification). Allowed
	 * separators are '.' or '/', mutually exclusive.
	 * 
	 * @param address string containing the KNX address
	 * @throws KNXFormatException on unknown address type, wrong address syntax, address
	 *         values out of range, or wrong separator used
	 */
	public IndividualAddress(String address) throws KNXFormatException
	{
		final String[] tokens = parse(address);
		if (tokens.length != 3)
			throw new KNXFormatException("wrong individual address syntax with "
				+ tokens.length + " levels", address);
		try {
			init(Byte.parseByte(tokens[0]), Byte.parseByte(tokens[1]), Short
				.parseShort(tokens[2]));
		}
		catch (final KNXIllegalArgumentException e) {
			throw new KNXFormatException(e.getMessage());
		}
	}

	/**
	 * Creates a KNX individual address from xml input.
	 * <p>
	 * If the current XML element position is no start tag, the next element tag is read.
	 * The KNX address element is then expected to be the current element in the reader.
	 * 
	 * @param r a XML reader
	 * @throws KNXMLException if the XML element is no KNXAddress or the address couldn't
	 *         be read in correctly
	 */
	public IndividualAddress(XMLReader r) throws KNXMLException
	{
		super(r);
	}

	/**
	 * Returns the area address.
	 * <p>
	 * The area address consists of the 4 most significant Bits in the address field.
	 * 
	 * @return the area value (high nibble of the address high byte)
	 */
	public final byte getArea()
	{
		return (byte) (address >>> 12);
	}

	/**
	 * Returns the line address.
	 * <p>
	 * The line address consists of 4 bits, starting with bit 8 to 11 in the address
	 * field.
	 * 
	 * @return the line value (low nibble of the address high byte)
	 */
	public final byte getLine()
	{
		return (byte) (address >>> 8 & 0x0F);
	}

	/**
	 * Returns the device address.
	 * <p>
	 * The device address consists of the low byte of the 16 Bit address field.
	 * 
	 * @return the device value (8 least significant bits)
	 */
	public final short getDevice()
	{
		return (short) (address & 0xFF);
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.KNXAddress#getType()
	 */
	public String getType()
	{
		return ATTR_IND;
	}

	/**
	 * Returns the address as a string using the 3-level "area.line.device" notation.
	 * <p>
	 * 
	 * @return the address string
	 */
	public String toString()
	{
		return getArea() + "." + getLine() + "." + getDevice();
	}

	/**
	 * Returns whether <code>obj</code> is equal to this KNX address type.
	 * <p>
	 * 
	 * @param obj knx address object
	 * @return <code>true</code> iff <code>obj</code> is of this type and contains the
	 *         same address, <code>false</code> otherwise
	 */
	public boolean equals(Object obj)
	{
		if (obj instanceof IndividualAddress)
			return address == ((IndividualAddress) obj).address;
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		// offset to distinguish between group address
		final int offset = 0x10000;
		return offset ^ address;
	}

	private void init(int area, int line, int device)
	{
		if ((area & ~0xF) != 0 || (line & ~0xF) != 0 || (device & ~0xFF) != 0)
			throw new KNXIllegalArgumentException("address value out of range");
		address = area << 12 | line << 8 | device;
	}
}
