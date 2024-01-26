package com.l2j4team.loginserver.network.loginserverpackets;

import com.l2j4team.loginserver.GameServerTable;
import com.l2j4team.loginserver.network.serverpackets.ServerBasePacket;

public class AuthResponse extends ServerBasePacket
{
	public AuthResponse(final int serverId)
	{
		writeC(2);
		writeC(serverId);
		writeS(GameServerTable.getInstance().getServerNames().get(serverId));
	}

	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}
