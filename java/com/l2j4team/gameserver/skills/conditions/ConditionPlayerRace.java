package com.l2j4team.gameserver.skills.conditions;

import com.l2j4team.gameserver.model.base.ClassRace;
import com.l2j4team.gameserver.skills.Env;

/**
 * @author mkizub
 */
public class ConditionPlayerRace extends Condition
{
	private final ClassRace _race;
	
	public ConditionPlayerRace(ClassRace race)
	{
		_race = race;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (env.getPlayer() == null)
			return false;
		
		return env.getPlayer().getRace() == _race;
	}
}