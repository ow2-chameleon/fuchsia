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

import tuwien.auto.calimero.exception.KNXFormatException;

/**
 * Immutable container for connection response data (CRD).
 * <p>
 * The CRD structure is returned by a connection response, and contains information
 * regarding the established communication.<br>
 * It contains description data of a specific connection type. A CRD is built up of host
 * protocol independent data and host protocol dependent data, both optional. Refer to the
 * available subtypes for more specific type information.
 * <p>
 * For now, a plain CRD is returned for management connections, since this connection type
 * doesn't require any additional host protocol data.
 * <p>
 * Factory methods are provided for creation of CRD objects.
 * 
 * @author B. Malinowsky
 * @see tuwien.auto.calimero.knxnetip.servicetype.ConnectResponse
 */
public class CRD extends CRBase
{
	/**
	 * Creates a new CRD out of a byte array.
	 * <p>
	 * 
	 * @param data byte array containing a CRD structure
	 * @param offset start offset of CRD in <code>data</code>
	 * @throws KNXFormatException if no CRD found or invalid structure
	 */
	public CRD(byte[] data, int offset) throws KNXFormatException
	{
		super(data, offset);
	}

	/**
	 * Creates a new CRD for the given connection type.
	 * <p>
	 * The array of <code>optionalData</code> is not copied for internal storage. No
	 * additional checks regarding content are done.
	 * 
	 * @param connectionType connection type the description is intended for
	 * @param optionalData byte array containing optional host protocol independent and
	 *        dependent data, this information is located starting at offset 2 in the CRD
	 *        structure, <code>optionalData.length</code> < 254
	 */
	public CRD(short connectionType, byte[] optionalData)
	{
		super(connectionType, optionalData);
	}

	/**
	 * Creates a new CRD out of a byte array.
	 * <p>
	 * If possible, a matching, more specific, CRD subtype is returned. Note, that CRD for
	 * specific communication types might expect certain characteristics on
	 * <code>data</code> (regarding contained data).<br>
	 * 
	 * @param data byte array containing the CRD structure
	 * @param offset start offset of CRD in <code>data</code>
	 * @return the new CRD object
	 * @throws KNXFormatException if no CRD found or invalid structure
	 */
	public static CRD createResponse(byte[] data, int offset) throws KNXFormatException
	{
		return (CRD) create(false, data, offset);
	}

	/**
	 * Creates a CRD for the given connection type.
	 * <p>
	 * If possible, a matching, more specific, CRD subtype is returned. Note, that CRD for
	 * specific communication types might expect certain characteristics on
	 * <code>optionalData</code> (regarding length and/or content).<br>
	 * 
	 * @param connectionType connection type this CRD is used for (e.g. tunnel connection)
	 * @param optionalData byte array containing optional host protocol independent and
	 *        dependent data, this information is located starting at offset 2 in the CRD
	 *        structure, <code>optionalData.length</code> < 254, may be
	 *        <code>null</code> for no optional data
	 * @return the new CRD object
	 */
	public static CRD createResponse(short connectionType, byte[] optionalData)
	{
		return (CRD) create(false, connectionType, optionalData);
	}
}
