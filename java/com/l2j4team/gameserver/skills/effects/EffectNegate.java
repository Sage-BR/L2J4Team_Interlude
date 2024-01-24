package com.l2j4team.gameserver.skills.effects;

import com.l2j4team.gameserver.model.L2Effect;
import com.l2j4team.gameserver.model.L2Skill;
import com.l2j4team.gameserver.skills.Env;
import com.l2j4team.gameserver.templates.skills.L2EffectType;
import com.l2j4team.gameserver.templates.skills.L2SkillType;

/**
 * @author Gnat
 */
public class EffectNegate extends L2Effect
{
	public EffectNegate(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.NEGATE;
	}
	
	@Override
	public boolean onStart()
	{
		L2Skill skill = getSkill();
		
		for (int negateSkillId : skill.getNegateId())
		{
			if (negateSkillId != 0)
				getEffected().stopSkillEffects(negateSkillId);
		}
		for (L2SkillType negateSkillType : skill.getNegateStats())
		{
			getEffected().stopSkillEffects(negateSkillType, skill.getNegateLvl());
		}
		return true;
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}