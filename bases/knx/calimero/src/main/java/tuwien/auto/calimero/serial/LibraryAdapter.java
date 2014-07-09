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
package tuwien.auto.calimero.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import tuwien.auto.calimero.log.LogManager;
import tuwien.auto.calimero.log.LogService;

/**
 * Adapter to access a serial communication port using some serial I/O library.
 * <p>
 * Subtypes of this class implementing the access to a specific library have to declare a
 * public constructor expecting a String and an <code>int</code> argument:<br>
 * <code>Ctor(String portID, int baudrate)</code><br>
 * Invoking this constructor will open the serial port according the supplied arguments.
 * <p>
 * After closing a library adapter, method behavior is undefined.
 * 
 * @author B. Malinowsky
 */
public abstract class LibraryAdapter
{
	/** The same logger as used for the FT1.2 connection. */
	protected static final LogService logger = LogManager.getManager().getLogService(
		"FT1.2");

	/** Creates a new library adapter. */
	protected LibraryAdapter()
	{}

	/**
	 * Returns the output stream for the opened serial communication port.
	 * <p>
	 * Subsequent invocations might return the same or a new stream object.
	 * 
	 * @return OutputStream
	 */
	public abstract OutputStream getOutputStream();

	/**
	 * Returns the input stream for the opened serial communication port.
	 * <p>
	 * Subsequent invocations might return the same or a new stream object.
	 * 
	 * @return InputStream
	 */
	public abstract InputStream getInputStream();

	/**
	 * Sets a new baud rate for this connection.
	 * <p>
	 * 
	 * @param baudrate requested baud rate [Bit/s], 0 &lt; baud rate
	 */
	public void setBaudRate(int baudrate)
	{
		try {
			invoke(this, "setBaudRate", new Object[] { new Integer(baudrate) });
		}
		catch (final Exception e) {}
	}

	/**
	 * Returns the currently used baud rate.
	 * <p>
	 * 
	 * @return baud rate in Bit/s
	 */
	public int getBaudRate()
	{
		try {
			return ((Integer) invoke(this, "getBaudRate", null)).intValue();
		}
		catch (final Exception e) {}
		return 0;
	}

	/**
	 * Closes an open serial port.
	 * <p>
	 * 
	 * @throws IOException on error during close
	 */
	public abstract void close() throws IOException;

	/**
	 * Invokes <code>method</code> name on object <code>obj</code> with arguments
	 * <code>args</code>.
	 * <p>
	 * Arguments wrapped in an object of type Integer are replaced with the primitive int
	 * type when looking up the method name.
	 * 
	 * @param obj object on which to invoke the method
	 * @param method method name
	 * @param args list of arguments
	 * @return the result of the invoked method
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @see Class#getMethod(String, Class[])
	 * @see Method#invoke(Object, Object[])
	 */
	protected Object invoke(Object obj, String method, Object[] args)
		throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
	{
		final Class[] c = new Class[args == null ? 0 : args.length];
		for (int i = 0; i < c.length; ++i) {
			c[i] = args[i].getClass();
			if (c[i] == Integer.class)
				c[i] = int.class;
		}
		try {
			if (obj instanceof Class)
				return ((Class) obj).getMethod(method, c).invoke(null, args);
			return obj.getClass().getMethod(method, c).invoke(obj, args);
		}
		catch (final NoSuchMethodException e) {
			logger.fatal("no such method", e);
			throw e;
		}
		catch (final IllegalArgumentException e) {
			logger.fatal("illegal argument on invoking " + obj.getClass().getName() + "."
				+ method, e);
			throw e;
		}
	}
}
