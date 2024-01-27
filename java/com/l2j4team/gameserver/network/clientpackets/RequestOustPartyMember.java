package com.l2j4team.gameserver.network.clientpackets;

import com.l2j4team.gameserver.model.L2Party;
import com.l2j4team.gameserver.model.L2Party.MessageType;
import com.l2j4team.gameserver.model.actor.instance.Player;

public final class RequestOustPartyMember extends L2GameClientPacket
{
	private String _name;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		final L2Party party = player.getParty();
		if (party == null || !party.isLeader(player))
			return;
		
		party.removePartyMember(_name, MessageType.Expelled);
	}
}