package com.l2j4team.gameserver.handler.admincommandhandlers;

import com.l2j4team.gameserver.data.SpawnTable;
import com.l2j4team.gameserver.handler.IAdminCommandHandler;
import com.l2j4team.gameserver.instancemanager.RaidBossSpawnManager;
import com.l2j4team.gameserver.model.L2Spawn;
import com.l2j4team.gameserver.model.WorldObject;
import com.l2j4team.gameserver.model.actor.Npc;
import com.l2j4team.gameserver.model.actor.instance.Player;
import com.l2j4team.gameserver.network.L2GameClient;
import com.l2j4team.gameserver.network.L2GameClient.GameClientState;
import com.l2j4team.gameserver.network.SystemMessageId;

/**
 * This class handles following admin commands: - delete = deletes target
 */
public class AdminDelete implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_delete"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		
		if (command.equals("admin_delete"))
		{
			WorldObject target = activeChar.getTarget();
			Player player = null;
			
			if (target != null && target instanceof Player)
			{
				player = (Player) target;
			
					final L2GameClient client = player.getClient();
					// detach the client from the char so that the connection isnt closed in the deleteMe
					player.setClient(null);
					// removing player from the world
					player.deleteMe();
					client.setActiveChar(null);
					client.setState(GameClientState.AUTHED);
				}
			}
			else
				handleDelete(activeChar);
			
			handleDelete(activeChar);
				
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private static void handleDelete(Player activeChar)
	{
		if (activeChar.getAccessLevel().getLevel() < 7)
			return;
		
		WorldObject obj = activeChar.getTarget();
		if (obj != null && obj instanceof Npc)
		{
			Npc target = (Npc) obj;
			
			L2Spawn spawn = target.getSpawn();
			if (spawn != null)
			{
				spawn.setRespawnState(false);
				
				if (RaidBossSpawnManager.getInstance().isDefined(spawn.getNpcId()))
					RaidBossSpawnManager.getInstance().deleteSpawn(spawn, true);
				else
					SpawnTable.getInstance().deleteSpawn(spawn, true);
			}
			target.deleteMe();
			
			activeChar.sendMessage("Deleted " + target.getName() + " from " + target.getObjectId() + ".");
		}
		else
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
	}
}