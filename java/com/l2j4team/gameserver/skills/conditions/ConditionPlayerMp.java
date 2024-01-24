package com.l2j4team.gameserver.skills.conditions;

import com.l2j4team.gameserver.skills.Env;

public class ConditionPlayerMp extends Condition
{
	private final int _mp;
	
	public ConditionPlayerMp(int mp)
	{
		_mp = mp;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		return env.getCharacter().getCurrentMp() * 100 / env.getCharacter().getMaxMp() <= _mp;
	}
}