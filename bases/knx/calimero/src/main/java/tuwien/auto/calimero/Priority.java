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

import tuwien.auto.calimero.exception.KNXIllegalArgumentException;

/**
 * Priority of KNX messages for access on a KNX medium.
 * <p>
 * All possible priority values are supplied as immutable Priority constants.
 * 
 * @author B. Malinowsky
 */
public final class Priority
{
	/**
	 * System priority, reserved for high priority management and system configuration.
	 * <p>
	 */
	public static final Priority SYSTEM = new Priority(0x00);

	/**
	 * Urgent priority, for urgent frames.
	 * <p>
	 */
	public static final Priority URGENT = new Priority(0x02);

	/**
	 * Normal priority, the default for short frames.
	 * <p>
	 */
	public static final Priority NORMAL = new Priority(0x01);

	/**
	 * Low priority, used for long frames.
	 * <p>
	 */
	public static final Priority LOW = new Priority(0x03);

	/**
	 * Constant with the 2 Bit representation of this priority used in the message
	 * priority field.
	 * <p>
	 */
	public final byte value;

	private Priority(int v)
	{
		value = (byte) v;
	}

	/**
	 * Returns the priority of the supplied priority value code.
	 * <p>
	 * 
	 * @param value priority value code, 0 &lt;= value &lt;= 3
	 * @return the corresponding priority object
	 */
	public static Priority get(int value)
	{
		if (value == 1)
			return NORMAL;
		if (value == 0)
			return SYSTEM;
		if (value == 3)
			return LOW;
		if (value == 2)
			return URGENT;
		throw new KNXIllegalArgumentException("invalid priority value");
	}

	/**
	 * Returns the priority of the supplied priority value representation.
	 * <p>
	 * The following textual representations for <code>value</code> are allowed:
	 * <ul>
	 * <li>"system"</li>
	 * <li>"normal"</li>
	 * <li>"urgent"</li>
	 * <li>"low"</li>
	 * </ul>
	 * These are in accordance with the ones returned by {@link #toString()}.
	 * 
	 * @param value priority value in textual representation, value is treated case
	 *        insensitive
	 * @return the corresponding priority object
	 */
	public static Priority get(String value)
	{
		if ("system".equalsIgnoreCase(value))
			return SYSTEM;
		if ("normal".equalsIgnoreCase(value))
			return NORMAL;
		if ("urgent".equalsIgnoreCase(value))
			return URGENT;
		if ("low".equalsIgnoreCase(value))
			return LOW;
		throw new KNXIllegalArgumentException("invalid priority value");
	}

	/**
	 * Returns the priority in textual representation.
	 * <p>
	 * <ul>
	 * <li>{@link #SYSTEM} returns "system"</li>
	 * <li>{@link #NORMAL} returns "normal"</li>
	 * <li>{@link #LOW} returns "low"</li>
	 * <li>{@link #URGENT} returns "urgent"</li>
	 * </ul>
	 * 
	 * @return priority as string
	 */
	public String toString()
	{
		return value == 0 ? "system" : value == 1 ? "normal" : value == 2 ? "urgent"
			: "low";
	}
}
