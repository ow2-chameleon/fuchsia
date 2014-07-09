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
import tuwien.auto.calimero.knxnetip.util.DIB;
import tuwien.auto.calimero.knxnetip.util.DeviceDIB;
import tuwien.auto.calimero.knxnetip.util.ManufacturerDIB;
import tuwien.auto.calimero.knxnetip.util.SuppFamiliesDIB;

/**
 * Represents a description response.
 * <p>
 * Such response is sent by a server in reply to a description request from a client.<br>
 * A response contains a device description information block (DIB) and a supported
 * service families DIB. Optionally, it might contain an additional DIB with other
 * information. Such optional DIB is only paid attention in case of type manufacturer data
 * DIB, otherwise it is ignored.
 * <p>
 * Objects of this type are immutable.
 * 
 * @author B. Malinowsky
 * @author Bernhard Erb
 * @see tuwien.auto.calimero.knxnetip.servicetype.DescriptionRequest
 */
public class DescriptionResponse extends ServiceType
{
	private final DeviceDIB device;
	private final SuppFamiliesDIB suppfam;
	// manufacturer data is optional in the specification
	private ManufacturerDIB mfr;

	/**
	 * Creates a new description response out of a byte array.
	 * <p>
	 * 
	 * @param data byte array containing a description response structure
	 * @param offset start offset of response in <code>data</code>
	 * @throws KNXFormatException if no description response was found or invalid
	 *         structure of DIBs carried in the response
	 */
	public DescriptionResponse(byte[] data, int offset) throws KNXFormatException
	{
		super(KNXnetIPHeader.DESCRIPTION_RES);
		device = new DeviceDIB(data, offset);
		suppfam = new SuppFamiliesDIB(data, offset + device.getStructLength());
		// check out if we have an optional DIB part
		final int len = device.getStructLength() + suppfam.getStructLength();
		if (len + 1 < data.length - offset
			&& (data[offset + len + 1] & 0xFF) == DIB.MFR_DATA)
			mfr = new ManufacturerDIB(data, offset);
	}

	/**
	 * Creates a new description response containing a device DIB and a supported service
	 * families DIB.
	 * <p>
	 * 
	 * @param device device description
	 * @param suppSvcFam supported service families
	 */
	public DescriptionResponse(DeviceDIB device, SuppFamiliesDIB suppSvcFam)
	{
		super(KNXnetIPHeader.DESCRIPTION_RES);
		this.device = device;
		suppfam = suppSvcFam;
	}

	/**
	 * Creates a new description response containing a device DIB, a supported service
	 * families DIB and a manufacturer DIB.
	 * <p>
	 * 
	 * @param device device description
	 * @param suppSvcFam supported service families
	 * @param mfr manufacturer specific data
	 */
	public DescriptionResponse(DeviceDIB device, SuppFamiliesDIB suppSvcFam,
		ManufacturerDIB mfr)
	{
		this(device, suppSvcFam);
		this.mfr = mfr;
	}

	/**
	 * Returns the device description information block contained in the response.
	 * <p>
	 * 
	 * @return a device DIB
	 */
	public final DeviceDIB getDevice()
	{
		return device;
	}

	/**
	 * Returns the supported service families description information block.
	 * <p>
	 * 
	 * @return a DIB with the supported service families
	 */
	public final SuppFamiliesDIB getServiceFamilies()
	{
		return suppfam;
	}

	/**
	 * Returns the manufacturer data description information block optionally contained in
	 * the response.
	 * <p>
	 * The manufacturer data is not a mandatory part of a description response. It is only
	 * available, if the optional DIB information of a response matches this DIB type.<br>
	 * 
	 * @return a manufacturer DIB, or <code>null</code> if no such DIB
	 */
	public final ManufacturerDIB getManufacturerData()
	{
		return mfr;
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.knxnetip.servicetype.ServiceType#getStructLength()
	 */
	short getStructLength()
	{
		int len = device.getStructLength() + suppfam.getStructLength();
		if (mfr != null)
			len += mfr.getStructLength();
		return (short) len;
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.knxnetip.servicetype.ServiceType#toByteArray
	 *      (java.io.ByteArrayOutputStream)
	 */
	byte[] toByteArray(ByteArrayOutputStream os)
	{
		byte[] buf = device.toByteArray();
		os.write(buf, 0, buf.length);
		buf = suppfam.toByteArray();
		os.write(buf, 0, buf.length);
		if (mfr != null) {
			buf = mfr.toByteArray();
			os.write(buf, 0, buf.length);
		}
		return os.toByteArray();
	}
}
