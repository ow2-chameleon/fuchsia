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

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.exception.KNXIllegalArgumentException;
import tuwien.auto.calimero.xml.KNXMLException;
import tuwien.auto.calimero.xml.XMLReader;
import tuwien.auto.calimero.xml.XMLWriter;

/**
 * A container for keeping {@link Datapoint}s, using some particular hierarchical
 * structure or order between those datapoints.
 * <p>
 * Its purpose is to imitate a real world datapoint layout, to build and maintain some
 * kind of model for it, allowing a user to create and work with an arrangement of
 * datapoints being adequate for the application requirements.<br>
 * This interface aims to act as the base for building more complex layouts, for example
 * to model part of a KNX network.
 * 
 * @author B. Malinowsky
 * @see DatapointMap
 */
public interface DatapointModel
{
	/**
	 * Adds a datapoint to this model.
	 * <p>
	 * An implementation might throw KNXIllegalArgumentException if tried to add a
	 * duplicate datapoint.
	 * 
	 * @param dp datapoint to add
	 * @throws KNXIllegalArgumentException on duplicate datapoint
	 */
	void add(Datapoint dp);

	/**
	 * Removes the specified datapoint from this model, if that datapoint is found.
	 * <p>
	 * 
	 * @param dp datapoint to remove
	 */
	void remove(Datapoint dp);

	/**
	 * Removes all datapoints contained in this model.
	 * <p>
	 */
	void removeAll();

	/**
	 * Returns the datapoint identified using the specified <code>main</code> address,
	 * if found in this model.
	 * <p>
	 * If no datapoint is found with that address, <code>null</code> is returned.
	 * 
	 * @param main KNX group address to get the datapoint for
	 * @return the datapoint or <code>null</code>
	 */
	Datapoint get(GroupAddress main);

	/**
	 * Checks whether this model contains the datapoint identified using the specified
	 * <code>main</code> address.
	 * <p>
	 * 
	 * @param main KNX group address to look up
	 * @return <code>true</code> iff such datapoint is found, <code>false</code>
	 *         otherwise
	 */
	boolean contains(GroupAddress main);

	/**
	 * Checks whether this model contains the specified datapoint.
	 * <p>
	 * 
	 * @param dp datapoint to look up
	 * @return <code>true</code> iff such datapoint is found, <code>false</code>
	 *         otherwise
	 */
	boolean contains(Datapoint dp);

	/**
	 * Loads a datapoint model from XML input into this model.
	 * <p>
	 * Datapoints already contained in this model are not required to be removed before
	 * loading, the loaded model will be added to the already existing datapoints.
	 * 
	 * @param r a XML reader
	 * @throws KNXMLException on error loading the datapoint model, or on duplicate loaded
	 *         datapoint
	 */
	void load(XMLReader r) throws KNXMLException;

	/**
	 * Saves the datapoint model to XML using the supplied writer.
	 * <p>
	 * 
	 * @param w a XML writer
	 * @throws KNXMLException on error saving the datapoint model
	 */
	void save(XMLWriter w) throws KNXMLException;
}
