package com.l2j4team.gameserver.skills.conditions;

import com.l2j4team.gameserver.skills.Env;
import com.l2j4team.gameserver.taskmanager.GameTimeTaskManager;

/**
 * @author mkizub
 */
public class ConditionGameTime extends Condition
{
	private final boolean _night;

	public ConditionGameTime(boolean night)
	{
		_night = night;
	}

	@Override
	public boolean testImpl(Env env)
	{
		return GameTimeTaskManager.getInstance().isNight() == _night;
	}
}