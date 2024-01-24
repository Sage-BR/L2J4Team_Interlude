package com.l2j4team.gameserver.network.serverpackets;

import com.l2j4team.gameserver.model.WorldObject;

/**
 * format d
 */
public class Revive extends L2GameServerPacket
{
	private final int _objectId;
	
	public Revive(WorldObject obj)
	{
		_objectId = obj.getObjectId();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x07);
		writeD(_objectId);
	}
}