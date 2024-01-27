package com.l2j4team.gameserver.network.serverpackets;

import com.l2j4team.gameserver.model.actor.instance.Player;
import com.l2j4team.gameserver.model.actor.instance.Player.TimeStamp;

import java.util.List;
import java.util.stream.Collectors;

public class SkillCoolTime extends L2GameServerPacket
{
	public List<TimeStamp> _reuseTimeStamps;
	
	public SkillCoolTime(Player cha)
	{
		_reuseTimeStamps = cha.getReuseTimeStamps().stream().filter(r -> r.hasNotPassed()).collect(Collectors.toList());
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xc1);
		writeD(_reuseTimeStamps.size()); // list size
		for (TimeStamp ts : _reuseTimeStamps)
		{
			writeD(ts.getSkillId());
			writeD(ts.getSkillLvl());
			writeD((int) ts.getReuse() / 1000);
			writeD((int) ts.getRemaining() / 1000);
		}
	}
}