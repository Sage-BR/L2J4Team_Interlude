package com.l2j4team.loginserver.network.gameserverpackets;

import com.l2j4team.loginserver.network.clientpackets.ClientBasePacket;

import java.util.ArrayList;
import java.util.List;

public class PlayerInGame extends ClientBasePacket
{
	private final List<String> _accounts;

	public PlayerInGame(final byte[] decrypt)
	{
		super(decrypt);
		_accounts = new ArrayList<>();
		for (int size = readH(), i = 0; i < size; ++i)
			_accounts.add(readS());
	}

	public List<String> getAccounts()
	{
		return _accounts;
	}
}
