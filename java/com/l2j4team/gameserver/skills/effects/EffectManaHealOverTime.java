package com.l2j4team.gameserver.skills.effects;

import com.l2j4team.gameserver.model.L2Effect;
import com.l2j4team.gameserver.network.serverpackets.StatusUpdate;
import com.l2j4team.gameserver.skills.Env;
import com.l2j4team.gameserver.templates.skills.L2EffectType;

class EffectManaHealOverTime extends L2Effect
{
	public EffectManaHealOverTime(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.MANA_HEAL_OVER_TIME;
	}
	
	@Override
	public boolean onActionTime()
	{
		if (getEffected().isDead())
			return false;
		
		double mp = getEffected().getCurrentMp();
		double maxmp = getEffected().getMaxMp();
		mp += calc();
		
		if (mp > maxmp)
			mp = maxmp;
		
		getEffected().setCurrentMp(mp);
		StatusUpdate sump = new StatusUpdate(getEffected());
		sump.addAttribute(StatusUpdate.CUR_MP, (int) mp);
		getEffected().sendPacket(sump);
		return true;
	}
}