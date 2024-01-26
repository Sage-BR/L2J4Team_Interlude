package com.l2j4team.gameserver.network.serverpackets;

import com.l2j4team.gameserver.model.actor.Creature;

/**
 * @author devScarlet
 */
public class TitleUpdate extends L2GameServerPacket
{
	private final String _title;
	private final int _objectId;

	public TitleUpdate(Creature cha)
	{
		_objectId = cha.getObjectId();
		_title = cha.getTitle();
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xcc);
		writeD(_objectId);
		writeS(_title);
	}
}