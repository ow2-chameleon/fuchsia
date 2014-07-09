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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import tuwien.auto.calimero.CloseEvent;
import tuwien.auto.calimero.FrameEvent;
import tuwien.auto.calimero.KNXListener;
import tuwien.auto.calimero.link.event.LinkListener;
import tuwien.auto.calimero.link.event.NetworkLinkListener;
import tuwien.auto.calimero.log.LogService;

/**
 * Threaded event notifier for network link and monitor.
 * <p>
 * 
 * @author B. Malinowsky
 */
abstract class EventNotifier extends Thread implements KNXListener
{
	// TODO extract event listener (throughout Calimero) and put it into its own class
	
	static interface EventCallback
	{
		/**
		 * Invokes the appropriate listener method with the event contained in this event
		 * callback.
		 * <p>
		 * 
		 * @param l the listener to notify
		 */
		void invoke(LinkListener l);
	}

	static final class Indication implements EventCallback
	{
		private final FrameEvent event;

		Indication(FrameEvent e)
		{
			event = e;
		}

		public void invoke(LinkListener l)
		{
			l.indication(event);
		}
	}

	static final class Confirmation implements EventCallback
	{
		private final FrameEvent event;

		Confirmation(FrameEvent e)
		{
			event = e;
		}

		public void invoke(LinkListener l)
		{
			((NetworkLinkListener) l).confirmation(event);
		}
	}

	static final class Closed implements EventCallback
	{
		private final CloseEvent event;

		Closed(CloseEvent e)
		{
			event = e;
		}

		public void invoke(LinkListener l)
		{
			l.linkClosed(event);
		}
	}

	final LogService logger;
	final Object source;

	// event listeners
	private final List listeners = new ArrayList();
	private List listenersCopy = new ArrayList();

	private final List events = new LinkedList();
	private volatile boolean stop;

	EventNotifier(Object source, LogService logger)
	{
		super("Link notifier");
		this.logger = logger;
		this.source = source;
		setDaemon(true);
		start();
	}

	public final void run()
	{
		while (!stop) {
			try {
				EventCallback ec;
				synchronized (events) {
					while (events.isEmpty())
						events.wait();
					ec = (EventCallback) events.remove(0);
				}
				fire(ec);
			}
			catch (final InterruptedException ignore) {}
		}
		// empty event queue
		synchronized (events) {
			while (!events.isEmpty())
				fire((EventCallback) events.remove(0));
		}
	}

	public abstract void frameReceived(FrameEvent e);

	public void connectionClosed(CloseEvent e)
	{
		addEvent(new Closed(new CloseEvent(source, e.isUserRequest(), e.getReason())));
		quit();
	}

	final void addEvent(EventCallback ec)
	{
		if (!stop) {
			synchronized (events) {
				events.add(ec);
				events.notify();
			}
		}
	}

	final void addListener(LinkListener l)
	{
		if (stop || l == null)
			return;
		synchronized (listeners) {
			if (!listeners.contains(l)) {
				listeners.add(l);
				listenersCopy = new ArrayList(listeners);
			}
			else
				logger.warn("event listener already registered");
		}
	}

	final void removeListener(LinkListener l)
	{
		synchronized (listeners) {
			if (listeners.remove(l))
				listenersCopy = new ArrayList(listeners);
		}
	}

	final void quit()
	{
		if (stop)
			return;
		stop = true;
		interrupt();
		if (currentThread() != this) {
			try {
				join();
			}
			catch (final InterruptedException e) {}
		}
	}

	private void fire(EventCallback ec)
	{
		for (final Iterator i = listenersCopy.iterator(); i.hasNext();) {
			final LinkListener l = (LinkListener) i.next();
			try {
				ec.invoke(l);
			}
			catch (final RuntimeException rte) {
				removeListener(l);
				logger.error("removed event listener", rte);
			}
		}
	}
}
