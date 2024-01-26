package com.l2j4team.gameserver.instancemanager;

import java.util.concurrent.atomic.AtomicInteger;

public final class InstanceIdFactory
{
	private static AtomicInteger nextAvailable = new AtomicInteger(1);

	public synchronized static int getNextAvailable()
	{
		return nextAvailable.getAndIncrement();
	}
}