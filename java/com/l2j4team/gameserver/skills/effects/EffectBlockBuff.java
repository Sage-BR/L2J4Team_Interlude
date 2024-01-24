package com.l2j4team.gameserver.skills.effects;

import com.l2j4team.gameserver.model.L2Effect;
import com.l2j4team.gameserver.skills.Env;
import com.l2j4team.gameserver.templates.skills.L2EffectType;

public class EffectBlockBuff extends L2Effect
{
	public EffectBlockBuff(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BLOCK_BUFF;
	}
	
	@Override
	public boolean onStart()
	{
		return true;
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}