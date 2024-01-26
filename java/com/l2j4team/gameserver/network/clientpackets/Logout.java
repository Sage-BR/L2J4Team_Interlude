package com.l2j4team.gameserver.network.clientpackets;

import com.l2j4team.Config;
import com.l2j4team.events.ArenaTask;
import com.l2j4team.gameserver.instancemanager.SevenSignsFestival;
import com.l2j4team.gameserver.model.actor.instance.Player;
import com.l2j4team.gameserver.model.zone.ZoneId;
import com.l2j4team.gameserver.network.SystemMessageId;
import com.l2j4team.gameserver.network.serverpackets.ActionFailed;
import com.l2j4team.gameserver.taskmanager.AttackStanceTaskManager;

public final class Logout extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;

		if (player.getActiveEnchantItem() != null || player.isLocked())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// Check if player is in Event
		if ((player._inEventTvT || player._inEventCTF) && !player.isGM())
		{
			player.sendMessage("You can't logout during Event.");
			return;
		}

		if ((player.isInStoreMode() && Config.OFFLINE_TRADE_ENABLE) || (player.isCrafting() && Config.OFFLINE_CRAFT_ENABLE))
		{
			player.closeNetConnection(true);
			return;
		}

		if ((player.isInArenaEvent() || player.isArenaProtection()) && ArenaTask.is_started())
		{
			player.sendMessage("You cannot logout while in Tournament Event!");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (player.isInsideZone(ZoneId.NO_RESTART) && !player.isGM() && !player.isInsideZone(ZoneId.PEACE))
		{
			player.sendPacket(SystemMessageId.NO_LOGOUT_HERE);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (AttackStanceTaskManager.getInstance().isInAttackStance(player) && !player.isGM())
		{
			player.sendPacket(SystemMessageId.CANT_LOGOUT_WHILE_FIGHTING);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (player.isFestivalParticipant() && SevenSignsFestival.getInstance().isFestivalInitialized() && !player.isGM())
		{
			player.sendPacket(SystemMessageId.NO_LOGOUT_HERE);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		player.removeFromBossZone();
		player.logout();
	}
}