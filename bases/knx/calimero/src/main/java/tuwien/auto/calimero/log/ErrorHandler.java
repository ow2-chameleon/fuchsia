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

package tuwien.auto.calimero.log;

/**
 * An error handler is invoked on problems of a log writer which are not handled by the
 * writer itself.
 * <p>
 * Such problems shouldn't get logged using another log writer, to prevent the possibility
 * of a fault / failure chain, and shouldn't lead to disruption of the logging system at a
 * whole.<br>
 * Moreover, log writer errors most certainly can't be returned back to the owner of the
 * writer responsible for handling the errors. And handling errors directly in log
 * services can rarely be done in a satisfying way.<br>
 * So the associated error handler is used for such error events.<br>
 * The handler itself might be adapted to the application needs. For example, an
 * overridden {@link #error(LogWriter, String, Exception)} might close the log writer the
 * error originated from.
 * 
 * @author B. Malinowsky
 * @see LogWriter
 */
public class ErrorHandler
{
	/**
	 * Sets the maximum number {@link #error(LogWriter, String, Exception)} should handle
	 * error events.
	 * <p>
	 * The number should be set reasonably low to prevent a continued handling of repeated
	 * errors of the same source.<br>
	 * Initialized to 5 by default.
	 */
	protected int maxInvocations = 5;

	/**
	 * Counter how often error() was called (without abort).
	 */
	protected int invocations;

	/**
	 * Creates a new error handler.
	 */
	public ErrorHandler()
	{}

	/**
	 * Invoked on a log writer error event.
	 * <p>
	 * The default behavior used here prints the logging source and the error message to
	 * the system standard error stream (System.err). If an exception object is given, the
	 * exception and the last method calls of the log writer leading to the error are also
	 * printed.<br>
	 * Only predefined maximum invocations are handled, subsequent calls are ignored.
	 * 
	 * @param source the log writer the error originated from
	 * @param msg message describing the error
	 * @param e exception related to the error, may be <code>null</code>
	 */
	public synchronized void error(LogWriter source, String msg, Exception e)
	{
		if (invocations >= maxInvocations)
			return;
		++invocations;

		String out = source + ": " + msg;
		StackTraceElement[] trace = null;
		if (e != null) {
			out += " (" + e.getMessage() + ")";
			trace = e.getStackTrace();
		}
		synchronized (System.err) {
			System.err.println(out);
			final String srcName = source.getClass().getName();
			if (trace != null)
				for (int i = 0; i < trace.length; ++i) {
					System.err.println("\t- " + trace[i]);
					if (trace[i].getClassName().equals(srcName))
						break;
				}
		}
	}
}
