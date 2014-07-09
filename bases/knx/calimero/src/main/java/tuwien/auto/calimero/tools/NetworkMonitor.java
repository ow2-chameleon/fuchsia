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

import tuwien.auto.calimero.CloseEvent;
import tuwien.auto.calimero.DataUnitBuilder;
import tuwien.auto.calimero.FrameEvent;
import tuwien.auto.calimero.Settings;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.exception.KNXIllegalArgumentException;
import tuwien.auto.calimero.knxnetip.KNXnetIPConnection;
import tuwien.auto.calimero.link.KNXNetworkMonitor;
import tuwien.auto.calimero.link.KNXNetworkMonitorFT12;
import tuwien.auto.calimero.link.KNXNetworkMonitorIP;
import tuwien.auto.calimero.link.event.LinkListener;
import tuwien.auto.calimero.link.event.MonitorFrameEvent;
import tuwien.auto.calimero.link.medium.KNXMediumSettings;
import tuwien.auto.calimero.link.medium.PLSettings;
import tuwien.auto.calimero.link.medium.RFSettings;
import tuwien.auto.calimero.link.medium.RawFrame;
import tuwien.auto.calimero.link.medium.RawFrameBase;
import tuwien.auto.calimero.link.medium.TPSettings;
import tuwien.auto.calimero.log.LogLevel;
import tuwien.auto.calimero.log.LogManager;
import tuwien.auto.calimero.log.LogStreamWriter;
import tuwien.auto.calimero.log.LogWriter;

/**
 * A tool for Calimero allowing monitoring of KNX network messages.
 * <p>
 * NetworkMonitor is a console based tool implementation allowing a user to track KNX
 * network messages in a KNX network. It allows monitoring access using a KNXnet/IP
 * connection or FT1.2 connection. It shows the necessary interaction with the Calimero
 * API for this particular task. To start monitoring invoke the <code>main</code>-method
 * of this class. Note that by default the network monitor will run with common settings,
 * if not specified otherwise using command line options. Since these settings might be
 * system dependent (for example the local host) and not always predictable, a user may
 * want to specify particular settings using available option flags.
 * <p>
 * The main part of this tool implementation interacts with the type
 * {@link KNXNetworkMonitor}, which offers monitoring access to a KNX network. All
 * monitoring output, as well as occurring problems are written to <code>System.out
 * </code>.
 * <p>
 * To quit a running monitor in the console, use a user interrupt for termination (
 * <code>^C</code> for example).
 *
 * @author B. Malinowsky
 */
public class NetworkMonitor
{
	private static final String tool = "NetworkMonitor";
	private static final String version = "0.2";
	private static final String sep = System.getProperty("line.separator");

	private final Map options;
	private KNXNetworkMonitor m;
	private LogWriter w;

	private final class MonitorListener implements LinkListener
	{
		private MonitorListener()
		{}

		public void indication(FrameEvent e)
		{
			final StringBuffer sb = new StringBuffer();
			sb.append(e.getFrame().toString());
			// since we specified decoding of raw frames in createMonitor(), we
			// can get the decoded raw frame here
			// but note, that on decoding error null is returned
			final RawFrame raw = ((MonitorFrameEvent) e).getRawFrame();
			if (raw != null) {
				sb.append(": ").append(raw.toString());
				if (raw instanceof RawFrameBase) {
					final RawFrameBase f = (RawFrameBase) raw;
					sb.append(": ").append(
						DataUnitBuilder.decode(f.getTPDU(), f.getDestination()));
				}
			}
			System.out.println(sb);
		}

		public void linkClosed(CloseEvent e)
		{}
	}

	/**
	 * Creates a new NetworkMonitor instance using the supplied options.
	 * <p>
	 * See {@link #main(String[])} for a list of options.
	 *
	 * @param args list with options
	 * @param w a log writer, can be <code>null</code>
	 * @throws KNXException on instantiation problems
	 * @throws KNXIllegalArgumentException on unknown/invalid options
	 */
	public NetworkMonitor(String[] args, LogWriter w) throws KNXException
	{
		this.w = w;
		try {
			// read the command line options
			options = new HashMap();
			if (!parseOptions(args, options))
				throw new KNXException("only show usage/version information, abort "
					+ tool);
		}
		catch (final RuntimeException e) {
			throw new KNXIllegalArgumentException(e.getMessage());
		}
	}

	/**
	 * Entry point for running the NetworkMonitor.
	 * <p>
	 * An IP host or port identifier has to be supplied, specifying the endpoint for the
	 * KNX network access.<br>
	 * To show the usage message of this tool on the console, supply the command line
	 * option -help (or -h).<br>
	 * Command line options are treated case sensitive. Available options for network
	 * monitoring:
	 * <ul>
	 * <li><code>-help -h</code> show help message</li>
	 * <li><code>-version</code> show tool/library version and exit</li>
	 * <li><code>-verbose -v</code> enable verbose status output</li>
	 * <li><code>-localhost</code> <i>id</i> &nbsp;local IP/host name</li>
	 * <li><code>-localport</code> <i>number</i> &nbsp;local UDP port (default system
	 * assigned)</li>
	 * <li><code>-port -p</code> <i>number</i> &nbsp;UDP port on host (default 3671)</li>
	 * <li><code>-nat -n</code> enable Network Address Translation</li>
	 * <li><code>-serial -s</code> use FT1.2 serial communication</li>
	 * <li><code>-medium -m</code> <i>id</i> &nbsp;KNX medium [tp0|tp1|p110|p132|rf]
	 * (defaults to tp1)</li>
	 * </ul>
	 *
	 * @param args command line options for network monitoring
	 */
	public static void main(String[] args)
	{
		try {
			final NetworkMonitor m = new NetworkMonitor(args, null);
			// supply a log writer for System.out (console)
			m.w = new ConsoleWriter(m.options.containsKey("verbose"));
			m.run(m.new MonitorListener());
		}
		catch (final Throwable t) {
			if (t.getMessage() != null)
				System.out.println(t.getMessage());
			else
				System.out.println(t.getClass().getName());
		}
	}

	/**
	 * Runs the network monitor.
	 * <p>
	 * This method returns when the network monitor is closed.
	 *
	 * @param l a link listener for monitor events
	 * @throws KNXException on problems on creating monitor or during monitoring
	 */
	public void run(LinkListener l) throws KNXException
	{
		createMonitor(l);
		final Thread sh = registerShutdownHandler();
		// TODO actually, this waiting block is just necessary if we're in console mode
		// to keep the current thread alive and for clean up
		// when invoked externally by a user, an immediate return could save one
		// additional thread (with the requirement to call quit for cleanup)
		try {
			// just wait for the network monitor to quit
			synchronized (this) {
				while (m.isOpen())
					try {
						wait();
					}
					catch (final InterruptedException e) {}
			}
		}
		finally {
			Runtime.getRuntime().removeShutdownHook(sh);
		}
	}

	/**
	 * Quits the network monitor, if running.
	 * <p>
	 */
	public void quit()
	{
		if (m != null && m.isOpen()) {
			m.close();
			synchronized (this) {
				notifyAll();
			}
		}
	}

	/**
	 * Creates a new network monitor using the supplied options.
	 * <p>
	 *
	 * @throws KNXException on problems on monitor creation
	 */
	private void createMonitor(LinkListener l) throws KNXException
	{
		final KNXMediumSettings medium = (KNXMediumSettings) options.get("medium");
		if (options.containsKey("serial")) {
			final String p = (String) options.get("serial");
			try {
				m = new KNXNetworkMonitorFT12(Integer.parseInt(p), medium);
			}
			catch (final NumberFormatException e) {
				m = new KNXNetworkMonitorFT12(p, medium);
			}
		}
		else {
			// create local and remote socket address for monitor link
			final InetSocketAddress local = createLocalSocket((InetAddress) options
				.get("localhost"), (Integer) options.get("localport"));
			final InetSocketAddress host = new InetSocketAddress((InetAddress) options
				.get("host"), ((Integer) options.get("port")).intValue());
			// create the monitor link, based on the KNXnet/IP protocol
			// specify whether network address translation shall be used,
			// and tell the physical medium of the KNX network
			m = new KNXNetworkMonitorIP(local, host, options.containsKey("nat"), medium);
		}
		// add the log writer for monitor log events
		LogManager.getManager().addWriter(m.getName(), w);
		// on console we want to have all possible information, so enable
		// decoding of a received raw frame by the monitor link
		m.setDecodeRawFrames(true);
		// listen to monitor link events
		m.addMonitorListener(l);
		// we always need a link closed notification (even with user supplied listener)
		m.addMonitorListener(new LinkListener() {
			public void indication(FrameEvent e)
			{}

			public void linkClosed(CloseEvent e)
			{
				System.out.println("network monitor exit, " + e.getReason());
				synchronized (NetworkMonitor.this) {
					NetworkMonitor.this.notify();
				}
			}
		});
	}

	private Thread registerShutdownHandler()
	{
		final class ShutdownHandler extends Thread
		{
			ShutdownHandler()
			{}

			public void run()
			{
				System.out.println("shutdown");
				quit();
			}
		}
		final ShutdownHandler sh = new ShutdownHandler();
		Runtime.getRuntime().addShutdownHook(sh);
		return sh;
	}

	/**
	 * Reads all options in the specified array, and puts relevant options into the
	 * supplied options map.
	 * <p>
	 * On options not relevant for doing network monitoring (like <code>help</code>),
	 * this method will take appropriate action (like showing usage information). On
	 * occurrence of such an option, other options will be ignored. On unknown options, an
	 * IllegalArgumentException is thrown.
	 *
	 * @param args array with command line options
	 * @param options map to store options, optionally with its associated value
	 * @return <code>true</code> if the supplied provide enough information to continue
	 *         with monitoring, <code>false</code> otherwise or if the options were
	 *         handled by this method
	 */
	private static boolean parseOptions(String[] args, Map options)
	{
		if (args.length == 0) {
			System.out.println("A tool for monitoring a KNX network");
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
			else if (options.containsKey("serial"))
				// add port number/identifier to serial option
				options.put("serial", arg);
			else if (!options.containsKey("host"))
				parseHost(arg, false, options);
			else
				throw new IllegalArgumentException("unknown option " + arg);
		}
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
		sb.append("  -localhost <id>         local IP/host name").append(sep);
		sb.append(
			"  -localport <number>     local UDP port (default system " + "assigned)")
			.append(sep);
		sb.append("  -port -p <number>       UDP port on host (default ").append(
			KNXnetIPConnection.IP_PORT).append(")").append(sep);
		sb.append("  -nat -n                 enable Network Address Translation").append(
			sep);
		sb.append("  -serial -s              use FT1.2 serial communication").append(sep);
		sb.append(
			"  -medium -m <id>         KNX medium [tp0|tp1|p110|p132|rf] "
				+ "(default tp1)").append(sep);
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

	private static final class ConsoleWriter extends LogStreamWriter
	{
		ConsoleWriter(boolean verbose)
		{
			super(verbose ? LogLevel.TRACE : LogLevel.WARN, System.out, true);
		}

		public void close()
		{}
	}
}
