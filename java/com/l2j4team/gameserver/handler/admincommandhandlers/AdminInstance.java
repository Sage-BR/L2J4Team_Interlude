package com.l2j4team.gameserver.handler.admincommandhandlers;

import com.l2j4team.gameserver.handler.IAdminCommandHandler;
import com.l2j4team.gameserver.instancemanager.InstanceManager;
import com.l2j4team.gameserver.model.World;
import com.l2j4team.gameserver.model.actor.instance.Player;

import java.util.StringTokenizer;

public class AdminInstance implements IAdminCommandHandler
{

	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_resetmyinstance"))
		{
			activeChar.setInstance(InstanceManager.getInstance().getInstance(0), false);
			activeChar.sendMessage("Your instance is now default");
		}
		else if (command.startsWith("admin_instanceid"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken(); // skip command

			if (!st.hasMoreTokens())
			{
				activeChar.sendMessage("Write the name.");
				return false;
			}

			String target_name = st.nextToken();
			Player player = World.getInstance().getPlayer(target_name);
			if (player == null)
			{
				activeChar.sendMessage("Player is offline");
				return false;
			}

			activeChar.sendMessage("" + target_name + " instance id: " + player.getInstance().getId());
		}
		else if (command.startsWith("admin_getinstance"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken(); // skip command

			if (!st.hasMoreTokens())
			{
				activeChar.sendMessage("Write the name.");
				return false;
			}

			String target_name = st.nextToken();
			Player player = World.getInstance().getPlayer(target_name);
			if (player == null)
			{
				activeChar.sendMessage("Player is offline");
				return false;
			}

			activeChar.setInstance(player.getInstance(), false);
			activeChar.sendMessage("You are with the same instance of player " + target_name);
		}
		return false;
	}

	@Override
	public String[] getAdminCommandList()
	{

		return new String[]
		{
			"admin_resetmyinstance",
			"admin_getinstance",
			"admin_instanceid"
		};
	}

}