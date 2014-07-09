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
import java.util.HashMap;
import java.util.Map;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.Settings;
import tuwien.auto.calimero.datapoint.Datapoint;
import tuwien.auto.calimero.datapoint.StateDP;
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
import tuwien.auto.calimero.log.LogWriter;
import tuwien.auto.calimero.process.ProcessCommunicator;
import tuwien.auto.calimero.process.ProcessCommunicatorImpl;
import tuwien.auto.calimero.process.ProcessListener;

/**
 * A tool for Calimero allowing simple process communication.
 * <p>
 * ProcComm is a console based tool implementation allowing a user to read or write group
 * values in a KNX network. It supports network access using a KNXnet/IP connection or
 * FT1.2 connection. It shows the necessary interaction with the Calimero API for this
 * particular task. To read or write one value, invoke the <code>main</code>-method of
 * this class. Note that by default the communication will use common settings, if not
 * specified otherwise using command line options. Since these settings might be system
 * dependent (for example the local host) and not always predictable, a user may want to
 * specify particular settings using available option flags.
 * <p>
 * The main part of this tool implementation interacts with the type
 * {@link ProcessCommunicator}, which offers high level access for reading and writing
 * process values. It also shows creation of a {@link KNXNetworkLink}, which is supplied
 * to the process communicator, serving as the link to the KNX network. All read
 * responses, as well as occurring problems are written to <code>System.out
 * </code>.
 *
 * @author B. Malinowsky
 */
public class ProcComm
{
	private static final String tool = "ProcComm";
	private static final String version = "0.2";
	private static final String sep = System.getProperty("line.separator");

	/**
	 * The used process communicator.
	 */
	protected ProcessCommunicator pc;

	// specifies parameters to use for the network link
	private final Map options;
	private LogWriter w;
	private ShutdownHandler sh;

	/**
	 * Creates a new ProcComm instance using the supplied options.
	 * <p>
	 * See {@link #main(String[])} for a list of options.
	 *
	 * @param args list with options
	 * @param w a log writer, might be <code>null</code>
	 * @throws KNXException
	 */
	public ProcComm(String[] args, LogWriter w) throws KNXException
	{
		this.w = w;
		// read the command line options and run the process communicator
		options = new HashMap();
		if (!parseOptions(args, options))
			throw new KNXException("only show usage/version information, " + "abort "
				+ tool);
	}

	/**
	 * Entry point for running ProcComm.
	 * <p>
	 * An IP host or port identifier has to be supplied, specifying the endpoint for the
	 * KNX network access.<br>
	 * To show the usage message of this tool on the console, supply the command line
	 * option -help (or -h).<br>
	 * Command line options are treated case sensitive. Available options for the
	 * communication connection:
	 * <ul>
	 * <li><code>-help -h</code> show help message</li>
	 * <li><code>-version</code> show tool/library version and exit</li>
	 * <li><code>-verbose -v</code> enable verbose status output</li>
	 * <li><code>-localhost</code> <i>id</i> &nbsp;local IP/host name</li>
	 * <li><code>-localport</code> <i>number</i> &nbsp;local UDP port (default system
	 * assigned)</li>
	 * <li><code>-port -p</code> <i>number</i> &nbsp;UDP port on host (default 3671)</li>
	 * <li><code>-nat -n</code> enable Network Address Translation</li>
	 * <li><code>-routing</code> use KNXnet/IP routing</li>
	 * <li><code>-serial -s</code> use FT1.2 serial communication</li>
	 * <li><code>-medium -m</code> <i>id</i> &nbsp;KNX medium [tp0|tp1|p110|p132|rf]
	 * (defaults to tp1)</li>
	 * </ul>
	 * Available commands for process communication:
	 * <ul>
	 * <li><code>read</code> <i>DPT &nbsp;KNX-address</i> &nbsp;read from group
	 * address, using DPT value format</li>
	 * <li><code>write</code> <i>DPT &nbsp;value &nbsp;KNX-address</i> &nbsp;write
	 * to group address, using DPT value format</li>
	 * </ul>
	 * For the more common datapoint types (DPTs) the following name aliases can be used
	 * instead of the general DPT number string:
	 * <ul>
	 * <li><code>switch</code> for DPT 1.001</li>
	 * <li><code>bool</code> for DPT 1.002</li>
	 * <li><code>string</code> for DPT 16.001</li>
	 * <li><code>float</code> for DPT 9.002</li>
	 * <li><code>ucount</code> for DPT 5.010</li>
	 * <li><code>angle</code> for DPT 5.003</li>
	 * </ul>
	 *
	 * @param args command line options for process communication
	 */
	public static void main(String[] args)
	{
		try {
			final ProcComm pc = new ProcComm(args, null);
			// use a log writer for the console (System.out), setting the user
			// specified log level, if any
			pc.w = new ConsoleWriter(pc.options.containsKey("verbose"));
			if (pc.options.containsKey("read") == pc.options.containsKey("write"))
				throw new IllegalArgumentException("do either read or write");
			try {
				pc.run(null);
				pc.readWrite();
			}
			finally {
				pc.quit();
			}
		}
		catch (final Throwable t) {
			if (t.getMessage() != null)
				System.out.println(t.getMessage());
		}
	}

	/**
	 * Runs the process communicator.
	 * <p>
	 * This method immediately returns when the process communicator is running. Call
	 * {@link #quit()} to quit process communication.
	 *
	 * @param l a process event listener, can be <code>null</code>
	 * @throws KNXException on problems creating network link or communication
	 */
	public void run(ProcessListener l) throws KNXException
	{
		// create the network link to the KNX network
		final KNXNetworkLink lnk = createLink();
		LogManager.getManager().addWriter(lnk.getName(), w);
		// create process communicator with the established link
		pc = new ProcessCommunicatorImpl(lnk);
		if (l != null)
			pc.addProcessListener(l);
		registerShutdownHandler();
		// user might specify a response timeout for KNX message
		// answers from the KNX network
		if (options.containsKey("timeout"))
			pc.setResponseTimeout(((Integer) options.get("timeout")).intValue());
	}

	/**
	 * Quits process communication.
	 * <p>
	 * Detaches the network link from the process communicator and closes the link.
	 */
	public void quit()
	{
		if (pc != null) {
			final KNXNetworkLink lnk = pc.detach();
			if (lnk != null)
				lnk.close();
			Runtime.getRuntime().removeShutdownHook(sh);
		}
	}

	/**
	 * Creates the KNX network link to access the network specified in
	 * <code>options</code>.
	 * <p>
	 *
	 * @return the KNX network link
	 * @throws KNXException on problems on link creation
	 */
	private KNXNetworkLink createLink() throws KNXException
	{
		final KNXMediumSettings medium = (KNXMediumSettings) options.get("medium");
		if (options.containsKey("serial")) {
			// create FT1.2 network link
			final String p = (String) options.get("serial");
			try {
				return new KNXNetworkLinkFT12(Integer.parseInt(p), medium);
			}
			catch (final NumberFormatException e) {
				return new KNXNetworkLinkFT12(p, medium);
			}
		}
		// create local and remote socket address for network link
		final InetSocketAddress local = createLocalSocket((InetAddress)
			options.get("localhost"), (Integer) options.get("localport"));
		final InetSocketAddress host = new InetSocketAddress((InetAddress)
			options.get("host"), ((Integer) options.get("port")).intValue());
		final int mode = options.containsKey("routing") ? KNXNetworkLinkIP.ROUTER
			: KNXNetworkLinkIP.TUNNEL;
		return new KNXNetworkLinkIP(mode, local, host, options.containsKey("nat"),
			medium);
	}

	/**
	 * Gets the datapoint type identifier from the <code>options</code>, and maps alias
	 * names of common datapoint types to its datapoint type ID.
	 * <p>
	 * The option map must contain a "dpt" key with value.
	 *
	 * @return datapoint type identifier
	 */
	private String getDPT()
	{
		final String dpt = (String) options.get("dpt");
		if (dpt.equals("switch"))
			return "1.001";
		if (dpt.equals("bool"))
			return "1.002";
		if (dpt.equals("string"))
			return "16.001";
		if (dpt.equals("float"))
			return "9.002";
		if (dpt.equals("ucount"))
			return "5.010";
		if (dpt.equals("angle"))
			return "5.003";
		return dpt;
	}

	private void readWrite() throws KNXException
	{
		// check if we are doing a read or write operation
		final boolean read = options.containsKey("read");
		final GroupAddress main = (GroupAddress) options.get(read ? "read" : "write");
		// encapsulate information into a datapoint
		// this is a convenient way to let the process communicator
		// handle the DPT stuff, so an already formatted string will be returned
		final Datapoint dp = new StateDP(main, "", 0, getDPT());
		if (read)
			System.out.println("read value: " + pc.read(dp));
		else {
			// note, a write to a non existing datapoint might finish successfully,
			// too.. no check for existence or read back of a written value is done
			pc.write(dp, (String) options.get("value"));
			System.out.println("write successful");
		}
	}

	/**
	 * Reads all options in the specified array, and puts relevant options into the
	 * supplied options map.
	 * <p>
	 * On options not relevant for doing process communication (like <code>help</code>),
	 * this method will take appropriate action (like showing usage information). On
	 * occurrence of such an option, other options will be ignored. On unknown options, an
	 * IllegalArgumentException is thrown.
	 *
	 * @param args array with command line options
	 * @param options map to store options, optionally with its associated value
	 * @return <code>true</code> if the supplied provide enough information to continue
	 *         with communication, <code>false</code> otherwise or if the options were
	 *         handled by this method
	 */
	private static boolean parseOptions(String[] args, Map options)
		throws KNXFormatException
	{
		if (args.length == 0) {
			System.out.println("A tool for KNX process communication");
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
			if (isOption(arg, "-verbose", "-v"))
				options.put("verbose", null);
			else if (isOption(arg, "read", null)) {
				if (i + 2 >= args.length)
					break;
				options.put("dpt", args[++i]);
				options.put("read", new GroupAddress(args[++i]));
			}
			else if (isOption(arg, "write", null)) {
				if (i + 3 >= args.length)
					break;
				options.put("dpt", args[++i]);
				options.put("value", args[++i]);
				options.put("write", new GroupAddress(args[++i]));
			}
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
			else if (isOption(arg, "-timeout", "-t"))
				options.put("timeout", Integer.decode(args[++i]));
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
		if (options.containsKey("host") == options.containsKey("serial"))
			throw new IllegalArgumentException("no host or serial port specified");
		return true;
	}

	private static void showUsage()
	{
		final StringBuffer sb = new StringBuffer();
		sb.append("usage: ").append(tool + " [options] <host|port>").append(sep);
		sb.append("options:").append(sep);
		sb.append("  -help -h                show this help message").append(sep);
		sb.append("  -version                show tool/library version and exit").append(
			sep);
		sb.append("  -verbose -v             enable verbose status output").append(sep);
		sb.append("  -localhost <id>         local IP/host name").append(sep);
		sb.append("  -localport <number>     local UDP port (default system assigned)")
			.append(sep);
		sb.append("  -port -p <number>       UDP port on <host> (default ").append(
			KNXnetIPConnection.IP_PORT + ")").append(sep);
		sb.append("  -nat -n                 enable Network Address Translation").append(
			sep);
		sb.append("  -routing                use KNX net/IP routing").append(sep);
		sb.append("  -serial -s              use FT1.2 serial communication").append(sep);
		sb.append("  -medium -m <id>         KNX medium [tp0|tp1|p110|p132|rf] "
			+ "(default tp1)").append(sep);
		sb.append("Available commands for process communication:").append(sep);
		sb.append("  read <DPT> <KNX address>           read from group address")
			.append(sep);
		sb.append("  write <DPT> <value> <KNX address>  write to group address")
			.append(sep);
		sb.append("Additionally recognized name aliases for DPT numbers:").append(sep);
		sb.append("  switch (1.001), bool (1.002), string (16.001)").append(sep)
			.append("  float (9.002), ucount (5.010), angle (5.003)");
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

	/**
	 * Creates a medium settings type for the supplied medium identifier.
	 * <p>
	 *
	 * @param id a medium identifier from command line option
	 * @return medium settings object
	 * @throws KNXIllegalArgumentException on unknown medium identifier
	 */
	private static KNXMediumSettings getMedium(String id)
	{
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

	private final class ShutdownHandler extends Thread
	{
		ShutdownHandler()
		{}

		public void run()
		{
			quit();
		}
	}

	private void registerShutdownHandler()
	{
		Runtime.getRuntime().addShutdownHook(sh = new ShutdownHandler());
	}

	// a log writer writing log events to System.out (i.e console in most cases)
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
