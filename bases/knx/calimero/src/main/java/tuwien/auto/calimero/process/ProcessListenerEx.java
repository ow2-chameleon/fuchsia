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
 
package tuwien.auto.calimero.process;

import tuwien.auto.calimero.dptxlator.DPTXlator;
import tuwien.auto.calimero.dptxlator.DPTXlator2ByteFloat;
import tuwien.auto.calimero.dptxlator.DPTXlator3BitControlled;
import tuwien.auto.calimero.dptxlator.DPTXlator8BitUnsigned;
import tuwien.auto.calimero.dptxlator.DPTXlatorBoolean;
import tuwien.auto.calimero.dptxlator.DPTXlatorString;
import tuwien.auto.calimero.dptxlator.TranslatorTypes;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.exception.KNXFormatException;

/**
 * Extended process listener interface with additional group read event handler methods,
 * as well as basic ASDU type translation capabilities.
 * <p>
 * This listener contains predefined methods equal to the ones in the ProcessCommunicator
 * interface to convert received ASDUs into common java data types.<br>
 * Usage example for group reads from datapoints with DPT main number 1:<br>
 * 
 * <pre>
 * public void groupWrite(ProcessEvent e)
 * {
 *     // the member variable model (of type DatapointModel, declared by the user)
 *     // contains datapoints of light switch objects in the KNX network (boolean type)
 *     if (model.contains(e.getDestination()))
 *         try {
 *             final boolean switch = asBool(e);
 *             System.out.println(model.get(e.getDestination()).getName()
 *                 + &quot; state = &quot; + switch);
 *             if (switch) {
 *                 // light switch is in position on
 *                 // do some visual feedback for the user ...
 *             }
 *         }
 *         catch (KNXFormatException kfe) { /* DPT not found ... *&lt;!-- --&gt;/ }
 * }
 * </pre>
 * 
 * @author B. Malinowsky
 * @see ProcessCommunicator
 */
public abstract class ProcessListenerEx implements ProcessListener
{
	/**
	 * Indicates that a KNX group read request message was received from the KNX network.
	 * <p>
	 * 
	 * @param e process event object
	 */
	public abstract void groupReadRequest(ProcessEvent e);

	/**
	 * Indicates that a KNX group read response message was received from the KNX network.
	 * <p>
	 * 
	 * @param e process event object
	 */
	public abstract void groupReadResponse(ProcessEvent e);

	/**
	 * Returns the ASDU of the received process event as boolean datapoint value.
	 * <p>
	 * This method has to be invoked manually by the user (either in
	 * {@link #groupReadResponse(ProcessEvent)} or
	 * {@link ProcessListener#groupWrite(ProcessEvent)}), depending on the received
	 * datapoint type.
	 * 
	 * @param e the process event with the ASDU to translate
	 * @return the received value of type boolean
	 * @throws KNXFormatException on not supported or not available boolean DPT
	 */
	public boolean asBool(ProcessEvent e) throws KNXFormatException
	{
		final DPTXlatorBoolean t = new DPTXlatorBoolean(DPTXlatorBoolean.DPT_BOOL);
		t.setData(e.getASDU());
		return t.getValueBoolean();
	}

	/**
	 * Returns the ASDU of the received process event as unsigned 8 Bit datapoint value.
	 * <p>
	 * This method has to be invoked manually by the user (either in
	 * {@link #groupReadResponse(ProcessEvent)} or
	 * {@link ProcessListener#groupWrite(ProcessEvent)}), depending on the received
	 * datapoint type.
	 * 
	 * @param e the process event with the ASDU to translate
	 * @param scale see {@link ProcessCommunicator#readUnsigned(
	 *        tuwien.auto.calimero.GroupAddress, String)}
	 * @return the received value of type 8 Bit unsigned
	 * @throws KNXFormatException on not supported or not available 8 Bit unsigned DPT
	 */
	public short asUnsigned(ProcessEvent e, String scale) throws KNXFormatException
	{
		final DPTXlator8BitUnsigned t = new DPTXlator8BitUnsigned(scale);
		t.setData(e.getASDU());
		return t.getValueUnsigned();
	}

	/**
	 * Returns the ASDU of the received process event as 3 Bit controlled datapoint value.
	 * <p>
	 * This method has to be invoked manually by the user (either in
	 * {@link #groupReadResponse(ProcessEvent)} or
	 * {@link ProcessListener#groupWrite(ProcessEvent)}), depending on the received
	 * datapoint type.
	 * 
	 * @param e the process event with the ASDU to translate
	 * @return the received value of type 3 Bit controlled
	 * @throws KNXFormatException on not supported or not available 3 Bit controlled DPT
	 */
	public byte asControl(ProcessEvent e) throws KNXFormatException
	{
		final DPTXlator3BitControlled t = new DPTXlator3BitControlled(
			DPTXlator3BitControlled.DPT_CONTROL_DIMMING);
		t.setData(e.getASDU());
		return t.getValueSigned();
	}

	/**
	 * Returns the ASDU of the received process event as 2 byte KNX float datapoint value.
	 * <p>
	 * This method has to be invoked manually by the user (either in
	 * {@link #groupReadResponse(ProcessEvent)} or
	 * {@link ProcessListener#groupWrite(ProcessEvent)}), depending on the received
	 * datapoint type.
	 * 
	 * @param e the process event with the ASDU to translate
	 * @return the received value of type float
	 * @throws KNXFormatException on not supported or not available float DPT
	 */
	public float asFloat(ProcessEvent e) throws KNXFormatException
	{
		final DPTXlator2ByteFloat t = new DPTXlator2ByteFloat(
			DPTXlator2ByteFloat.DPT_TEMPERATURE_DIFFERENCE);
		t.setData(e.getASDU());
		return t.getValueFloat();
	}

	/**
	 * Returns the ASDU of the received process event as string datapoint value.
	 * <p>
	 * The used character set is ISO-8859-1 (Latin 1), with an allowed string length of 14
	 * characters.
	 * <p>
	 * This method has to be invoked manually by the user (either in
	 * {@link #groupReadResponse(ProcessEvent)} or
	 * {@link ProcessListener#groupWrite(ProcessEvent)}), depending on the received
	 * datapoint type.
	 * 
	 * @param e the process event with the ASDU to translate
	 * @return the received value of type String
	 * @throws KNXFormatException on not supported or not available ISO-8859-1 DPT
	 */
	public String asString(ProcessEvent e) throws KNXFormatException
	{
		final DPTXlatorString t = new DPTXlatorString(DPTXlatorString.DPT_STRING_8859_1);
		t.setData(e.getASDU());
		return t.getValue();
	}

	/**
	 * Returns the ASDU of the received process event as datapoint value of the requested
	 * DPT in String representation.
	 * <p>
	 * This method has to be invoked manually by the user (either in
	 * {@link #groupReadResponse(ProcessEvent)} or
	 * {@link ProcessListener#groupWrite(ProcessEvent)}), depending on the received
	 * datapoint type.
	 * 
	 * @param e the process event with the ASDU to translate
	 * @param dptMainNumber datapoint type main number, number >= 0; use 0 to infer
	 *        translator type from <code>dptID</code> argument only
	 * @param dptID datapoint type ID for selecting a particular kind of value translation
	 * @return the received value of the requested type as String representation
	 * @throws KNXException on not supported or not available DPT
	 * @see TranslatorTypes#createTranslator(int, String)
	 */
	public String asString(ProcessEvent e, int dptMainNumber, String dptID)
		throws KNXException
	{
		final DPTXlator t = TranslatorTypes.createTranslator(dptMainNumber, dptID);
		t.setData(e.getASDU());
		return t.getValue();
	}
}
