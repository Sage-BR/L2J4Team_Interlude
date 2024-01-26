package com.l2j4team.gameserver.network.clientpackets;

import com.l2j4team.gameserver.model.actor.instance.Player;
import com.l2j4team.gameserver.model.pledge.Clan;
import com.l2j4team.gameserver.network.serverpackets.PledgePowerGradeList;

/**
 * Format: (ch)
 * @author -Wooden-
 */
public final class RequestPledgePowerGradeList extends L2GameClientPacket
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

		final Clan clan = player.getClan();
		if (clan == null)
			return;

		player.sendPacket(new PledgePowerGradeList(clan.getPriviledges().keySet(), clan.getMembers()));
	}
}