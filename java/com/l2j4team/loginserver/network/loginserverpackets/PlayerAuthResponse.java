package com.l2j4team.loginserver.network.loginserverpackets;

import com.l2j4team.loginserver.network.serverpackets.ServerBasePacket;

public class PlayerAuthResponse extends ServerBasePacket
{
	public PlayerAuthResponse(final String account, final boolean response)
	{
		writeC(3);
		writeS(account);
		writeC(response ? 1 : 0);
	}
	
	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}
