package com.l2j4team.gameserver.skills.basefuncs;

import com.l2j4team.gameserver.skills.Env;

import com.l2j4team.commons.random.Rnd;

/**
 * @author mkizub
 */
public final class LambdaRnd extends Lambda
{
	private final Lambda _max;
	private final boolean _linear;
	
	public LambdaRnd(Lambda max, boolean linear)
	{
		_max = max;
		_linear = linear;
	}
	
	@Override
	public double calc(Env env)
	{
		return _max.calc(env) * ((_linear) ? Rnd.nextDouble() : Rnd.nextGaussian());
	}
}