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

package tuwien.auto.calimero.link;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import tuwien.auto.calimero.CloseEvent;
import tuwien.auto.calimero.FrameEvent;
import tuwien.auto.calimero.cemi.CEMIBusMon;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.exception.KNXFormatException;
import tuwien.auto.calimero.exception.KNXIllegalArgumentException;
import tuwien.auto.calimero.knxnetip.KNXnetIPConnection;
import tuwien.auto.calimero.knxnetip.KNXnetIPTunnel;
import tuwien.auto.calimero.link.event.LinkListener;
import tuwien.auto.calimero.link.event.MonitorFrameEvent;
import tuwien.auto.calimero.link.medium.KNXMediumSettings;
import tuwien.auto.calimero.link.medium.RawFrame;
import tuwien.auto.calimero.link.medium.RawFrameFactory;
import tuwien.auto.calimero.log.LogManager;
import tuwien.auto.calimero.log.LogService;

/**
 * Implementation of the KNX network monitor link based on the KNXnet/IP protocol, using a
 * {@link KNXnetIPConnection}.
 * <p>
 * Once a monitor has been closed, it is not available for further link communication,
 * i.e. it can't be reopened.
 * <p>
 * Pay attention to the IP address consideration stated in the documentation comments of
 * class {@link KNXNetworkLinkIP}.
 * 
 * @author B. Malinowsky
 */
public class KNXNetworkMonitorIP implements KNXNetworkMonitor
{
	private static final class MonitorNotifier extends EventNotifier
	{
		volatile boolean decode;

		MonitorNotifier(Object source, LogService logger)
		{
			super(source, logger);
		}

		public void frameReceived(FrameEvent e)
		{
			final int mc = e.getFrame().getMessageCode();
			if (mc == CEMIBusMon.MC_BUSMON_IND) {
				RawFrame raw = null;
				logger.info("received monitor indication");
				if (decode)
					try {
						final short m = ((KNXNetworkMonitorIP) source).medium.getMedium();
						raw = RawFrameFactory.create(m, e.getFrame().getPayload(), 0);
					}
					catch (final KNXFormatException ex) {
						logger.error("decoding raw frame", ex);
					}
				addEvent(new Indication(new MonitorFrameEvent(source, e.getFrame(), raw)));
			}
			else
				logger.warn("unspecified frame event - ignored, msg code = 0x"
					+ Integer.toHexString(mc));
		}

		public void connectionClosed(CloseEvent e)
		{
			((KNXNetworkMonitorIP) source).closed = true;
			super.connectionClosed(e);
			logger.info("monitor closed");
			LogManager.getManager().removeLogService(logger.getName());
		}
	};

	private volatile boolean closed;
	private final KNXnetIPConnection conn;
	private KNXMediumSettings medium;

	private final LogService logger;
	// our link connection event notifier
	private final MonitorNotifier notifier;

	/**
	 * Creates a new network monitor based on the KNXnet/IP protocol for accessing the KNX
	 * network.
	 * <p>
	 * 
	 * @param localEP the local endpoint to use for the link, this is the client control
	 *        endpoint, use <code>null</code> for the default local host and an
	 *        ephemeral port number
	 * @param remoteEP the remote endpoint of the link; this is the server control
	 *        endpoint
	 * @param useNAT <code>true</code> to use network address translation in the
	 *        KNXnet/IP protocol, <code>false</code> to use the default (non aware) mode
	 * @param settings medium settings defining the specific KNX medium needed for
	 *        decoding raw frames received from the KNX network
	 * @throws KNXException on failure establishing the link
	 */
	public KNXNetworkMonitorIP(InetSocketAddress localEP, InetSocketAddress remoteEP,
		boolean useNAT, KNXMediumSettings settings) throws KNXException
	{
		InetSocketAddress ep = localEP;
		if (ep == null)
			try {
				ep = new InetSocketAddress(InetAddress.getLocalHost(), 0);
			}
			catch (final UnknownHostException e) {
				throw new KNXException("no local host available");
			}
		conn = new KNXnetIPTunnel(KNXnetIPTunnel.BUSMONITOR_LAYER, ep, remoteEP, useNAT);
		logger = LogManager.getManager().getLogService(getName());
		logger.info("in busmonitor mode - ready to receive");
		notifier = new MonitorNotifier(this, logger);
		conn.addConnectionListener(notifier);
		// configure KNX medium stuff
		setKNXMedium(settings);
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.link.KNXNetworkMonitor#setKNXMedium
	 * (tuwien.auto.calimero.link.medium.KNXMediumSettings)
	 */
	public void setKNXMedium(KNXMediumSettings settings)
	{
		if (settings == null)
			throw new KNXIllegalArgumentException("medium settings are mandatory");
		if (medium != null && !settings.getClass().isAssignableFrom(medium.getClass())
			&& !medium.getClass().isAssignableFrom(settings.getClass()))
			throw new KNXIllegalArgumentException("medium differs");
		medium = settings;
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.link.KNXNetworkMonitor#getKNXMedium()
	 */
	public KNXMediumSettings getKNXMedium()
	{
		return medium;
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.link.KNXNetworkMonitor#addMonitorListener
	 * (tuwien.auto.calimero.link.event.LinkListener)
	 */
	public void addMonitorListener(LinkListener l)
	{
		notifier.addListener(l);
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.link.KNXNetworkMonitor#removeMonitorListener
	 * (tuwien.auto.calimero.link.event.LinkListener)
	 */
	public void removeMonitorListener(LinkListener l)
	{
		notifier.removeListener(l);
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.link.KNXNetworkMonitor#setDecodeRawFrames(boolean)
	 */
	public void setDecodeRawFrames(boolean decode)
	{
		notifier.decode = decode;
		logger.info((decode ? "enable" : "disable") + " decoding of raw frames");
	}

	/**
	 * {@inheritDoc}<br>
	 * The returned name is "monitor " + remote IP address of the control endpoint + ":" +
	 * remote port used by the monitor.
	 */
	public String getName()
	{
		// do our own IP:port string, since InetAddress.toString() always prepends a '/'
		final InetSocketAddress a = conn.getRemoteAddress();
		return "monitor " + a.getAddress().getHostAddress() + ":" + a.getPort();
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.link.KNXNetworkMonitor#isOpen()
	 */
	public boolean isOpen()
	{
		return !closed;
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.link.KNXNetworkMonitor#close()
	 */
	public void close()
	{
		synchronized (this) {
			if (closed)
				return;
			closed = true;
		}
		conn.close();
		notifier.quit();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return getName() + (closed ? " (closed), " : ", ") + medium.getMediumString()
			+ " medium" + (notifier.decode ? ", decode raw frames" : "");
	}
}
