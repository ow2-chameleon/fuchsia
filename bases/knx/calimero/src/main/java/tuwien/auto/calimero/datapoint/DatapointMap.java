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

package tuwien.auto.calimero.datapoint;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.exception.KNXIllegalArgumentException;
import tuwien.auto.calimero.xml.Element;
import tuwien.auto.calimero.xml.KNXMLException;
import tuwien.auto.calimero.xml.XMLReader;
import tuwien.auto.calimero.xml.XMLWriter;

/**
 * A datapoint model storing datapoints with no defined order or hierarchy using a map
 * implementation.
 * <p>
 * 
 * @author B. Malinowsky
 */
public class DatapointMap implements DatapointModel
{
	private static final String TAG_DATAPOINTS = "datapoints";

	private final Map points;

	/**
	 * Creates a new empty datapoint map.
	 * <p>
	 */
	public DatapointMap()
	{
		points = Collections.synchronizedMap(new HashMap(20));
	}

	/**
	 * Creates a new datapoint map and adds all <code>datapoints</code> to the map.
	 * <p>
	 * A datapoint to be added has to be unique according its main address, the attempt to
	 * add two datapoints using the same main address results in a
	 * KNXIllegalArgumentException.
	 * 
	 * @param datapoints collection with entries of type {@link Datapoint}
	 * @throws KNXIllegalArgumentException on duplicate datapoint
	 */
	public DatapointMap(Collection datapoints)
	{
		// not all HashSets put additional capacity in HashSet(Collection) ctor
		final Map m = new HashMap(Math.max(2 * datapoints.size(), 11));
		for (final Iterator i = datapoints.iterator(); i.hasNext();) {
			final Datapoint dp = (Datapoint) i.next();
			if (m.containsKey(dp.getMainAddress()))
				throw new KNXIllegalArgumentException("duplicate datapoint "
					+ dp.getMainAddress());
			m.put(dp.getMainAddress(), dp);
		}
		points = Collections.synchronizedMap(m);
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.datapoint.DatapointModel#add
	 * (tuwien.auto.calimero.datapoint.Datapoint)
	 */
	public void add(Datapoint dp)
	{
		synchronized (points) {
			if (points.containsKey(dp.getMainAddress()))
				throw new KNXIllegalArgumentException("duplicate datapoint "
					+ dp.getMainAddress());
			points.put(dp.getMainAddress(), dp);
		}
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.datapoint.DatapointModel#remove
	 * (tuwien.auto.calimero.datapoint.Datapoint)
	 */
	public void remove(Datapoint dp)
	{
		points.remove(dp.getMainAddress());
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.datapoint.DatapointModel#removeAll()
	 */
	public void removeAll()
	{
		points.clear();
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.datapoint.DatapointModel#get
	 * (tuwien.auto.calimero.GroupAddress)
	 */
	public Datapoint get(GroupAddress main)
	{
		return (Datapoint) points.get(main);
	}

	// ??? make this a super type interface method
	/**
	 * Returns all datapoints currently contained in this map.
	 * <p>
	 * 
	 * @return unmodifiable collection with entries of type {@link Datapoint}
	 */
	public Collection getDatapoints()
	{
		return Collections.unmodifiableCollection(points.values());
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.datapoint.DatapointModel#contains
	 * (tuwien.auto.calimero.GroupAddress)
	 */
	public boolean contains(GroupAddress main)
	{
		return points.containsKey(main);
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.datapoint.DatapointModel#contains
	 * (tuwien.auto.calimero.datapoint.Datapoint)
	 */
	public boolean contains(Datapoint dp)
	{
		return points.containsKey(dp.getMainAddress());
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.datapoint.DatapointModel#load
	 * (tuwien.auto.calimero.xml.XMLReader)
	 */
	public void load(XMLReader r) throws KNXMLException
	{
		if (r.getPosition() != XMLReader.START_TAG)
			r.read();
		final Element e = r.getCurrent();
		if (r.getPosition() != XMLReader.START_TAG || !e.getName().equals(TAG_DATAPOINTS))
			throw new KNXMLException(TAG_DATAPOINTS + " element not found", e != null ? e
				.getName() : null, r.getLineNumber());
		synchronized (points) {
			while (r.read() == XMLReader.START_TAG) {
				final Datapoint dp = Datapoint.create(r);
				if (points.containsKey(dp.getMainAddress()))
					throw new KNXMLException(TAG_DATAPOINTS + " element contains "
						+ "duplicate datapoint", dp.getMainAddress().toString(), r
						.getLineNumber());
				points.put(dp.getMainAddress(), dp);
			}
		}
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.datapoint.DatapointModel#save
	 * (tuwien.auto.calimero.xml.XMLWriter)
	 */
	public void save(XMLWriter w) throws KNXMLException
	{
		w.writeElement(TAG_DATAPOINTS, Collections.EMPTY_LIST, null);
		synchronized (points) {
			for (final Iterator i = points.values().iterator(); i.hasNext();)
				((Datapoint) i.next()).save(w);
		}
		w.endElement();
	}
}
