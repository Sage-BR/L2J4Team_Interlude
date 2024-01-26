package com.l2j4team.gameserver.skills.conditions;

import com.l2j4team.gameserver.skills.Env;

public class ConditionPlayerHpPercentage extends Condition
{
	private final double _p;

	public ConditionPlayerHpPercentage(double p)
	{
		_p = p;
	}

	@Override
	public boolean testImpl(Env env)
	{
		return env.getCharacter().getCurrentHp() <= env.getCharacter().getMaxHp() * _p;
	}
}
