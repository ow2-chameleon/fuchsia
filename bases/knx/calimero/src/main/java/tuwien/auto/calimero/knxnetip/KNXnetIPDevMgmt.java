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
import java.net.DatagramPacket;
import java.net.InetSocketAddress;

import tuwien.auto.calimero.DataUnitBuilder;
import tuwien.auto.calimero.cemi.CEMI;
import tuwien.auto.calimero.cemi.CEMIDevMgmt;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.exception.KNXFormatException;
import tuwien.auto.calimero.exception.KNXIllegalArgumentException;
import tuwien.auto.calimero.exception.KNXInvalidResponseException;
import tuwien.auto.calimero.exception.KNXRemoteException;
import tuwien.auto.calimero.exception.KNXTimeoutException;
import tuwien.auto.calimero.knxnetip.servicetype.ErrorCodes;
import tuwien.auto.calimero.knxnetip.servicetype.KNXnetIPHeader;
import tuwien.auto.calimero.knxnetip.servicetype.PacketHelper;
import tuwien.auto.calimero.knxnetip.servicetype.ServiceAck;
import tuwien.auto.calimero.knxnetip.servicetype.ServiceRequest;
import tuwien.auto.calimero.knxnetip.util.CRI;
import tuwien.auto.calimero.log.LogLevel;

/**
 * KNXnet/IP connection for KNX local device management.
 * <p>
 * The communication on OSI layer 4 is done using UDP.<br>
 * 
 * @author B. Malinowsky
 */
public class KNXnetIPDevMgmt extends ConnectionImpl
{
	/**
	 * Connection type used to configure a KNXnet/IP device.
	 * <p>
	 */
	public static final short DEVICE_MGMT_CONNECTION = 0x03;

	// client SHALL wait 10 seconds for a device config response from server
	private static final int CONFIGURATION_REQ_TIMEOUT = 10;

	/**
	 * Creates a new KNXnet/IP device management connection to a remote device.
	 * <p>
	 * 
	 * @param localEP the local endpoint to use for communication channel
	 * @param serverCtrlEP the remote server control endpoint used for connect request
	 * @param useNAT <code>true</code> to use a NAT (network address translation) aware
	 *        communication mechanism, <code>false</code> to use the default way
	 * @throws KNXException on socket communication error
	 * @throws KNXTimeoutException on no connect response before connect timeout
	 * @throws KNXRemoteException if response indicates an error condition at the server
	 *         concerning the request
	 * @throws KNXInvalidResponseException if connect response is in wrong format
	 */
	public KNXnetIPDevMgmt(InetSocketAddress localEP, InetSocketAddress serverCtrlEP,
		boolean useNAT) throws KNXException
	{
		responseTimeout = CONFIGURATION_REQ_TIMEOUT;
		serviceRequest = KNXnetIPHeader.DEVICE_CONFIGURATION_REQ;
		serviceAck = KNXnetIPHeader.DEVICE_CONFIGURATION_ACK;
		maxSendAttempts = 4;
		try {
			final CRI cri = CRI.createRequest(DEVICE_MGMT_CONNECTION, null);
			connect(localEP, serverCtrlEP, cri, useNAT);
		}
		catch (final KNXFormatException ignore) {}
	}

	/**
	 * Sends a cEMI device management frame to the remote server communicating with this
	 * endpoint.
	 * <p>
	 * 
	 * @param frame cEMI device management message of type {@link CEMIDevMgmt} to send
	 */
	public void send(CEMI frame, BlockingMode mode) throws KNXTimeoutException,
		KNXConnectionClosedException
	{
		if (!(frame instanceof CEMIDevMgmt))
			throw new KNXIllegalArgumentException("unsupported cEMI type");
		super.send(frame, mode);
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.knxnetip.KNXnetIPConnection#getName()
	 */
	public String getName()
	{
		return "KNXnet/IP DM " + ctrlEP.getAddress().getHostAddress();
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.knxnetip.ConnectionImpl#handleService
	 * (tuwien.auto.calimero.knxnetip.servicetype.KNXnetIPHeader, byte[], int)
	 */
	void handleService(KNXnetIPHeader h, byte[] data, int offset)
		throws KNXFormatException, IOException
	{
		final int svc = h.getServiceType();
		if (svc == KNXnetIPHeader.DEVICE_CONFIGURATION_REQ) {
			ServiceRequest req;
			try {
				req = PacketHelper.getServiceRequest(h, data, offset);
			}
			catch (final KNXFormatException e) {
				// try to get at least the connection header of the service request
				req = PacketHelper.getEmptyServiceRequest(h, data, offset);
				final byte[] junk = new byte[h.getTotalLength() - h.getStructLength() - 4];
				System.arraycopy(data, offset + 4, junk, 0, junk.length);
				logger.warn("received dev.mgmt request with unknown cEMI part "
					+ DataUnitBuilder.toHex(junk, " "), e);
			}
			final short seq = req.getSequenceNumber();
			if (req.getChannelID() == getChannelID() && seq == getSeqNoRcv()) {
				final short status = h.getVersion() == KNXNETIP_VERSION_10 ?
					ErrorCodes.NO_ERROR	: ErrorCodes.VERSION_NOT_SUPPORTED;
				final byte[] buf =
					PacketHelper.toPacket(new ServiceAck(KNXnetIPHeader.
						DEVICE_CONFIGURATION_ACK, getChannelID(), seq, status));
				final DatagramPacket p = new DatagramPacket(buf, buf.length,
					dataEP.getAddress(), dataEP.getPort());
				socket.send(p);
				incSeqNoRcv();
				if (status == ErrorCodes.VERSION_NOT_SUPPORTED) {
					close(ConnectionCloseEvent.INTERNAL, "protocol version changed",
						LogLevel.ERROR, null);
					return;
				}
				final CEMI cemi = req.getCEMI();
				// leave if we are working with an empty (broken) service request
				if (cemi == null)
					return;
				final short mc = cemi.getMessageCode();
				if (mc == CEMIDevMgmt.MC_PROPINFO_IND || mc == CEMIDevMgmt.MC_RESET_IND)
					fireFrameReceived(cemi);
				else if (mc == CEMIDevMgmt.MC_PROPREAD_CON
					|| mc == CEMIDevMgmt.MC_PROPWRITE_CON) {
					// invariant: notify listener before return from blocking send
					fireFrameReceived(cemi);
					setStateNotify(OK);
				}
			}
			else
				logger.warn("received dev.mgmt request channel-ID " + req.getChannelID()
					+ ", receive-sequence " + seq + ", expected " + getSeqNoRcv()
					+ " - ignored");
		}
		else
			logger.warn("received unknown frame (service type 0x"
				+ Integer.toHexString(svc) + ") - ignored");
	}
}
