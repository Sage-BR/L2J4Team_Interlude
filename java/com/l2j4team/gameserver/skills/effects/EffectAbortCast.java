package com.l2j4team.gameserver.skills.effects;

import com.l2j4team.gameserver.model.L2Effect;
import com.l2j4team.gameserver.skills.Env;
import com.l2j4team.gameserver.templates.skills.L2EffectType;

public class EffectAbortCast extends L2Effect
{
	public EffectAbortCast(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.ABORT_CAST;
	}
	
	@Override
	public boolean onStart()
	{
		if (getEffected() == null || getEffected() == getEffector())
			return false;
		
		if (getEffected().isRaid())
			return false;
		
		getEffected().breakCast();
		return true;
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}