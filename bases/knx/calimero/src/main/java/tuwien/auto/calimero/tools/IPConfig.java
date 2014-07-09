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
    Copyright (C) 2006-2008 W. Kastner

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package tuwien.auto.calimero.tools;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.Settings;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.exception.KNXFormatException;
import tuwien.auto.calimero.exception.KNXIllegalArgumentException;
import tuwien.auto.calimero.exception.KNXRemoteException;
import tuwien.auto.calimero.knxnetip.KNXnetIPConnection;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.KNXNetworkLinkFT12;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.KNXMediumSettings;
import tuwien.auto.calimero.link.medium.PLSettings;
import tuwien.auto.calimero.link.medium.RFSettings;
import tuwien.auto.calimero.link.medium.TPSettings;
import tuwien.auto.calimero.mgmt.Description;
import tuwien.auto.calimero.mgmt.KnIPDeviceMgmtAdapter;
import tuwien.auto.calimero.mgmt.PropertyAdapter;
import tuwien.auto.calimero.mgmt.PropertyClient;
import tuwien.auto.calimero.mgmt.RemotePropertyServiceAdapter;

/**
 * A tool for Calimero to read/set the IP configuration of a KNXnet/IP server using KNX
 * properties.
 * <p>
 * IPConfig is a console based tool implementation for reading and writing the IP
 * configuration in the KNXnet/IP Parameter Object. It supports network access using a
 * KNXnet/IP connection or FT1.2 connection. To run IPConfig, invoke the <code>main</code>
 * -method of this class. Take a look at the command line options for a list of
 * communication settings.
 * <p>
 * This tool interacts with the PropertyClient interface, which offers high level access
 * to KNX property information. It shows creation of the {@link PropertyAdapter},
 * necessary for a property client to work. All output is written to <code>System.out
 * </code>.
 *
 * @author B. Malinowsky
 */
public class IPConfig
{
	private static final String tool = "IPConfig";
	private static final String version = "0.2";

	private static final String sep = System.getProperty("line.separator");
	private static final int IPObjType = 11;

	private PropertyClient pc;
	private KNXNetworkLink lnk;
	private int objIndex = -1;

	/**
	 * Creates a new IPConfig instance using the supplied options.
	 * <p>
	 * See {@link #main(String[])} for a list of options.
	 *
	 * @param args list with options
	 * @throws KNXException
	 */
	protected IPConfig(String[] args) throws KNXException
	{
		// read the command line options and run config
		final Map options = new HashMap();
		try {
			if (!parseOptions(args, options))
				throw new KNXException("only show usage/version information, " + "abort "
					+ tool);
		}
		catch (final RuntimeException e) {
			throw new KNXIllegalArgumentException(e.getMessage());
		}
		run(options);
	}

	/**
	 * Entry point for running IPConfig.
	 * <p>
	 * An IP host or port identifier has to be supplied to specify the endpoint for the
	 * KNX network access.<br>
	 * To show the usage message of this tool on the console, supply the command line
	 * option -help (or -h).<br>
	 * Command line options are treated case sensitive. Available options:
	 * <ul>
	 * <li><code>-help -h</code> show help message</li>
	 * <li><code>-version</code> show tool/library version and exit</li>
	 * <li><code>-local -l</code> local device management</li>
	 * <li><code>-remote -r</code> <i>KNX addr</i> &nbsp;remote property service</li>
	 * <li><code>-localhost</code> <i>id</i> &nbsp;local IP/host name</li>
	 * <li><code>-localport</code> <i>number</i> &nbsp;local UDP port (default system
	 * assigned)</li>
	 * <li><code>-port -p</code> <i>number</i> &nbsp;UDP port on host (default 3671)</li>
	 * <li><code>-nat -n</code> enable Network Address Translation</li>
	 * <li><code>-serial -s</code> use FT1.2 serial communication</li>
	 * </ul>
	 * For remote property service these options are available:
	 * <ul>
	 * <li><code>-routing</code> use KNXnet/IP routing</li>
	 * <li><code>-medium -m</code> <i>id</i> &nbsp;KNX medium [tp0|tp1|p110|p132|rf]
	 * (defaults to tp1)</li>
	 * <li><code>-connect -c</code> connection oriented mode</li>
	 * <li><code>-authorize -a</code> <i>key</i> &nbsp;authorize key to access KNX
	 * device</li>
	 * </ul>
	 * <br>
	 * In any case, the tool reads out the IP configuration of the connected endpoint and
	 * writes it to standard output.<br>
	 * Supply one or more of the following commands to change the IP configuration (these
	 * commands are accepted without regard to capitalization):
	 * <ul>
	 * <li><code>IP</code> <i>address</i> &nbsp;set the configured fixed IP address</li>
	 * <li><code>subnet</code> <i>address</i> &nbsp;set the configured IP subnet mask</li>
	 * <li><code>gateway</code> <i>address</i> &nbsp;set the configured IP address of
	 * the default gateway</li>
	 * <li><code>multicast</code> <i>address</i> &nbsp;set the routing multicast
	 * address</li>
	 * <li><code>manual</code> set manual IP assignment for the current IP address to
	 * enabled</li>
	 * <li><code>BootP</code> set Bootstrap Protocol IP assignment for the current IP
	 * address to enabled</li>
	 * <li><code>DHCP</code> set DHCP IP assignment for the current IP address to
	 * enabled</li>
	 * <li><code>AutoIP</code> set automatic IP assignment for the current IP address
	 * to enabled</li>
	 * </ul>
	 *
	 * @param args command line options to run the tool
	 */
	public static void main(String[] args)
	{
		try {
			new IPConfig(args);
		}
		catch (final Throwable t) {
			if (t.getMessage() != null)
				System.out.println(t.getMessage());
		}
	}

	private void run(Map options) throws KNXException
	{
		try {
			// create a property adapter and supply it to a new client
			pc = new PropertyClient(create(options));
			// get object type with KNXnet/IP parameters
			final List l = pc.scanProperties(false);
			for (final Iterator i = l.iterator(); i.hasNext();) {
				final Description d = (Description) i.next();
				if (d.getObjectType() == IPObjType) {
					objIndex = d.getObjectIndex();
					break;
				}
			}
			if (objIndex == -1) {
				System.out.println(PropertyClient.getObjectTypeName(IPObjType)
					+ " not found");
				return;
			}
			setIPAssignment(options);
			setIP(PropertyClient.PID.IP_ADDRESS, "IP", options);
			setIP(PropertyClient.PID.SUBNET_MASK, "subnet", options);
			setIP(PropertyClient.PID.DEFAULT_GATEWAY, "gateway", options);
			setIP(PropertyClient.PID.ROUTING_MULTICAST_ADDRESS, "multicast", options);
			readConfig();
		}
		finally {
			if (pc != null)
				pc.close();
			if (lnk != null)
				lnk.close();
		}
	}

	private void setIPAssignment(Map options) throws KNXException
	{
		int assignment = 0;
		if (options.containsKey("manual"))
			assignment |= 0x01;
		if (options.containsKey("BootP"))
			assignment |= 0x02;
		if (options.containsKey("DHCP"))
			assignment |= 0x04;
		if (options.containsKey("AutoIP"))
			assignment |= 0x08;
		if (assignment != 0)
			pc.setProperty(objIndex, PropertyClient.PID.IP_ASSIGNMENT_METHOD, 1, 1,
				new byte[] { (byte) assignment });
	}

	private void setIP(int pid, String key, Map options)
	{
		if (options.containsKey(key))
			try {
				pc.setProperty(objIndex, pid, 1, 1, ((InetAddress) options.get(key))
					.getAddress());
			}
			catch (final KNXException e) {
				System.out.println("setting " + key + " failed, " + e.getMessage());
			}
	}

	private void readConfig() throws KNXException
	{
		final List config = new ArrayList();
		int pid = PropertyClient.PID.KNX_INDIVIDUAL_ADDRESS;
		byte[] data = query(pid);
		if (data != null)
			add(config, pid, "KNXnet/IP server", new IndividualAddress(data).toString());
		add(config, PropertyClient.PID.FRIENDLY_NAME, "name", queryFriendlyName());

		pid = PropertyClient.PID.IP_CAPABILITIES;
		if ((data = query(pid)) != null)
			add(config, pid, "IP address assignment available",
				getIPAssignment(new byte[] { (byte) (data[0] << 1 | 0x01) }));

		pid = PropertyClient.PID.IP_ASSIGNMENT_METHOD;
		if ((data = query(pid)) != null)
			add(config, pid, "IP address assignment enabled", getIPAssignment(data));

		pid = PropertyClient.PID.CURRENT_IP_ASSIGNMENT_METHOD;
		if ((data = query(pid)) != null)
			add(config, pid, "IP address assignment current", getIPAssignment(data));

		pid = PropertyClient.PID.KNXNETIP_ROUTING_CAPABILITIES;
		if ((data = query(pid)) != null)
			add(config, pid, "routing capabilities", getRoutingCaps(data));

		addIP(config, PropertyClient.PID.IP_ADDRESS, "IP address configured");
		addIP(config, PropertyClient.PID.CURRENT_IP_ADDRESS, "IP address current");
		addIP(config, PropertyClient.PID.SUBNET_MASK, "subnet mask configured");
		addIP(config, PropertyClient.PID.CURRENT_SUBNET_MASK, "subnet mask  current");
		addIP(config, PropertyClient.PID.DEFAULT_GATEWAY, "default gateway configured");
		addIP(config, PropertyClient.PID.CURRENT_DEFAULT_GATEWAY,
			"default gateway current");
		addIP(config, PropertyClient.PID.DHCP_BOOTP_SERVER, "DHCP/BootP server");
		addIP(config, PropertyClient.PID.ROUTING_MULTICAST_ADDRESS, "routing multicast");

		receivedConfig(config);
	}

	private void addIP(List config, int pid, String name)
	{
		add(config, pid, name, queryIP(pid));
	}

	private void add(List config, int pid, String name, String value)
	{
		config.add(new String[] { Integer.toString(pid), name, value });
	}

	/**
	 * Supplies information about a received IP configuration.
	 * <p>
	 * This default implementation writes information to standard output.
	 *
	 * @param config a list with config entries, a config entry is of type String[3], with
	 *        [0] being the PID, [1] being a short config name, [2] being the value
	 */
	protected void receivedConfig(List config)
	{
		System.out.println("KNXnet/IP server " + ((String[]) config.get(0))[2] + " "
			+ ((String[]) config.get(1))[2]);
		final String padding = "                                   ";
		for (int i = 2; i < config.size(); ++i) {
			final String[] s = (String[]) config.get(i);
			System.out.println(s[1] + padding.substring(s[1].length()) + s[2]);
		}
	}

	private String queryFriendlyName() throws KNXException
	{
		final char[] name = new char[30];
		int start = 0;
		while (true) {
			final byte[] data = pc.getProperty(objIndex,
				PropertyClient.PID.FRIENDLY_NAME, start + 1, 10);
			for (int i = 0; i < 10 && data[i] != 0; ++i, ++start)
				name[start] = (char) (data[i] & 0xff);
			if (start >= 30 || data[9] == 0)
				return new String(name, 0, start);
		}
	}

	private byte[] query(int pid) throws KNXException
	{
		try {
			return pc.getProperty(objIndex, pid, 1, 1);
		}
		catch (final KNXRemoteException e) {
			System.out.println("get property ID " + pid + " failed, " + e.getMessage());
			return null;
		}
	}

	private String queryIP(int pid)
	{
		try {
			final byte[] data = query(pid);
			return data == null ? "PID not found" : InetAddress.getByAddress(data)
				.getHostAddress();
		}
		catch (final UnknownHostException e) {}
		catch (final KNXException e) {}
		return "-";
	}

	private static String getIPAssignment(byte[] value)
	{
		final int bitset = value[0] & 0xff;
		String assignment = "";
		if ((bitset & 0x01) != 0)
			assignment += "manual, ";
		if ((bitset & 0x02) != 0)
			assignment += "BootP, ";
		if ((bitset & 0x04) != 0)
			assignment += "DHCP, ";
		if ((bitset & 0x08) != 0)
			assignment += "AutoIP";
		return assignment;
	}

	private static String getRoutingCaps(byte[] value)
	{
		final int bitset = value[0] & 0xff;
		String caps = "";
		if ((bitset & 0x01) != 0)
			caps += "queue overflow statistics, ";
		if ((bitset & 0x02) != 0)
			caps += "transmitted telegrams statistics, ";
		if ((bitset & 0x04) != 0)
			caps += "priority/FIFO, ";
		if ((bitset & 0x08) != 0)
			caps += "multiple KNX installations, ";
		if ((bitset & 0x10) != 0)
			caps += "group address mapping, ";
		return caps;
	}

	/**
	 * Creates the property adapter to be used, depending on the supplied user
	 * <code>options</code>.
	 * <p>
	 * There are two types of property adapters. One uses KNXnet/IP local device
	 * management to access KNX properties in an interface object, the other type uses
	 * remote property services. The remote adapter needs a KNX network link to access the
	 * KNX network, the link is also created by this method if this adapter type is
	 * requested.
	 *
	 * @param options contains parameters for property adapter creation
	 * @return the created adapter
	 * @throws KNXException on adapter creation problem
	 */
	private PropertyAdapter create(Map options) throws KNXException
	{
		// create local and remote socket address for use in adapter
		final InetSocketAddress local = createLocalSocket((InetAddress) options
			.get("localhost"), (Integer) options.get("localport"));
		final InetSocketAddress host = new InetSocketAddress((InetAddress) options
			.get("host"), ((Integer) options.get("port")).intValue());
		// decide what type of adapter to create
		if (options.containsKey("localDM"))
			return createLocalDMAdapter(local, host, options);
		return createRemoteAdapter(local, host, options);
	}

	/**
	 * Creates a local device management adapter.
	 * <p>
	 *
	 * @param local local socket address
	 * @param host remote socket address of host
	 * @param options contains parameters for property adapter creation
	 * @return local DM adapter
	 * @throws KNXException on adapter creation problem
	 */
	private PropertyAdapter createLocalDMAdapter(InetSocketAddress local,
		InetSocketAddress host, Map options) throws KNXException
	{
		return new KnIPDeviceMgmtAdapter(local, host, options.containsKey("nat"), null,
			false);
	}

	/**
	 * Creates a remote property service adapter for one device in the KNX network.
	 * <p>
	 * The adapter uses a KNX network link for access, also is created by this method.
	 *
	 * @param local local socket address
	 * @param host remote socket address of host
	 * @param options contains parameters for property adapter creation
	 * @return remote property service adapter
	 * @throws KNXException on adapter creation problem
	 */
	private PropertyAdapter createRemoteAdapter(InetSocketAddress local,
		InetSocketAddress host, Map options) throws KNXException
	{
		final KNXMediumSettings medium = (KNXMediumSettings) options.get("medium");
		if (options.containsKey("serial")) {
			// create FT1.2 network link
			final String p = (String) options.get("serial");
			try {
				lnk = new KNXNetworkLinkFT12(Integer.parseInt(p), medium);
			}
			catch (final NumberFormatException e) {
				lnk = new KNXNetworkLinkFT12(p, medium);
			}
		}
		else {
			final int mode = options.containsKey("routing") ? KNXNetworkLinkIP.ROUTER
				: KNXNetworkLinkIP.TUNNEL;
			lnk = new KNXNetworkLinkIP(mode, local, host, options.containsKey("nat"),
				medium);
		}
		final IndividualAddress remote = (IndividualAddress) options.get("remote");
		// if an authorization key was supplied, the adapter uses
		// connection oriented mode and tries to authenticate
		final byte[] authKey = (byte[]) options.get("authorize");
		if (authKey != null)
			return new RemotePropertyServiceAdapter(lnk, remote, null, authKey);
		return new RemotePropertyServiceAdapter(lnk, remote, null, options
			.containsKey("connect"));
	}

	private static boolean parseOptions(String[] args, Map options)
		throws KNXFormatException
	{
		if (args.length == 0) {
			System.out.println("A tool for KNXnet/IP address configuration");
			showVersion();
			System.out.println("type -help for help message");
			return false;
		}
		// add defaults
		options.put("port", new Integer(KNXnetIPConnection.IP_PORT));
		options.put("medium", TPSettings.TP1);

		int i = 0;
		for (; i < args.length; i++) {
			final String arg = args[i];
			if (isOption(arg, "-help", "-h")) {
				showUsage();
				return false;
			}
			if (isOption(arg, "-version", null)) {
				showVersion();
				return false;
			}
			if (isOption(arg, "-local", "-l"))
				options.put("localDM", null);
			else if (isOption(arg, "-remote", "-r"))
				options.put("remote", new IndividualAddress(args[++i]));
			else if (isOption(arg, "-localhost", null))
				parseIP(args[++i], "localhost", options);
			else if (isOption(arg, "-localport", null))
				options.put("localport", Integer.decode(args[++i]));
			else if (isOption(arg, "-port", "-p"))
				options.put("port", Integer.decode(args[++i]));
			else if (isOption(arg, "-nat", "-n"))
				options.put("nat", null);
			else if (isOption(arg, "-serial", "-s"))
				options.put("serial", null);
			else if (isOption(arg, "-medium", "-m"))
				options.put("medium", getMedium(args[++i]));
			else if (isOption(arg, "-connect", "-c"))
				options.put("connect", null);
			else if (isOption(arg, "-authorize", "-a"))
				options.put("authorize", getAuthorizeKey(args[++i]));
			else if (isOption(arg, "-routing", null))
				options.put("routing", null);
			// IP configuration options
			else if (arg.equalsIgnoreCase("manual"))
				options.put("manual", null);
			else if (arg.equalsIgnoreCase("BootP"))
				options.put("BootP", null);
			else if (arg.equalsIgnoreCase("DHCP"))
				options.put("DHCP", null);
			else if (arg.equalsIgnoreCase("AutoIP"))
				options.put("AutoIP", null);
			else if (arg.equalsIgnoreCase("IP"))
				parseIP(args[++i], "IP", options);
			else if (arg.equalsIgnoreCase("subnet"))
				parseIP(args[++i], "subnet", options);
			else if (arg.equalsIgnoreCase("gateway"))
				parseIP(args[++i], "gateway", options);
			else if (arg.equalsIgnoreCase("multicast"))
				parseIP(args[++i], "multicast", options);
			// add option as port identifier or host name
			else if (options.containsKey("serial"))
				options.put("serial", arg);
			else if (!options.containsKey("host"))
				parseIP(arg, "host", options);
			else
				throw new IllegalArgumentException("unknown option " + arg);
		}
		if (!options.containsKey("localDM") && !options.containsKey("remote"))
			throw new IllegalArgumentException("no connection category specified");
		if (!options.containsKey("host") && !options.containsKey("serial"))
			throw new IllegalArgumentException("no host or serial port specified");
		if (options.containsKey("serial") && !options.containsKey("remote"))
			throw new IllegalArgumentException("-remote option is mandatory with -serial");
		return true;
	}

	private static void showUsage()
	{
		final StringBuffer sb = new StringBuffer();
		sb.append("usage: ").append(tool).append(" [options] <host|port>").append(sep);
		sb.append("options:").append(sep);
		sb.append("  -help -h                show this help message").append(sep);
		sb.append("  -version                show tool/library version and exit").append(
			sep);
		sb.append("  -local -l               local device management").append(sep);
		sb.append("  -remote -r <KNX addr>   remote property service").append(sep);
		sb.append("  -localhost <id>         local IP/host name").append(sep);
		sb.append("  -localport <number>     local UDP port (default system assigned)")
			.append(sep);
		sb.append("  -port -p <number>       UDP port on <host> (default ").append(
			KNXnetIPConnection.IP_PORT).append(")").append(sep);
		sb.append("  -nat -n                 enable Network Address Translation").append(
			sep);
		sb.append("  -serial -s              use FT1.2 serial communication").append(sep);
		sb.append(" remote property service only:").append(sep);
		sb.append("  -routing                use KNXnet/IP routing").append(sep);
		sb.append("  -medium -m <id>         KNX medium [tp0|tp1|p110|p132|rf] "
			+ "(default tp1)").append(sep);
		sb.append("  -connect -c             connection oriented mode").append(sep);
		sb.append("  -authorize -a <key>     authorize key to access KNX device").append(
			sep);
		sb.append("To change the IP configuration, supply one or more commands "
			+ "(case insensitive):").append(sep);
		sb.append("  IP <address>            set the configured fixed IP address")
			.append(sep);
		sb.append("  subnet <address>        set the configured IP subnet mask").append(
			sep);
		sb.append("  gateway <address>       set the configured IP address of the "
			+ "default gateway").append(sep);
		sb.append("  multicast <address>     set the routing multicast address").append(
			sep);
		sb.append("  manual         enable manual IP assignment for current IP address")
			.append(sep);
		sb.append("  BootP          enable Bootstrap Protocol IP assignment for current "
			+ "IP address").append(sep);
		sb.append("  DHCP           enable DHCP IP assignment for current IP address")
			.append(sep);
		sb.append("  AutoIP         enable automatic IP assignment for current "
			+ "IP address").append(sep);
		System.out.println(sb);
	}

	//
	// utility methods
	//

	private static void showVersion()
	{
		System.out.println(tool + " version " + version + " using "
			+ Settings.getLibraryHeader(false));
	}

	private static KNXMediumSettings getMedium(String id)
	{
		// for now, the local device address is always left 0 in the
		// created medium setting, since there is no user cmd line option for this
		// so KNXnet/IP server will supply address
		if (id.equals("tp0"))
			return TPSettings.TP0;
		else if (id.equals("tp1"))
			return TPSettings.TP1;
		else if (id.equals("p110"))
			return new PLSettings(false);
		else if (id.equals("p132"))
			return new PLSettings(true);
		else if (id.equals("rf"))
			return new RFSettings(null);
		else
			throw new KNXIllegalArgumentException("unknown medium");
	}

	private static byte[] getAuthorizeKey(String key)
	{
		final long value = Long.decode(key).longValue();
		if (value < 0 || value > 0xFFFFFFFFL)
			throw new KNXIllegalArgumentException("invalid authorize key");
		return new byte[] { (byte) (value >> 24), (byte) (value >> 16),
			(byte) (value >> 8), (byte) value };
	}

	private static void parseIP(String address, String key, Map options)
	{
		try {
			options.put(key, InetAddress.getByName(address));
		}
		catch (final UnknownHostException e) {
			throw new IllegalArgumentException("failed to read IP " + address);
		}
	}

	private static InetSocketAddress createLocalSocket(InetAddress host, Integer port)
	{
		final int p = port != null ? port.intValue() : 0;
		try {
			return host != null ? new InetSocketAddress(host, p) : p != 0
				? new InetSocketAddress(InetAddress.getLocalHost(), p) : null;
		}
		catch (final UnknownHostException e) {
			throw new IllegalArgumentException("failed to create local host "
				+ e.getMessage());
		}
	}

	private static boolean isOption(String arg, String longOpt, String shortOpt)
	{
		return arg.equals(longOpt) || shortOpt != null && arg.equals(shortOpt);
	}
}
