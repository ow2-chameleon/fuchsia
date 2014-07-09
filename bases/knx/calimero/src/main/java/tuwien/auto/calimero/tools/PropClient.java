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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import tuwien.auto.calimero.CloseEvent;
import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.Settings;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.exception.KNXFormatException;
import tuwien.auto.calimero.exception.KNXIllegalArgumentException;
import tuwien.auto.calimero.knxnetip.KNXnetIPConnection;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.KNXNetworkLinkFT12;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.KNXMediumSettings;
import tuwien.auto.calimero.link.medium.PLSettings;
import tuwien.auto.calimero.link.medium.RFSettings;
import tuwien.auto.calimero.link.medium.TPSettings;
import tuwien.auto.calimero.log.LogLevel;
import tuwien.auto.calimero.log.LogManager;
import tuwien.auto.calimero.log.LogStreamWriter;
import tuwien.auto.calimero.mgmt.Description;
import tuwien.auto.calimero.mgmt.KnIPDeviceMgmtAdapter;
import tuwien.auto.calimero.mgmt.PropertyAdapter;
import tuwien.auto.calimero.mgmt.PropertyAdapterListener;
import tuwien.auto.calimero.mgmt.PropertyClient;
import tuwien.auto.calimero.mgmt.RemotePropertyServiceAdapter;
import tuwien.auto.calimero.mgmt.PropertyClient.Property;
import tuwien.auto.calimero.mgmt.PropertyClient.PropertyKey;

/**
 * A tool for Calimero showing features of the {@link PropertyClient} used for KNX
 * property access.
 * <p>
 * PropClient is a console based tool implementation for reading and writing KNX
 * properties. It supports network access using a KNXnet/IP connection or FT1.2
 * connection. To start the PropClient, invoke the <code>main</code>-method of this
 * class. Take a look at the command line options to configure the tool with the desired
 * communication settings.
 * <p>
 * The main part of this tool implementation interacts with the PropertyClient interface,
 * which offers high level access to KNX property information. It also shows creation of
 * the {@link PropertyAdapter}, necessary for a property client to work. All queried
 * property values, as well as occurring problems are written to <code>System.out
 * </code>.
 *
 * @author B. Malinowsky
 */
public class PropClient
{
	private static final String tool = "PropClient";
	private static final String version = "0.1";
	private static final String sep = System.getProperty("line.separator");

	private PropertyClient pc;
	private KNXNetworkLink lnk;
	private Map definitions;

	/**
	 * Empty constructor.
	 * <p>
	 */
	protected PropClient()
	{}

	/**
	 * Entry point for running the PropClient.
	 * <p>
	 * An IP host or port identifier has to be supplied to specify the endpoint for the
	 * KNX network access.<br>
	 * To show the usage message of this tool on the console, supply the command line
	 * option -help (or -h).<br>
	 * Command line options are treated case sensitive. Available options for the property
	 * client:
	 * <ul>
	 * <li><code>-help -h</code> show help message</li>
	 * <li><code>-version</code> show tool/library version and exit</li>
	 * <li><code>-verbose -v</code> enable verbose status output</li>
	 * <li><code>-local -l</code> local device management</li>
	 * <li><code>-remote -r</code> <i>KNX addr</i> &nbsp;remote property service</li>
	 * <li><code>-definitions -d</code> <i>file</i> &nbsp;use property definition file</li>
	 * <li><code>-localhost</code> <i>id</i> &nbsp;local IP/host name</li>
	 * <li><code>-localport</code> <i>number</i> &nbsp;local UDP port (default system
	 * assigned)</li>
	 * <li><code>-port -p</code> <i>number</i> &nbsp;UDP port on host (default 3671)</li>
	 * <li><code>-nat -n</code> enable Network Address Translation</li>
	 * <li><code>-serial -s</code> use FT1.2 serial communication</li>
	 * </ul>
	 * For local device management these options are available:
	 * <ul>
	 * <li><code>-emulatewriteenable -e</code> check write-enable of a property</li>
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
	 *
	 * @param args command line options for property client
	 */
	public static void main(String[] args)
	{
		try {
			// read the command line options and run the client
			final Map options = new HashMap();
			if (parseOptions(args, options))
				new PropClient().run(options);
		}
		catch (final Throwable t) {
			if (t.getMessage() != null)
				System.out.println(t.getMessage());
		}
	}

	private void run(Map options) throws KNXException, IOException
	{
		// create the supported user commands for KNX property access
		final Map commands = new HashMap();
		commands.put("get", new GetProperty());
		commands.put("set", new SetProperty());
		commands.put("scan", new ScanProperties());
		commands.put("desc", new GetDescription());
		commands.put("?", new Help());
		commands.put("quit", new Quit());

		try {
			// create a property adapter and supply it to a new client
			pc = new PropertyClient(create(options));
			// check if user supplied a XML resource with property definitions
			if (options.containsKey("defs"))
				PropertyClient.loadDefinitions((String) options.get("defs"));
			definitions = PropertyClient.getDefinitions();

			// show some command info
			((Command) commands.get("?")).execute(null);
			// create reader for user input
			final BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
			String[] args;
			while ((args = readLine(r)) != null) {
				if (args.length > 0) {
					final Command c = (Command) commands.get(args[0]);
					if (c == null)
						System.out.println("unknown command, type ? for help");
					else if (args.length > 1 && args[1].equals("?"))
						c.printHelp();
					else {
						// execute the requested command
						try {
							if (!c.execute(args))
								break;
						}
						catch (final KNXException e) {
							if (!pc.isOpen())
								throw e;
							System.out.println(e.getMessage());
						}
						catch (final KNXIllegalArgumentException e) {
							System.out.println(e.getMessage());
						}
						catch (final NumberFormatException e) {
							System.out.println("invalid number (" + e.getMessage() + ")");
						}
					}
				}
			}
		}
		finally {
			if (pc != null)
				pc.close();
			if (lnk != null)
				lnk.close();
		}
	}

	/**
	 * Creates the property adapter to be used with the property client depending on the
	 * supplied user <code>options</code>.
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
		// add a log writer for the console (System.out), setting a
		// user log level, if specified
		LogManager.getManager().addWriter(null,
			new ConsoleWriter(options.containsKey("verbose")));
		// create local and remote socket address for use in adapter
		final InetSocketAddress local =
			createLocalSocket((InetAddress) options.get("localhost"), (Integer) options
				.get("localport"));
		final InetSocketAddress host =
			new InetSocketAddress((InetAddress) options.get("host"), ((Integer) options
				.get("port")).intValue());
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
		return new KnIPDeviceMgmtAdapter(local, host, options.containsKey("nat"),
			new PropertyListener(), options.containsKey("emulatewrite"));
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
		final PropertyListener l = new PropertyListener();
		// if an authorization key was supplied, the adapter uses
		// connection oriented mode and tries to authenticate
		final byte[] authKey = (byte[]) options.get("authorize");
		if (authKey != null)
			return new RemotePropertyServiceAdapter(lnk, remote, l, authKey);
		return new RemotePropertyServiceAdapter(lnk, remote, l,
			options.containsKey("connect"));
	}

	/**
	 * Writes command prompt and waits for command request from user.
	 * <p>
	 *
	 * @param r input reader
	 * @return array with command and command arguments
	 * @throws IOException on I/O error
	 */
	private String[] readLine(BufferedReader r) throws IOException
	{
		System.out.print("> ");
		final String line = r.readLine();
		return line != null ? split(line) : null;
	}

	private static boolean parseOptions(String[] args, Map options)
		throws KNXFormatException
	{
		if (args.length == 0) {
			System.out.println("A tool for KNX property access");
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
			else if (isOption(arg, "-definitions", "-d"))
				options.put("defs", args[++i]);
			else if (isOption(arg, "-verbose", "-v"))
				options.put("verbose", null);
			else if (isOption(arg, "-localhost", null))
				parseHost(args[++i], true, options);
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
			else if (isOption(arg, "-emulatewriteenable", "-e"))
				options.put("emulatewrite", null);
			else if (isOption(arg, "-connect", "-c"))
				options.put("connect", null);
			else if (isOption(arg, "-authorize", "-a"))
				options.put("authorize", getAuthorizeKey(args[++i]));
			else if (isOption(arg, "-routing", null))
				options.put("routing", null);
			else if (options.containsKey("serial"))
				// add port number/identifier to serial option
				options.put("serial", arg);
			else if (!options.containsKey("host"))
				parseHost(arg, false, options);
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
		sb.append("  -verbose -v             enable verbose status output").append(sep);
		sb.append("  -local -l               local device management").append(sep);
		sb.append("  -remote -r <KNX addr>   remote property service").append(sep);
		sb.append("  -definitions -d <file>  use property definition file").append(sep);
		sb.append("  -localhost <id>         local IP/host name").append(sep);
		sb.append("  -localport <number>     local UDP port (default system assigned)")
			.append(sep);
		sb.append("  -port -p <number>       UDP port on <host> (default ").append(
			KNXnetIPConnection.IP_PORT).append(")").append(sep);
		sb.append("  -nat -n                 enable Network Address Translation").append(
			sep);
		sb.append("  -serial -s              use FT1.2 serial communication").append(sep);
		sb.append(" local DM only:").append(sep);
		sb.append("  -emulatewriteenable -e  check write-enable of a property").append(
			sep);
		sb.append(" remote property service only:").append(sep);
		sb.append("  -routing                use KNXnet/IP routing").append(sep);
		sb.append("  -medium -m <id>         KNX medium [tp0|tp1|p110|p132|rf] "
			+ "(default tp1)").append(sep);
		sb.append("  -connect -c             connection oriented mode").append(sep);
		sb.append("  -authorize -a <key>     authorize key to access KNX device").append(
			sep);
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

	private static void parseHost(String host, boolean local, Map options)
	{
		try {
			options.put(local ? "localhost" : "host", InetAddress.getByName(host));
		}
		catch (final UnknownHostException e) {
			throw new IllegalArgumentException("failed to read host " + host);
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

	private static void printHex(byte[] data, int elements)
	{
		final StringBuffer sb = new StringBuffer();
		sb.append("hex: [");
		for (int i = 0; i < data.length; ++i) {
			final int no = data[i] & 0xff;
			if (no < 0x10)
				sb.append('0');
			sb.append(Integer.toHexString(no));
			if (i > 0 && i % (data.length / elements) == 0)
				sb.append(", ");
		}
		sb.append("]");
		System.out.println(sb);
	}

	private static String[] split(String text)
	{
		final StringTokenizer st = new StringTokenizer(text, " \t");
		final String[] tokens = new String[st.countTokens()];
		for (int i = 0; i < tokens.length; ++i)
			tokens[i] = st.nextToken();
		return tokens;
	}

	private void printDescription(Description d)
	{
		final StringBuffer buf = new StringBuffer();
		buf.append(d.getPropIndex());
		buf.append(" OT " + d.getObjectType());
		buf.append(", OI " + d.getObjectIndex());
		buf.append(", PID " + d.getPID());
		if (definitions != null) {
			Property p;
			if ((p =
				(Property) definitions.get(new PropertyClient.PropertyKey(d
					.getObjectType(), d.getPID()))) != null)
				buf.append(" (" + p.getName() + ")");
			else if ((p =
				(Property) definitions.get(new PropertyClient.PropertyKey(
					PropertyKey.GLOBAL_OBJTYPE, d.getPID()))) != null)
				buf.append(" (" + p.getName() + ")");
		}
		buf.append(", PDT " + (d.getPDT() == -1 ? "-" : Integer.toString(d.getPDT())));
		buf.append(", curr. elems " + d.getCurrentElements());
		buf.append(", max. " + d.getMaxElements());
		buf.append(", r/w access " + d.getReadLevel() + "/" + d.getWriteLevel());
		buf.append(d.isWriteEnabled() ? ", w.enabled" : ", r.only");
		System.out.println(buf);
	}

	private static final class PropertyListener implements PropertyAdapterListener
	{
		PropertyListener()
		{}

		public void adapterClosed(CloseEvent e)
		{
			System.out.println(tool + " quits, " + e.getReason());
			if (!e.isUserRequest())
				System.exit(1);
		}
	}

	private abstract static class Command
	{
		String help;

		abstract boolean execute(String[] args) throws KNXException;

		final void printHelp()
		{
			System.out.println(help);
		}

		int toInt(String number)
		{
			return Integer.decode(number).intValue();
		}
	}

	private final class GetProperty extends Command
	{
		GetProperty()
		{
			help = "get object-idx pid [start-idx elements]";
		}

		boolean execute(String[] args) throws KNXException
		{
			if (args.length < 3 || args.length > 5) {
				System.out.println("sorry, wrong number of arguments");
				return true;
			}
			final int oi = toInt(args[1]);
			final int pid = toInt(args[2]);
			try {
				if (args.length == 3)
					System.out.println(pc.getProperty(oi, pid));
			}
			catch (final KNXException e) {
				printHex(pc.getProperty(oi, pid, 1, 1), 1);
			}
			try {
				if (args.length == 5)
					System.out.println(Arrays.asList(
						pc.getPropertyTranslated(oi, pid, toInt(args[3]), toInt(args[4]))
							.getAllValues()).toString());
			}
			catch (final KNXException e) {
				printHex(pc.getProperty(oi, pid, toInt(args[3]), toInt(args[4])),
					toInt(args[4]));
			}
			return true;
		}
	}

	private final class GetDescription extends Command
	{
		GetDescription()
		{
			help = "desc object-idx pid" + sep + "desc object-idx \"i\" prop-idx";
		}

		boolean execute(String[] args) throws KNXException
		{
			if (args.length == 3)
				printDescription(pc.getDescription(toInt(args[1]), toInt(args[2])));
			else if (args.length == 4 && args[2].equals("i"))
				printDescription(pc.getDescriptionByIndex(toInt(args[1]), toInt(args[3])));
			else
				System.out.println("sorry, wrong number of arguments");
			return true;
		}
	}

	private final class SetProperty extends Command
	{
		SetProperty()
		{
			help = "set object-idx pid [start-idx] string-value" + sep
				+ "set object-idx pid start-idx elements [\"0x\"|\"0\"|\"b\"]data" + sep
				+ "(use hexadecimal format for more than 8 byte data or leading zeros)";
		}

		boolean execute(String[] args) throws KNXException
		{
			if (args.length < 4 || args.length > 6) {
				System.out.println("sorry, wrong number of arguments");
				return true;
			}
			final int cnt = args.length;
			final int oi = toInt(args[1]);
			final int pid = toInt(args[2]);
			if (cnt == 4)
				pc.setProperty(oi, pid, 1, args[3]);
			else if (cnt == 5)
				pc.setProperty(oi, pid, toInt(args[3]), args[4]);
			else if (cnt == 6)
				pc.setProperty(oi, pid, toInt(args[3]), toInt(args[4]),
					toByteArray(args[5]));
			return true;
		}

		private byte[] toByteArray(String s)
		{
			// use of BigXXX equivalent is a bit awkward, for now this is sufficient..
			long l = 0;
			if (s.startsWith("0x") || s.startsWith("0X")) {
				final byte[] d = new byte[(s.length() - 1) / 2];
				int k = (s.length() & 0x01) != 0 ? 3 : 4;
				for (int i = 2; i < s.length(); i = k, k += 2)
					d[(i - 1) / 2] = (byte) Short.parseShort(s.substring(i, k), 16);
				return d;
			}
			else if (s.length() > 1 && s.startsWith("0"))
				l = Long.parseLong(s, 8);
			else if (s.startsWith("b"))
				l = Long.parseLong(s.substring(1), 2);
			else
				l = Long.parseLong(s);
			int i = 0;
			for (long test = l; test != 0; test /= 0x100)
				++i;
			final byte[] d = new byte[i == 0 ? 1 : i];
			for (; i-- > 0; l /= 0x100)
				d[i] = (byte) (l & 0xff);
			return d;
		}
	}

	private final class ScanProperties extends Command
	{
		ScanProperties()
		{
			help = "scan [object-idx] [\"all\" for all object properties]";
		}

		boolean execute(String[] args) throws KNXException
		{
			final int cnt = args.length;
			List l = Collections.EMPTY_LIST;
			if (cnt == 1)
				l = pc.scanProperties(false);
			else if (cnt == 2) {
				if (args[1].equals("all"))
					l = pc.scanProperties(true);
				else
					l = pc.scanProperties(toInt(args[1]), false);
			}
			else if (cnt == 3 && args[2].equals("all"))
				l = pc.scanProperties(toInt(args[1]), true);
			else
				System.out.println("sorry, wrong number of arguments");
			for (final Iterator i = l.iterator(); i.hasNext();) {
				final Description d = (Description) i.next();
				printDescription(d);
			}
			return true;
		}
	}

	private final class Help extends Command
	{
		Help()
		{
			help = "show command help";
		}

		boolean execute(String[] args)
		{
			final StringBuffer buf = new StringBuffer();
			buf.append("commands: get | set | desc | scan | quit (append ? for help)"
				+ sep);
			buf.append("get - read property value(s)" + sep);
			buf.append("set - write property value(s)" + sep);
			buf.append("desc - read one property description" + sep);
			buf.append("scan - read property descriptions" + sep);
			buf.append("quit - quit this program" + sep);
			System.out.print(buf);
			return true;
		}
	}

	private final class Quit extends Command
	{
		Quit()
		{
			help = "close property client and quit";
		}

		boolean execute(String[] args)
		{
			return false;
		}
	}

	private static final class ConsoleWriter extends LogStreamWriter
	{
		ConsoleWriter(boolean verbose)
		{
			super(verbose ? LogLevel.INFO : LogLevel.ERROR, System.out, true);
		}

		public void close()
		{}
	}
}
