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

package tuwien.auto.calimero.link.medium;

import java.io.ByteArrayInputStream;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.KNXAddress;
import tuwien.auto.calimero.Priority;
import tuwien.auto.calimero.exception.KNXFormatException;

/**
 * Implementation for raw frames with common used functionality in L-data and L-polldata
 * frames.
 * <p>
 * For now, implementation is only done to accommodate reception of raw frame (contained
 * in byte arrays), not to build raw frames out of information parts required to assemble
 * a new raw frame.
 * <p>
 * Objects of this type are considered immutable.
 * 
 * @author B. Malinowsky
 */
public abstract class RawFrameBase implements RawFrame
{
	/**
	 * Frame type identifier.
	 * <p>
	 */
	protected int type;

	/**
	 * Source address.
	 * <p>
	 */
	protected IndividualAddress src;

	/**
	 * Destination address.
	 */
	protected KNXAddress dst;

	/**
	 * Is this an extended (<code>true</code>) or a standard frame (<code>false</code>).
	 * <p>
	 */
	protected boolean ext;

	/**
	 * Frame repetition flag.
	 * <p>
	 */
	protected boolean repetition;

	/**
	 * Frame priority.
	 * <p>
	 */
	protected Priority p;

	/**
	 * Frame hop count.
	 * <p>
	 */
	protected int hopcount;

	/**
	 * Frame checksum.
	 * <p>
	 */
	protected int fcs;

	/**
	 * Transport layer protocol data unit.
	 * <p>
	 * In L-polldata frame tpdu is not used.
	 */
	protected byte[] tpdu;

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.link.medium.RawFrame#getFrameType()
	 */
	public final int getFrameType()
	{
		return type;
	}

	/**
	 * Returns the KNX individual source address.
	 * <p>
	 * 
	 * @return address of type IndividualAddress
	 */
	public final IndividualAddress getSource()
	{
		return src;
	}

	/**
	 * Returns the KNX destination address.
	 * <p>
	 * 
	 * @return destination address of type KNXAddress
	 */
	public final KNXAddress getDestination()
	{
		return dst;
	}

	/**
	 * Returns the message priority used for this frame.
	 * <p>
	 * 
	 * @return the used Priority
	 */
	public final Priority getPriority()
	{
		return p;
	}

	/**
	 * Returns the hop count of this frame.
	 * <p>
	 * 
	 * @return hop count in the range 0 &lt;= count &lt;= 7
	 */
	public final byte getHopcount()
	{
		return (byte) hopcount;
	}

	/**
	 * Returns whether frame repetition is requested, or this is a repeated frame.
	 * <p>
	 * A request for repetition or repeated frame is indicated with <code>true</code>,
	 * otherwise <code>false</code> is returned.
	 * 
	 * @return repeat state as boolean
	 */
	public final boolean isRepetition()
	{
		return repetition;
	}

	/**
	 * Returns a copy of the TPDU, if available.
	 * <p>
	 * 
	 * @return tpdu as byte array or <code>null</code> for L-polldata frames
	 */
	public final byte[] getTPDU()
	{
		return tpdu == null ? null : (byte[]) tpdu.clone();
	}

	/**
	 * Returns the frame checksum as contained in the frame.
	 * <p>
	 * The returned checksum is taken from the frame "as is", it is not recalculated for
	 * validation nor checked for correctness.<br>
	 * The length and structure of the returned checksum depends on the communication
	 * medium.
	 * 
	 * @return frame checksum
	 */
	public final int getChecksum()
	{
		return fcs;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		final StringBuffer sb = new StringBuffer();
		sb.append(type == LDATA_FRAME ? "L-Data" : "L-Polldata").append(".req ");
		sb.append(ext ? "(ext)" : "(std)");
		sb.append(" from ").append(src).append(" to ").append(dst);
		sb.append(", ").append(p).append(" priority");
		if (repetition)
			sb.append(" repeat");
		sb.append(" fcs 0x").append(Integer.toHexString(fcs));
		return sb.toString();
	}

	void setDestination(int addr, boolean group)
	{
		dst = group ? (KNXAddress) new GroupAddress(addr) : new IndividualAddress(addr);
	}

	int readCtrlEx(ByteArrayInputStream is) throws KNXFormatException
	{
		final int ctrle = is.read();
		if ((ctrle & 0xf) != 0)
			throw new KNXFormatException("LTE-HEE frame not supported");
		return ctrle;
	}
}
