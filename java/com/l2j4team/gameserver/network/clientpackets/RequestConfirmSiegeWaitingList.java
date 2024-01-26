package com.l2j4team.gameserver.network.clientpackets;

import com.l2j4team.gameserver.data.sql.ClanTable;
import com.l2j4team.gameserver.instancemanager.CastleManager;
import com.l2j4team.gameserver.model.actor.instance.Player;
import com.l2j4team.gameserver.model.entity.Castle;
import com.l2j4team.gameserver.model.entity.Siege.SiegeSide;
import com.l2j4team.gameserver.model.pledge.Clan;
import com.l2j4team.gameserver.network.serverpackets.SiegeDefenderList;

public final class RequestConfirmSiegeWaitingList extends L2GameClientPacket
{
	private int _approved;
	private int _castleId;
	private int _clanId;

	@Override
	protected void readImpl()
	{
		_castleId = readD();
		_clanId = readD();
		_approved = readD();
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		// Check if the player has a clan
		if ((player == null) || (player.getClan() == null))
			return;

		final Castle castle = CastleManager.getInstance().getCastleById(_castleId);
		if (castle == null)
			return;

		// Check if leader of the clan who owns the castle?
		if (castle.getOwnerId() != player.getClanId() || !player.isClanLeader())
			return;

		final Clan clan = ClanTable.getInstance().getClan(_clanId);
		if (clan == null)
			return;

		if (!castle.getSiege().isRegistrationOver())
		{
			if (_approved == 1)
			{
				if (castle.getSiege().checkSide(clan, SiegeSide.PENDING))
					castle.getSiege().registerClan(clan, SiegeSide.DEFENDER);
			}
			else
			{
				if (castle.getSiege().checkSides(clan, SiegeSide.PENDING, SiegeSide.DEFENDER))
					castle.getSiege().unregisterClan(clan);
			}
		}

		// Update the defender list
		player.sendPacket(new SiegeDefenderList(castle));
	}
}