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

package tuwien.auto.calimero.buffer.cache;

import tuwien.auto.calimero.exception.KNXIllegalArgumentException;

/**
 * Used to remove expired entries from a cache.
 * <p>
 * The cache sweeper is running in its own thread, waking up for work every
 * sweep time interval set by the user. Then {@link Cache#removeExpired()}
 * is invoked on the specified cache.
 * 
 * @author B. Malinowsky
 * @see Cache
 */
public final class CacheSweeper extends Thread
{
	// interval in seconds
	private volatile int sweepInterval;
	private Cache cache;
	private final Object lock = new Object();

	/**
	 * Creates a {@link CacheSweeper} for <code>cache</code> with the given
	 * <code>sweepInterval</code>.
	 * <p>
	 * 
	 * @param cache the cache for which {@link Cache#removeExpired()} should be
	 *        invoked
	 * @param sweepInterval lapse of time between sweeping in seconds
	 */
	public CacheSweeper(Cache cache, int sweepInterval)
	{
		super("CacheSweeper");
		this.cache = cache;
		setSweepInterval(sweepInterval);
		// priority below normal
		setPriority(3);
		setDaemon(true);
	}

	/**
	 * Sets a new sweep interval.
	 * <p>
	 * If the cache sweeper is in waiting state for next sweep, the new interval
	 * is immediately applied and checked against elapsed time.
	 * 
	 * @param interval new time interval between sweeping in seconds
	 */
	public void setSweepInterval(int interval)
	{
		if (interval <= 0)
			throw new KNXIllegalArgumentException("sweep interval has to be > 0");
		synchronized (lock) {
			sweepInterval = interval;
			lock.notify();
		}
	}

	/**
	 * Returns the time interval between {@link Cache#removeExpired()} calls used by
	 * this cache sweeper.
	 * <p>
	 * 
	 * @return the time in seconds
	 */
	public int getSweepInterval()
	{
		return sweepInterval;
	}

	/**
	 * Stops the sweeper and quits the thread.
	 * <p>
	 */
	public void stopSweeper()
	{
		synchronized (lock) {
			cache = null;
			lock.notify();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run()
	{
		while (true) {
			final long start = System.currentTimeMillis();
			long remaining;
			Cache c;
			synchronized (lock) {
				c = cache;
				remaining = sweepInterval * 1000;
				while (remaining > 0 && cache != null) {
					try {
						lock.wait(remaining);
					}
					catch (final InterruptedException e) {}
					remaining = start + sweepInterval * 1000 - System.currentTimeMillis();
				}
				if (cache == null)
					break;
			}
			c.removeExpired();
		}
	}
}
