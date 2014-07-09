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

package tuwien.auto.calimero.mgmt;

import tuwien.auto.calimero.CloseEvent;
import tuwien.auto.calimero.FrameEvent;
import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.link.KNXLinkClosedException;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.event.NetworkLinkListener;

/**
 * Property adapter for remote property services.
 * <p>
 * 
 * @author B. Malinowsky
 */
public class RemotePropertyServiceAdapter implements PropertyAdapter
{
	private final ManagementClient mc;
	private final Destination dst;
	private byte[] key;
	private final NetworkLinkListener nll;
	private final PropertyAdapterListener pal;
	
	private final class NLListener implements NetworkLinkListener
	{
		NLListener()
		{}

		public void confirmation(FrameEvent e)
		{}

		public void indication(FrameEvent e)
		{}

		public void linkClosed(CloseEvent e)
		{
			pal.adapterClosed(new CloseEvent(RemotePropertyServiceAdapter.this, e
				.isUserRequest(), e.getReason()));
		}
	}
	
	/**
	 * Creates a new property adapter for remote property access.
	 * 
	 * @param link KNX network link used for communication with the KNX network
	 * @param remote KNX individual address to access its interface objects
	 * @param l property adapter listener to get notified about adapter events, use
	 *        <code>null</code> for no listener
	 * @param connOriented <code>true</code> to use connection oriented mode for access,
	 *        <code>false</code> to use connectionless mode
	 * @throws KNXLinkClosedException if the network link is closed
	 */
	public RemotePropertyServiceAdapter(KNXNetworkLink link, IndividualAddress remote,
		PropertyAdapterListener l, boolean connOriented) throws KNXLinkClosedException
	{
		mc = new ManagementClientImpl(link);
		dst = mc.createDestination(remote, connOriented);
		pal = l;
		nll = pal != null ? new NLListener() : null;
		if (nll != null)
			link.addLinkListener(nll);
		key = null;
	}

	/**
	 * Creates a new property adapter for remote property access in connection-oriented
	 * mode with authorization.
	 * 
	 * @param link KNX network link used for communication with the KNX network
	 * @param remote KNX individual address to access its interface objects
	 * @param l property adapter listener to get notified about adapter events, use
	 *        <code>null</code> for no listener
	 * @param authorizeKey byte array with authorization key
	 * @throws KNXLinkClosedException if the network link is closed
	 * @throws KNXException on failure during authorization
	 */
	public RemotePropertyServiceAdapter(KNXNetworkLink link, IndividualAddress remote,
		PropertyAdapterListener l, byte[] authorizeKey) throws KNXException
	{
		this(link, remote, l, true);
		key = (byte[]) authorizeKey.clone();
		try {
			mc.authorize(dst, key);
		}
		catch (final KNXException e) {
			close();
			throw e;
		}
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.mgmt.PropertyAdapter#setProperty
	 * (int, int, int, int, byte[])
	 */
	public void setProperty(int objIndex, int pid, int start, int elements, byte[] data)
		throws KNXException
	{
		mc.writeProperty(dst, objIndex, pid, start, elements, data);
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.mgmt.PropertyAdapter#getProperty(int, int, int, int)
	 */
	public byte[] getProperty(int objIndex, int pid, int start, int elements)
		throws KNXException
	{
		return mc.readProperty(dst, objIndex, pid, start, elements);
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.mgmt.PropertyAdapter#getDescription(int, int, int)
	 */
	public byte[] getDescription(int objIndex, int pid, int propIndex)
		throws KNXException
	{
		return mc.readPropertyDesc(dst, objIndex, pid, propIndex);
	}

	/**
	 * {@inheritDoc} The name for this adapter starts with "remote PS " + remote KNX
	 * individual address, allowing easier distinction of adapter types.
	 */
	public String getName()
	{
		return "remote PS " + dst.getAddress();
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.mgmt.PropertyAdapter#isOpen()
	 */
	public boolean isOpen()
	{
		return mc.isOpen();
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.mgmt.PropertyAdapter#close()
	 */
	public void close()
	{
		final KNXNetworkLink lnk = mc.detach();
		if (lnk != null && nll != null)
			lnk.removeLinkListener(nll);
	}
}
