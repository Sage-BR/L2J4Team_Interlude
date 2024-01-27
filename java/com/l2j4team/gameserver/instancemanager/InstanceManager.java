package com.l2j4team.gameserver.instancemanager;

import com.l2j4team.gameserver.model.actor.instance.Door;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InstanceManager
{
	private final Map<Integer, Instance> instances;
	
	protected InstanceManager()
	{
		instances = new ConcurrentHashMap<>();
		instances.put(0, new Instance(0));
	}
	
	public void addDoor(int id, Door door)
	{
		if (!instances.containsKey(id) || id == 0)
			return;
		
		instances.get(id).addDoor(door);
	}
	
	public void deleteInstance(int id)
	{
		if (id == 0)
		{
			System.out.println("Attempt to delete instance with id 0.");
			return;
		}
		
		// delete doors
	}
	
	public synchronized Instance createInstance()
	{
		Instance instance = new Instance(InstanceIdFactory.getNextAvailable());
		instances.put(instance.getId(), instance);
		return instance;
	}
	
	public Instance getInstance(int id)
	{
		return instances.get(id);
	}
	
	public static InstanceManager getInstance()
	{
		return SingletonHolder.instance;
	}
	
	private static final class SingletonHolder
	{
		protected static final InstanceManager instance = new InstanceManager();
	}
}