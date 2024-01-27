package com.l2j4team.gameserver.network.serverpackets;

import com.l2j4team.gameserver.model.actor.instance.Player;

/**
 * @author godson
 */
public class ExOlympiadUserInfo extends L2GameServerPacket
{
	private final int _side;
	private final int _objectId;
	private final String _name;
	private final int _classId;
	private final int _curHp;
	private final int _maxHp;
	private final int _curCp;
	private final int _maxCp;
	
	public ExOlympiadUserInfo(Player player)
	{
		_side = player.getOlympiadSide();
		_objectId = player.getObjectId();
		_name = player.getName();
		_classId = player.getClassId().getId();
		_curHp = (int) player.getCurrentHp();
		_maxHp = player.getMaxHp();
		_curCp = (int) player.getCurrentCp();
		_maxCp = player.getMaxCp();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xfe);
		writeH(0x29);
		writeC(_side);
		writeD(_objectId);
		writeS(_name);
		writeD(_classId);
		writeD(_curHp);
		writeD(_maxHp);
		writeD(_curCp);
		writeD(_maxCp);
	}
}