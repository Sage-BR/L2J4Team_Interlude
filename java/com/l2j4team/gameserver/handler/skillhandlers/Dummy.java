package com.l2j4team.gameserver.handler.skillhandlers;

import com.l2j4team.gameserver.handler.ISkillHandler;
import com.l2j4team.gameserver.model.L2Skill;
import com.l2j4team.gameserver.model.WorldObject;
import com.l2j4team.gameserver.model.actor.Creature;
import com.l2j4team.gameserver.model.actor.instance.Player;
import com.l2j4team.gameserver.templates.skills.L2SkillType;

public class Dummy implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.DUMMY,
		L2SkillType.BEAST_FEED,
		L2SkillType.DELUXE_KEY_UNLOCK
	};
	
	@Override
	public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets)
	{
		if (!(activeChar instanceof Player))
			return;
		
		if (skill.getSkillType() == L2SkillType.BEAST_FEED)
		{
			final WorldObject target = targets[0];
			if (target == null)
				return;
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}