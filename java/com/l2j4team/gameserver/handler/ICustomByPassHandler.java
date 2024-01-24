package com.l2j4team.gameserver.handler;

import com.l2j4team.gameserver.model.actor.instance.Player;

public interface ICustomByPassHandler
{
	public String[] getByPassCommands();
	
	public void handleCommand(String command, Player player, String parameters);
}