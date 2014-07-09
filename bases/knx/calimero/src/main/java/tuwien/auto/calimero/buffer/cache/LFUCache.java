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

import java.util.Comparator;
import java.util.TreeMap;

/**
 * A {@link Cache} using a LFU replacement policy.
 * <p>
 * The usage value of {@link CacheObject#getUsage()} equals the access count,
 * {@link CacheObject#getCount()}.
 * 
 * @author B. Malinowsky
 */
public class LFUCache extends ExpiringCache
{
	private final TreeMap tree;
	private int maxSize;
	private long hits;
	private long misses;
	
	/**
	 * Creates a new LFU cache.
	 * <p>
	 * Optionally, a maximum cache size and an expiring time can be specified.
	 * 
	 * @param cacheSize maximum number of {@link CacheObject}s in the cache, or
	 *        0 for no maximum
	 * @param timeToExpire time in seconds for cache objects to stay valid,
	 *        or 0 for no expiring
	 */
	public LFUCache(int cacheSize, int timeToExpire)
	{
		super(timeToExpire);
		if (cacheSize > 0)
			maxSize = cacheSize;
		tree = new TreeMap(new LFUObjectCompare());
	}

	/**
	 * {@inheritDoc}<br>
	 * If expiring of cache objects is set, and the timestamp of a
	 * {@link CacheObject} is renewed after it has been put into the cache, a
	 * new {@link #put(CacheObject)} is required for that object to apply the
	 * timestamp and keep the cache in a consistent state.
	 */
	public synchronized void put(CacheObject obj)
	{
		// ensure sweeping is on if we have expiring objects
		startSweeper();
		final Object old = map.remove(obj.getKey());
		if (old != null)
			tree.remove(old);
		else
			ensureSizeLimits();
		obj.resetTimestamp();
		map.put(obj.getKey(), obj);
		tree.put(obj, obj);
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.buffer.cache.Cache#get(java.lang.Object)
	 */
	public synchronized CacheObject get(Object key)
	{
		final CacheObject o = (CacheObject) map.get(key);
		if (o != null) {
			tree.remove(o);
			updateAccess(o);
			tree.put(o, o);
			++hits;
		}
		else
			++misses;
		return o;
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.buffer.cache.Cache#remove(java.lang.Object)
	 */
	public synchronized void remove(Object key)
	{
		final Object o = map.remove(key);
		if (o != null)
			tree.remove(o);
	}
	
	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.buffer.cache.Cache#clear()
	 */
	public synchronized void clear()
	{
		stopSweeper();
		map.clear();
		tree.clear();
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.buffer.cache.Cache#statistic()
	 */
	public synchronized Statistic statistic()
	{
		return new StatisticImpl(hits, misses);
	}

	/* (non-Javadoc)
	 * @see tuwien.auto.calimero.buffer.cache.ExpiringCache#notifyRemoved(
	 * tuwien.auto.calimero.buffer.cache.CacheObject)
	 */
	protected final void notifyRemoved(CacheObject obj)
	{
		tree.remove(obj);
	}
	
	private static void updateAccess(CacheObject obj)
	{
		obj.incCount();
		obj.setUsage(obj.getCount());
		// obj.setUsage(obj.getUsage() >> 1 | (1 << 30));
	}

	private void ensureSizeLimits()
	{
		if (maxSize > 0)
			while (map.size() >= maxSize)
				remove(((CacheObject) tree.firstKey()).getKey());
	}

	private static class LFUObjectCompare implements Comparator
	{
		LFUObjectCompare() {}
		public int compare(Object o1, Object o2)
		{
			final CacheObject cmp1 = (CacheObject) o1;
			final CacheObject cmp2 = (CacheObject) o2;
			if (cmp1.getUsage() > cmp2.getUsage())
				return 1;
			if (cmp1.getUsage() < cmp2.getUsage())
				return -1;
			if (cmp1.getCount() > cmp2.getCount())
				return 1;
			if (cmp1.getCount() < cmp2.getCount())
				return -1;
			return 0;
		}
	}
}
