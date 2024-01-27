package com.l2j4team.gameserver.network.serverpackets;

import com.l2j4team.gameserver.model.L2Skill;
import com.l2j4team.gameserver.model.actor.instance.Player;

import java.util.Collection;

public class GMViewSkillInfo extends L2GameServerPacket
{
	private final Player _activeChar;
	private final Collection<L2Skill> _skills;
	
	public GMViewSkillInfo(Player cha)
	{
		_activeChar = cha;
		_skills = _activeChar.getSkills().values();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x91);
		writeS(_activeChar.getName());
		writeD(_skills.size());
		
		boolean isDisabled = false;
		if (_activeChar.getClan() != null)
			isDisabled = _activeChar.getClan().getReputationScore() < 0;
		
		for (L2Skill skill : _skills)
		{
			writeD(skill.isPassive() ? 1 : 0);
			writeD(skill.getLevel());
			writeD(skill.getId());
			writeC(isDisabled && skill.isClanSkill() ? 1 : 0);
		}
	}
}