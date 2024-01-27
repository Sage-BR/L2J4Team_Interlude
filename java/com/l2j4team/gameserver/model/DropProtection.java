/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2j4team.gameserver.model;

import com.l2j4team.Config;
import com.l2j4team.gameserver.model.actor.instance.Pet;
import com.l2j4team.gameserver.model.actor.instance.Player;

import java.util.concurrent.ScheduledFuture;

import com.l2j4team.commons.concurrent.ThreadPool;

/**
 * @author DrHouse
 */
public class DropProtection implements Runnable
{
	private volatile boolean _isProtected = false;
	private Player _owner = null;
	private ScheduledFuture<?> _task = null;
	
	private static final long PROTECTED_MILLIS_TIME = Config.DROP_PROTECTED_TIME * 1000;
	
	@Override
	public synchronized void run()
	{
		_isProtected = false;
		_owner = null;
		_task = null;
	}
	
	public boolean isProtected()
	{
		return _isProtected;
	}
	
	public Player getOwner()
	{
		return _owner;
	}
	
	public synchronized boolean tryPickUp(Player actor)
	{
		if (!_isProtected || (_owner == actor) || (_owner.getParty() != null && _owner.getParty() == actor.getParty()))
			return true;
		
		return false;
	}
	
	public boolean tryPickUp(Pet pet)
	{
		return tryPickUp(pet.getOwner());
	}
	
	public synchronized void unprotect()
	{
		if (_task != null)
			_task.cancel(false);
		
		_isProtected = false;
		_owner = null;
		_task = null;
	}
	
	public synchronized void protect(Player player)
	{
		unprotect();
		
		_isProtected = true;
		
		if ((_owner = player) == null)
			throw new NullPointerException("Trying to protect dropped item to null owner");
		
		_task = ThreadPool.schedule(this, PROTECTED_MILLIS_TIME);
		
	}
}
