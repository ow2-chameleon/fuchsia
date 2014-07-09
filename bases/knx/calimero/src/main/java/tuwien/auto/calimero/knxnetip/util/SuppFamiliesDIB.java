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

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import tuwien.auto.calimero.exception.KNXFormatException;

/**
 * Supported service families description information block.
 * <p>
 * It informs about the service families supported by the device.<br>
 * <p>
 * Objects of this type are immutable.
 * 
 * @author B. Malinowsky
 * @see tuwien.auto.calimero.knxnetip.servicetype.DescriptionResponse
 */
public class SuppFamiliesDIB extends DIB
{
	private static final String[] familyNames = {
		null, null, "Core", "Device Management", "Tunneling", "Routing",
		"Remote Logging", "Remote Configuration/Diagnosis", "Object Server" };

	private final Map map = new HashMap();

	/**
	 * Creates a supported families DIB out of a byte array.
	 * <p>
	 * 
	 * @param data byte array containing the service families DIB structure
	 * @param offset start offset of DIB in <code>data</code>
	 * @throws KNXFormatException if no DIB found or invalid structure
	 */
	public SuppFamiliesDIB(byte[] data, int offset) throws KNXFormatException
	{
		super(data, offset);
		if (type != SUPP_SVC_FAMILIES)
			throw new KNXFormatException("not a supported service families DIB", type);
		final ByteArrayInputStream is =
			new ByteArrayInputStream(data, offset + 2, data.length - offset - 2);
		for (int i = 2; i < size; i += 2) {
			final short family = (short) is.read();
			map.put(new Short(family), new Short((short) is.read()));
		}
	}

	/**
	 * Returns all supported service families, each family together with the version it is
	 * implemented and supported up to.
	 * <p>
	 * The returned set holds <code>Map.Entry</code> items, with the service family of type
	 * {@link Short} being the key, and the version of type {@link Short} being the value.
	 * 
	 * @return an unmodifiable set containing supported entries (family-version pair)
	 */
	public final Set getFamilies()
	{
		return Collections.unmodifiableSet(map.entrySet());
	}

	/**
	 * Returns the version associated to a given supported service family.
	 * <p>
	 * If the service family is not supported, 0 is returned.
	 * 
	 * @param family supported service family to lookup
	 * @return version as unsigned byte, or 0
	 */
	public final short getVersion(short family)
	{
		return ((Short) map.get(new Short(family))).shortValue();
	}

	/**
	 * Returns the service family name for the supplied family ID.
	 * <p>
	 * 
	 * @param family service family ID to get name for
	 * @return family name as string, or <code>null</code> on no name available
	 */
	public final String getFamilyName(short family)
	{
		return family < familyNames.length ? familyNames[family] : null;
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.knxnetip.util.DIB#toByteArray()
	 */
	public byte[] toByteArray()
	{
		final byte[] buf = super.toByteArray();
		int i = 2;
		for (final Iterator it = map.entrySet().iterator(); it.hasNext();) {
			final Map.Entry e = (Map.Entry) it.next();
			buf[i++] = ((Short) e.getKey()).byteValue();
			buf[i++] = ((Short) e.getValue()).byteValue();
		}
		return buf;
	}

	/**
	 * Returns a textual representation of this supported service families DIB.
	 * <p>
	 * 
	 * @return a string representation of the DIB object
	 */
	public String toString()
	{
		final StringBuffer buf = new StringBuffer();
		for (final Iterator i = map.entrySet().iterator(); i.hasNext();) {
			final Map.Entry entry = (Map.Entry) i.next();
			buf.append(getFamilyName(((Short) entry.getKey()).shortValue()));
			buf.append(" - version ").append(entry.getValue());
			if (i.hasNext())
				buf.append(", ");
		}
		return buf.toString();
	}
}
