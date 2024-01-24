package com.l2j4team.gameserver.handler.usercommandhandlers;

import com.l2j4team.gameserver.handler.IUserCommandHandler;
import com.l2j4team.gameserver.model.actor.instance.Player;

/**
 * Support for /dismount command.
 * @author Micht
 */
public class DisMount implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		62
	};
	
	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if (activeChar.isMounted())
			activeChar.dismount();
		
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}