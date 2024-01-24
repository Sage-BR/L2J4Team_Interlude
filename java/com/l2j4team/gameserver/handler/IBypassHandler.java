package com.l2j4team.gameserver.handler;

import com.l2j4team.gameserver.model.actor.instance.Player;

/**
 * @author Dwight
 */
public interface IBypassHandler
{
	public boolean handleBypass(String bypass, Player activeChar);
	
	public String[] getBypassHandlersList();
}