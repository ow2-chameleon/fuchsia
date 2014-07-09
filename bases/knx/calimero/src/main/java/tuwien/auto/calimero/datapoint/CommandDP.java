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
import tuwien.auto.calimero.xml.KNXMLException;
import tuwien.auto.calimero.xml.XMLReader;
import tuwien.auto.calimero.xml.XMLWriter;

/**
 * Represents a command based KNX datapoint.
 * <p>
 * Interaction of command based datapoints is done solely with events (i.e. "commands"),
 * not telling anything about a datapoint state.
 * 
 * @author B. Malinowsky
 */
public class CommandDP extends Datapoint
{
	/**
	 * Creates a new command based datapoint with a name.
	 * <p>
	 * 
	 * @param main the group address used to identify this datapoint
	 * @param name user defined datapoint name
	 */
	public CommandDP(GroupAddress main, String name)
	{
		super(main, name, false);
	}

	/**
	 * Creates a new command based datapoint with a name, and specifies datapoint
	 * translation type.
	 * <p>
	 * 
	 * @param main the group address used to identify this datapoint
	 * @param name user defined datapoint name
	 * @param mainNumber main number of the data type used for translation of a datapoint
	 *        value; if the used <code>dptID</code> argument unambiguously identifies a
	 *        DPT translator, main number might be left 0
	 * @param dptID the datapoint type ID used for translation in a DPT translator
	 */
	public CommandDP(GroupAddress main, String name, int mainNumber, String dptID)
	{
		super(main, name, false);
		setDPT(mainNumber, dptID);
	}

	/**
	 * Creates a new command based datapoint from XML input.
	 * <p>
	 * If the current XML element position is no start tag, the next element tag is read.
	 * The datapoint element is then expected to be the current element in the reader.
	 * 
	 * @param r a XML reader
	 * @throws KNXMLException if the XML element is no datapoint or could not be read
	 *         correctly
	 */
	public CommandDP(XMLReader r) throws KNXMLException
	{
		super(r);
		if (isStateBased())
			throw new KNXMLException("no command based KNX datapoint element", null, r
				.getLineNumber());
		doLoad(r);
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.datapoint.Datapoint#toString()
	 */
	public String toString()
	{
		return "command DP " + super.toString();
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.datapoint.Datapoint#doSave(
	 * tuwien.auto.calimero.xml.XMLWriter)
	 */
	void doSave(XMLWriter w)
	{}
}
