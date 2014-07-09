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
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import tuwien.auto.calimero.Settings;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.exception.KNXIllegalArgumentException;
import tuwien.auto.calimero.knxnetip.Discoverer;
import tuwien.auto.calimero.knxnetip.KNXnetIPConnection;
import tuwien.auto.calimero.knxnetip.servicetype.DescriptionResponse;
import tuwien.auto.calimero.knxnetip.servicetype.SearchResponse;
import tuwien.auto.calimero.log.LogLevel;
import tuwien.auto.calimero.log.LogManager;
import tuwien.auto.calimero.log.LogStreamWriter;
import tuwien.auto.calimero.log.LogWriter;

/**
 * A tool for Calimero showing the KNXnet/IP discovery and self description feature.
 * <p>
 * Discover is a console based tool implementation allowing a user to do KNXnet/IP
 * discovery and self description. As the protocol name already implies, this is done
 * using the IP protocol. It shows the necessary interaction with the Calimero API for
 * this particular task. Run the Discovery/Description by invoking the <code>main</code>-
 * method of this class.
 * <p>
 * The main part of this tool implementation interacts with the type {@link Discoverer},
 * which offers discovery and self description features.
 * <p>
 * Discovery and self description responses, as well as errors and problems during
 * discovery/description are written to <code>System.out</code>.
 * <p>
 * To quit a running discovery/description request on the console, use a user interrupt
 * for termination (<code>^C</code> for example).
 *
 * @author B. Malinowsky
 */
public class Discover
{
	private static final String tool = "Discover";
	private static final String version = "0.2";
	private static final String sep = System.getProperty("line.separator");

	private final Discoverer d;
	private final Map options;
	private final LogWriter w;
	private ShutdownHandler sh;

	/**
	 * Creates a new Discover instance using the supplied options.
	 * <p>
	 * See {@link #main(String[])} for a list of options.
	 *
	 * @param args list with options
	 * @param w a log writer, may be <code>null</code>
	 * @throws KNXException on instantiation problems
	 * @throws KNXIllegalArgumentException on unknown/invalid options
	 */
	protected Discover(String[] args, LogWriter w) throws KNXException
	{
		options = new HashMap();
		// read user supplied command line options
		try {
			// if there are options for usage/version supplied, we throw
			// because we won't run then ...
			if (!parseOptions(args, options))
				throw new KNXException("only show usage/version information, "
					+ "abort " + tool);
		}
		catch (final RuntimeException e) {
			throw new KNXIllegalArgumentException(e.getMessage());
		}
		this.w = w;
		if (w != null)
			LogManager.getManager().getLogService(Discoverer.LOG_SERVICE).addWriter(w);

		// create a new discoverer with a (default) local port and specify
		// whether network address translation (NAT) should be used
		final int lp = ((Integer) options.get("localport")).intValue();
		// if a network interface was specified, use an assigned IP for local
		// host
		final NetworkInterface nif = (NetworkInterface) options.get("if");
		final InetAddress local = (InetAddress) (nif != null ? nif.getInetAddresses()
			.nextElement() : null);
		try {
			if (local != null)
				d = new Discoverer(local, lp, options.containsKey("nat"));
			else
				d = new Discoverer(lp, options.containsKey("nat"));
		}
		catch (final KNXException e) {
			if (w != null)
				LogManager.getManager().removeWriter(Discoverer.LOG_SERVICE, w);
			throw e;
		}
	}

	/**
	 * Entry point for running Discover.
	 * <p>
	 * To show usage message of the tool on the console, supply the command line option
	 * -help (or -h).<br>
	 * Command line options are treated case sensitive. Available options for
	 * discovery/self description:
	 * <ul>
	 * <li>no arguments: only show short description and version info</li>
	 * <li><code>-help -h</code> show help message</li>
	 * <li><code>-version</code> show tool/library version and exit</li>
	 * <li><code>-localport</code> <i>number</i> &nbsp;local UDP port (default system
	 * assigned) </li>
	 * <li><code>-nat -n</code> enable Network Address Translation</li>
	 * <li><code>-timeout -t</code> discovery/self description response timeout</li>
	 * <li><code>-search -s</code> start a discovery search</li>
	 * <li><code>-interface -i</code> <i>if-name</i> | <i>ip-address</i> &nbsp;local
	 * multicast network interface for discovery or local host for self description
	 * (default system assigned)</li>
	 * <li><code>-description -d <i>host</i></code> &nbsp;query description from host</li>
	 * <li><code>-serverport -p</code> <i>number</i> &nbsp;server UDP port for
	 * description (defaults to port 3671)</li>
	 * </ul>
	 *
	 * @param args command line options for discovery / self description
	 */
	public static void main(String[] args)
	{
		try {
			// add a log writer for the console (System.out)
			new Discover(args, new ConsoleWriter(true)).run();
		}
		catch (final Throwable t) {
			if (t.getMessage() != null)
				System.out.println(t.getMessage());
		}
	}

	/**
	 * Runs the search or description request.
	 * <p>
	 *
	 * @throws KNXException on errors during search/description
	 */
	public void run() throws KNXException
	{
		// decide whether we're doing a discovery or requesting a description
		final boolean discover = options.containsKey("search");
		registerShutdownHandler(discover);
		// invoke the appropriate method for the selected task
		try {
			if (discover)
				search();
			else
				description();
		}
		catch (final InterruptedException e) {}
		finally {
			if (w != null)
				LogManager.getManager().removeWriter(Discoverer.LOG_SERVICE, w);
			unregisterShutdownHandler();
		}
	}

	/**
	 * Starts a discovery search using the supplied options.
	 * <p>
	 *
	 * @throws KNXException on problem during discovery
	 * @throws InterruptedException
	 */
	private void search() throws KNXException, InterruptedException
	{
		final int timeout = ((Integer) options.get("timeout")).intValue();
		// start the search, using a particular network interface if supplied
		if (options.containsKey("if"))
			d.startSearch(0, (NetworkInterface) options.get("if"), timeout, false);
		else
			d.startSearch(timeout, false);
		int displayed = 0;
		// wait until search finished, and update console 4 times/second with
		// received search responses
		while (d.isSearching()) {
			Thread.sleep(250);
			final SearchResponse[] res = d.getSearchResponses();
			for (; displayed < res.length; ++displayed)
				receivedSearchResponse(res[displayed]);
		}
		final SearchResponse[] res = d.getSearchResponses();
		for (; displayed < res.length; ++displayed)
			receivedSearchResponse(res[displayed]);
	}

	/**
	 * Supplies information about a received search response.
	 * <p>
	 * This default implementation writes information to standard output.
	 *
	 * @param r a search response
	 */
	protected void receivedSearchResponse(SearchResponse r)
	{
		final StringBuffer buf = new StringBuffer();
		buf.append(sep).append("control endpoint ");
		buf.append(r.getControlEndpoint().toString()).append(sep);
		buf.append(r.getDevice().toString());
		buf.append(sep).append(sep).append("supported service families:").append(sep);
		buf.append(r.getServiceFamilies().toString());
		for (int i = buf.indexOf(", "); i != -1; i = buf.indexOf(", "))
			buf.replace(i, i + 2, sep);
		System.out.println(buf);
	}

	/**
	 * Requests a self description using the supplied options.
	 * <p>
	 *
	 * @throws KNXException on problem requesting the description
	 */
	private void description() throws KNXException
	{
		// create socket address of server to request self description from
		final InetSocketAddress host = new InetSocketAddress((InetAddress) options
			.get("host"), ((Integer) options.get("serverport")).intValue());
		final int timeout = ((Integer) options.get("timeout")).intValue();
		// request description
		final DescriptionResponse res = d.getDescription(host, timeout);
		receivedDescriptionResponse(res);
	}

	/**
	 * Supplies information about a received description response.
	 * <p>
	 * This default implementation extracts the information and writes it to the standard
	 * output.
	 * <p>
	 *
	 * @param r a description response
	 */
	protected void receivedDescriptionResponse(DescriptionResponse r)
	{
		final StringBuffer buf = new StringBuffer();
		buf.append(r.getDevice().toString());
		buf.append(sep).append(sep).append("supported service families:").append(sep);
		buf.append(r.getServiceFamilies().toString());
		if (r.getManufacturerData() != null)
			buf.append(sep).append(sep).append(r.getManufacturerData().toString());
		for (int i = buf.indexOf(", "); i != -1; i = buf.indexOf(", "))
			buf.replace(i, i + 2, sep);
		System.out.println(buf);
	}

	/**
	 * Reads all command line options, and puts relevant options into the supplied options
	 * map.
	 * <p>
	 * On options not relevant for doing discovery/description (like <code>help</code>),
	 * this method will take appropriate action (like showing usage information). On
	 * occurrence of such an option, other options will be ignored. On unknown options, an
	 * IllegalArgumentException is thrown.
	 *
	 * @param args array with command line options
	 * @param options map to store options
	 * @return <code>true</code> if the supplied provide enough information to continue
	 *         with discovery or description, <code>false</code> otherwise or if the
	 *         options were handled by this method
	 */
	private static boolean parseOptions(String[] args, Map options)
	{
		if (args.length == 0) {
			System.out.println("A tool for KNXnet/IP router discovery "
				+ "& self description");
			showVersion();
			System.out.println("type -help for help message");
			return false;
		}
		// add defaults
		options.put("localport", new Integer(0));
		options.put("serverport", new Integer(KNXnetIPConnection.IP_PORT));
		options.put("timeout", new Integer(5));

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
			if (isOption(arg, "-localport", null))
				options.put("localport", Integer.decode(args[++i]));
			else if (isOption(arg, "-nat", "-n"))
				options.put("nat", null);
			else if (isOption(arg, "-interface", "-i"))
				options.put("if", getNetworkIF(args[++i]));
			else if (isOption(arg, "-timeout", "-t")) {
				final Integer timeout = Integer.valueOf(args[++i]);
				// a value of 0 means infinite timeout
				if (timeout.intValue() > 0)
					options.put("timeout", timeout);
			}
			else if (isOption(arg, "-search", "-s"))
				options.put("search", null);
			else if (isOption(arg, "-description", "-d"))
				parseHost(args[++i], false, options);
			else if (isOption(arg, "-serverport", "-p"))
				options.put("serverport", Integer.decode(args[++i]));
			else
				throw new IllegalArgumentException("unknown option " + arg);
		}
		return true;
	}

	/**
	 * Gets the network interface through a supplied identifier.
	 * <p>
	 *
	 * @param id network interface identifier, either a IF name or an IP address bound to
	 *        that interface
	 * @return network interface
	 * @throws KNXIllegalArgumentException if no network interface found
	 */
	private static NetworkInterface getNetworkIF(String id)
	{
		try {
			NetworkInterface nif = NetworkInterface.getByName(id);
			if (nif != null)
				return nif;
			nif = NetworkInterface.getByInetAddress(InetAddress.getByName(id));
			if (nif != null)
				return nif;
			throw new KNXIllegalArgumentException(
				"IP address not bound to network interface");
		}
		catch (final UnknownHostException e) {}
		catch (final SocketException e) {}
		throw new KNXIllegalArgumentException("network interface not found");
	}

	private static void showUsage()
	{
		final StringBuffer sb = new StringBuffer();
		sb.append("usage: ").append(tool).append(" [options]").append(sep);
		sb.append("options:").append(sep);
		sb.append("  -help -h                show this help message").append(sep);
		sb.append("  -version                show tool/library version and exit").append(
			sep);
		sb.append("  -localport <number>     local UDP port (default system assigned)")
			.append(sep);
		sb.append("  -nat -n                 enable Network Address Translation").append(
			sep);
		sb.append("  -timeout -t             discovery/description response timeout")
			.append(sep);
		sb.append("  -search -s              start a discovery search").append(sep);
		sb.append("  -interface -i <if-name | ip-address>").append(sep);
		sb.append("      local multicast network interface for discovery or").append(sep);
		sb.append("      local host for self description (default system assigned)")
			.append(sep);
		sb.append("  -description -d <host>  query description from host").append(sep);
		sb.append("  -serverport -p <number> server UDP port for description (default ")
			.append(KNXnetIPConnection.IP_PORT).append(")").append(sep);
		System.out.println(sb);
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

	private static boolean isOption(String arg, String longOpt, String shortOpt)
	{
		return arg.equals(longOpt) || shortOpt != null && arg.equals(shortOpt);
	}

	private static void showVersion()
	{
		System.out.println(tool + " version " + version + " using "
			+ Settings.getLibraryHeader(false));
	}

	private void registerShutdownHandler(boolean isDiscovering)
	{
		sh = new ShutdownHandler(isDiscovering);
		Runtime.getRuntime().addShutdownHook(sh);
	}

	private void unregisterShutdownHandler()
	{
		Runtime.getRuntime().removeShutdownHook(sh);
	}

	private final class ShutdownHandler extends Thread
	{
		private final String s;

		ShutdownHandler(boolean isDiscovering)
		{
			s = isDiscovering ? "stopped discovery search" : "self description canceled";
		}

		public void run()
		{
			System.out.println(s);
			d.stopSearch();
		}
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
