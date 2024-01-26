package com.l2j4team.gameserver.network.clientpackets;

import com.l2j4team.gameserver.data.cache.CrestCache;
import com.l2j4team.gameserver.data.cache.CrestCache.CrestType;
import com.l2j4team.gameserver.network.serverpackets.AllyCrest;

public final class RequestAllyCrest extends L2GameClientPacket
{
	private int _crestId;

	@Override
	protected void readImpl()
	{
		_crestId = readD();
	}

	@Override
	protected void runImpl()
	{
		byte[] data = CrestCache.getInstance().getCrest(CrestType.ALLY, _crestId);
		if (data != null)
			sendPacket(new AllyCrest(_crestId, data));
	}
}