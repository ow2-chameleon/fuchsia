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

package tuwien.auto.calimero.buffer;

import tuwien.auto.calimero.KNXAddress;
import tuwien.auto.calimero.buffer.cache.CacheObject;
import tuwien.auto.calimero.cemi.CEMILData;
import tuwien.auto.calimero.exception.KNXIllegalArgumentException;

/**
 * Used for holding {@link CEMILData} frames.
 * <p>
 * 
 * @author B. Malinowsky
 */
public class LDataObject extends CacheObject
{
	/**
	 * Creates a {@link LDataObject} holding the <code>frame</code> argument.
	 * <p>
	 * 
	 * @param frame {@link CEMILData} frame
	 */
	public LDataObject(CEMILData frame)
	{
		super(frame.getDestination(), frame);
	}

	/**
	 * Creates a {@link LDataObject} set to key <code>addrKey</code> and holding
	 * <code>value</code>.
	 * <p>
	 * If <code>value</code> is not of type {@link CEMILData}, the methods
	 * {@link #setFrame(CEMILData)} and {@link #getFrame()} have to be overridden.
	 * 
	 * @param key key of this cache object
	 * @param value value hold by this cache object
	 */
	protected LDataObject(KNXAddress key, Object value)
	{
		super(key, value);
	}

	/**
	 * Sets a new {@link CEMILData} <code>frame</code> for this cache object.
	 * <p>
	 * The key generated out of <code>frame</code> (i.e. out of the KNX address of
	 * <code>frame</code>) has to be equal to the key of this {@link LDataObject},
	 * as returned from {@link #getKey()}. Otherwise a
	 * {@link KNXIllegalArgumentException} will be thrown.<br>
	 * Note that on successful set the timestamp is renewed.
	 * 
	 * @param frame the new {@link CEMILData} frame to set
	 */
	public synchronized void setFrame(CEMILData frame)
	{
		if (!frame.getDestination().equals(getKey()))
			throw new KNXIllegalArgumentException("frame key differs from cache key");
		value = frame;
		resetTimestamp();
	}

	/**
	 * Returns a {@link CEMILData} frame contained in this cache object.
	 * <p>
	 * On no frame available, <code>null</code> is returned.
	 * 
	 * @return the {@link CEMILData} frame, or <code>null</code>
	 */
	public synchronized CEMILData getFrame()
	{
		return (CEMILData) value;
	}
}
