package com.l2j4team.gameserver.skills.funcs;

import com.l2j4team.gameserver.skills.Env;
import com.l2j4team.gameserver.skills.Formulas;
import com.l2j4team.gameserver.skills.Stats;
import com.l2j4team.gameserver.skills.basefuncs.Func;

public class FuncMaxCpMul extends Func
{
	static final FuncMaxCpMul _fmcm_instance = new FuncMaxCpMul();
	
	public static Func getInstance()
	{
		return _fmcm_instance;
	}
	
	private FuncMaxCpMul()
	{
		super(Stats.MAX_CP, 0x20, null, null);
	}
	
	@Override
	public void calc(Env env)
	{
		env.mulValue(Formulas.CON_BONUS[env.getCharacter().getCON()]);
	}
}