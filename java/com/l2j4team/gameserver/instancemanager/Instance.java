package com.l2j4team.gameserver.instancemanager;

import com.l2j4team.gameserver.model.actor.instance.Door;

import java.util.ArrayList;
import java.util.List;

public class Instance
{
	private final int id;
	private final List<Door> doors;

	public Instance(int id)
	{
		this.id = id;
		doors = new ArrayList<>();
	}

	public void openDoors()
	{
		for (Door door : doors)
			door.openMe();
	}

	public void closeDoors()
	{
		for (Door door : doors)
			door.closeMe();
	}

	public void addDoor(Door door)
	{
		doors.add(door);
	}

	public List<Door> getDoors()
	{
		return doors;
	}

	public int getId()
	{
		return id;
	}
}