package com.l2j4team.gameserver.network.clientpackets;

import com.l2j4team.gameserver.model.actor.instance.Player;
import com.l2j4team.gameserver.model.pledge.Clan;
import com.l2j4team.gameserver.model.pledge.SubPledge;
import com.l2j4team.gameserver.network.serverpackets.PledgeShowMemberListAll;

public final class RequestPledgeMemberList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		final Clan clan = activeChar.getClan();
		if (clan == null)
			return;
		
		activeChar.sendPacket(new PledgeShowMemberListAll(clan, 0));
		
		for (SubPledge sp : clan.getAllSubPledges())
			activeChar.sendPacket(new PledgeShowMemberListAll(clan, sp.getId()));
	}
}