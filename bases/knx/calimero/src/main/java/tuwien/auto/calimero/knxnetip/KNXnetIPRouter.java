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

package tuwien.auto.calimero.knxnetip;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Iterator;

import tuwien.auto.calimero.KNXListener;
import tuwien.auto.calimero.cemi.CEMI;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.exception.KNXFormatException;
import tuwien.auto.calimero.exception.KNXIllegalArgumentException;
import tuwien.auto.calimero.exception.KNXTimeoutException;
import tuwien.auto.calimero.knxnetip.servicetype.KNXnetIPHeader;
import tuwien.auto.calimero.knxnetip.servicetype.RoutingIndication;
import tuwien.auto.calimero.knxnetip.servicetype.RoutingLostMessage;
import tuwien.auto.calimero.log.LogLevel;
import tuwien.auto.calimero.log.LogManager;

/**
 * KNXnet/IP connection for KNX routing protocol.
 * <p>
 * A KNXnet/IP router is a fast replacement for line/backbone couplers and connected
 * main/backbone lines, using Ethernet cabling for example.<br>
 * The router use point to multipoint communication (multicast). By default, routers are
 * joined to the {@link KNXnetIPRouter#DEFAULT_MULTICAST} multicast group. On more KNX
 * installations in one IP network, different multicast addresses have to be assigned.<br>
 * All IP datagrams use port number 3671, i.e only datagrams on this port are observed.
 * <br>
 * The routing protocol is an unconfirmed service.
 * <p>
 * Optionally, a listener of type {@link RouterListener} can be supplied to
 * {@link #addConnectionListener(KNXListener)} instead of a default {@link KNXListener},
 * to receive {@link RouterListener#lostMessage(LostMessageEvent)} notifications.
 * <p>
 * Multicast considerations:<br>
 * By default, the loopback mode of the socket used for sending multicast datagrams is
 * disabled.
 * <p>
 * A multicast datagram sent with an initial hop count greater 1 may be delivered to the
 * sending host on a different interface (than the sending one), if the host is a member
 * of the multicast group on that interface. The loopback mode setting of the sender's
 * socket has no effect on this behavior.
 * 
 * @author B. Malinowsky
 */
public class KNXnetIPRouter extends ConnectionImpl
{
	/**
	 * Multicast address assigned by default to KNXnet/IP routers, address
	 * {@value #DEFAULT_MULTICAST}.
	 * <p>
	 * This is the standard system setup multicast address used in KNXnet/IP.
	 * <p>
	 */
	public static final String DEFAULT_MULTICAST = "224.0.23.12";

	private final InetAddress multicast;

	/**
	 * Creates a new KNXnet/IP routing service.
	 * <p>
	 * In general, routers are assigned a multicast address by adding an offset to the
	 * system setup multicast address ({@value #DEFAULT_MULTICAST}) for each KNX
	 * installation, by default this offset is 0 (i.e. only one used installation).
	 * 
	 * @param netIf specifies the local network interface used to join the multicast group
	 *        and send outgoing multicast data, use <code>null</code> to use the default
	 *        interface; useful for multihomed hosts
	 * @param mcGroup address of the multicast group this router is joined to, or
	 *        <code>null</code> to use the default multicast ({@value #DEFAULT_MULTICAST});
	 *        value of <code>mcAddr</code> >= {@value #DEFAULT_MULTICAST}
	 * @throws KNXException on socket error, or if joining to group failed
	 */
	public KNXnetIPRouter(NetworkInterface netIf, InetAddress mcGroup)
		throws KNXException
	{
		InetAddress def = null;
		try {
			def = InetAddress.getByName(DEFAULT_MULTICAST);
		}
		catch (final UnknownHostException e) {}
		if (mcGroup == null)
			multicast = def;
		else if (!mcGroup.isMulticastAddress() || toLong(mcGroup) < toLong(def))
			throw new KNXIllegalArgumentException("invalid routing multicast " + mcGroup);
		else
			multicast = mcGroup;

		responseTimeout = 0;
		serviceRequest = KNXnetIPHeader.ROUTING_IND;
		serviceAck = 0;
		maxSendAttempts = 1;
		dataEP = new InetSocketAddress(multicast, IP_PORT);
		ctrlEP = new InetSocketAddress(multicast, IP_PORT);
		logger = LogManager.getManager().getLogService(getName());
		
		MulticastSocket s = null;
		try {
			s = new MulticastSocket(IP_PORT);
			if (netIf != null) {
				s.setNetworkInterface(netIf);
				// port number is not used in join group
				s.joinGroup(new InetSocketAddress(multicast, 0), netIf);
			}
			else
				s.joinGroup(multicast);
		}
		catch (final IOException e) {
			if (s != null)
				s.close();
			throw new KNXException(e.getMessage());
		}
		// try to disable loopback of sent frames back to our socket
		try {
			s.setLoopbackMode(true);
		}
		catch (final SocketException e) {
			logger.warn("loopback mode for multicast datagrams couldn't be disabled");
		}
		socket = s;
		startReceiver();
		setState(OK);
	}

	/**
	 * Sends a cEMI frame to the joined multicast group.
	 * <p>
	 * 
	 * @param frame cEMI message to send
	 * @param mode arbitrary value, does not influence behavior, since routing is always a
	 *        unconfirmed, nonblocking service
	 */
	public void send(CEMI frame, BlockingMode mode) throws KNXConnectionClosedException
	{
		try {
			super.send(frame, NONBLOCKING);
			// we always succeed...
			setState(OK);
		}
		catch (final KNXTimeoutException e) {}
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.knxnetip.KNXnetIPConnection#getName()
	 */
	public String getName()
	{
		return "KNXnet/IP routing " + ctrlEP.getAddress().getHostAddress();
	}

	/**
	 * Sets the default hop count (TTL) used in the IP header of encapsulated cEMI
	 * messages.
	 * <p>
	 * This value is used to limit the multicast geographically, although this is just a
	 * rough estimation. The hop count value is forwarded to the underlying multicast
	 * socket used for communication.
	 * 
	 * @param hopCount hop count value, 0 &lt;= value &lt;= 255
	 */
	public final void setHopCount(int hopCount)
	{
		if (hopCount < 0 || hopCount > 255)
			throw new KNXIllegalArgumentException("hop count out of range");
		try {
			((MulticastSocket) socket).setTimeToLive(hopCount);
		}
		catch (final IOException e) {
			logger.error("failed to set hop count", e);
		}
	}

	/**
	 * Returns the default hop count (TTL) used in the IP header of encapsulated cEMI
	 * messages.
	 * <p>
	 * The hop count value is queried from the used multicast socket.
	 * 
	 * @return hop count in the range 0 to 255
	 */
	public final int getHopCount()
	{
		try {
			return ((MulticastSocket) socket).getTimeToLive();
		}
		catch (final IOException e) {
			logger.error("failed to get hop count", e);
		}
		return 1;
	}

	void close(int initiator, String reason, LogLevel level, Throwable t)
	{
		if (getState() == CLOSED)
			return;
		try {
			((MulticastSocket) socket).leaveGroup(multicast);
		}
		catch (final IOException e) {
			logger.warn("problem on leaving multicast group", e);
		}
		finally {
			shutdown(initiator, reason, level, t);
		}
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.knxnetip.ConnectionImpl#handleService
	 * (tuwien.auto.calimero.knxnetip.servicetype.KNXnetIPHeader, byte[], int)
	 */
	void handleService(KNXnetIPHeader h, byte[] data, int offset)
		throws KNXFormatException
	{
		final int svc = h.getServiceType();
		if (h.getVersion() != KNXNETIP_VERSION_10)
			close(ConnectionCloseEvent.INTERNAL, "protocol version changed",
				LogLevel.ERROR, null);
		else if (svc == KNXnetIPHeader.ROUTING_IND) {
			final RoutingIndication ind = new RoutingIndication(data, offset,
				h.getTotalLength() - h.getStructLength());
			fireFrameReceived(ind.getCEMI());
		}
		else if (svc == KNXnetIPHeader.ROUTING_LOST_MSG) {
			final RoutingLostMessage lost = new RoutingLostMessage(data, offset);
			fireLostMessage(lost);
		}
		else
			logger.warn("received unknown frame (service type 0x"
				+ Integer.toHexString(svc) + ") - ignored");
	}

	private void fireLostMessage(RoutingLostMessage lost)
	{
		final LostMessageEvent e =
			new LostMessageEvent(this, lost.getDeviceState(), lost.getLostMessages());
		for (final Iterator i = getListeners().iterator(); i.hasNext();) {
			final Object o = i.next();
			if (o instanceof RouterListener)
				try {
					((RouterListener) o).lostMessage(e);
				}
				catch (final RuntimeException rte) {
					removeConnectionListener((KNXListener) o);
					logger.error("removed event listener", rte);
				}
		}
	}

	private long toLong(InetAddress addr)
	{
		// we assume 4 byte Internet address for multicast
		final byte[] buf = addr.getAddress();
		long ret = buf[3] & 0xffL;
		ret |= (buf[2] << 8) & 0xff00L;
		ret |= (buf[1] << 16) & 0xff0000L;
		ret |= (buf[0] << 24) & 0xff000000L;
		return ret;
	}
}
