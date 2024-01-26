package com.l2j4team.gameserver.network.clientpackets;

import com.l2j4team.gameserver.model.actor.instance.Player;

public class RequestRecipeShopMessageSet extends L2GameClientPacket
{
	private static final int MAX_MSG_LENGTH = 29;
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
		if ((player == null) || (_name != null && _name.length() > MAX_MSG_LENGTH))
			return;

		if (player.getCreateList() != null)
			player.getCreateList().setStoreName(_name);
	}
}