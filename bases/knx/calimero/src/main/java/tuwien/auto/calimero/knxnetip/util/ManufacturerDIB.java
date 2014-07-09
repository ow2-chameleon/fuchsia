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

import tuwien.auto.calimero.DataUnitBuilder;
import tuwien.auto.calimero.exception.KNXFormatException;

/**
 * Represents a manufacturer data description information block.
 * <p>
 * Since the data in this DIB is dependent on the manufacturer and might contain any
 * information, no specific content parsing is done.
 * <p>
 * Objects of this type are immutable.
 * 
 * @author B. Malinowsky
 */
public class ManufacturerDIB extends DIB
{
	private final int id;
	private final byte[] mfrData;

	/**
	 * Creates a manufacturer data DIB out of a byte array.
	 * <p>
	 * 
	 * @param data byte array containing manufacturer data DIB structure
	 * @param offset start offset of DIB in <code>data</code>
	 * @throws KNXFormatException if no DIB found or invalid structure
	 */
	public ManufacturerDIB(byte[] data, int offset) throws KNXFormatException
	{
		super(data, offset);
		if (type != MFR_DATA)
			throw new KNXFormatException("DIB is not of type manufacturer data", type);
		if (size < 4)
			throw new KNXFormatException("MFR DIB too short");
		id = (data[2] & 0xFF) << 8 | data[3] & 0xFF;
		mfrData = new byte[data.length - offset - 4];
		for (int i = 0; i < mfrData.length; ++i)
			mfrData[i] = data[4 + offset + i];
	}

	/**
	 * Returns the KNX manufacturer ID.
	 * <p>
	 * The ID clearly identifies the manufacturer who created this DIB structure.
	 * 
	 * @return ID as unsigned short
	 */
	public final int getID()
	{
		return id;
	}

	/**
	 * Returns the manufacturer specific description data.
	 * <p>
	 * This data block starts at byte offset 4 in the DIB structure.
	 * 
	 * @return byte array with manufacturer data
	 */
	public final byte[] getData()
	{
		return (byte[]) mfrData.clone();
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.knxnetip.util.DIB#toByteArray()
	 */
	public byte[] toByteArray()
	{
		final byte[] buf = super.toByteArray();
		buf[2] = (byte) (id >> 8);
		buf[3] = (byte) id;
		for (int i = 0; i < mfrData.length; ++i)
			buf[4 + i] = mfrData[i];
		return buf;
	}

	/**
	 * Returns a textual representation of this manufacturer DIB.
	 * <p>
	 * 
	 * @return a string representation of the DIB object
	 */
	public String toString()
	{
		return "KNX manufacturer ID 0x" + Integer.toHexString(id) + ", data 0x"
			+ DataUnitBuilder.toHex(mfrData, null);
	}
}
