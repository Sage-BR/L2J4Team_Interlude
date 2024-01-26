package com.l2j4team.gameserver.skills.effects;

import com.l2j4team.gameserver.model.L2Effect;
import com.l2j4team.gameserver.model.actor.Npc;
import com.l2j4team.gameserver.model.actor.instance.Folk;
import com.l2j4team.gameserver.model.actor.instance.SiegeSummon;
import com.l2j4team.gameserver.network.serverpackets.StartRotation;
import com.l2j4team.gameserver.network.serverpackets.StopRotation;
import com.l2j4team.gameserver.skills.Env;
import com.l2j4team.gameserver.templates.skills.L2EffectType;

public class EffectBluff extends L2Effect
{
	public EffectBluff(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BLUFF;
	}

	@Override
	public boolean onStart()
	{
		if (getEffected() instanceof SiegeSummon || getEffected() instanceof Folk)
			return false;

		if (getEffected() instanceof Npc)
		{
			final Npc npc = (Npc) getEffected();
			if ((npc.getNpcId() == 35062) || npc.isRaid() || npc.isRaidMinion())
				return false;
		}

		getEffected().broadcastPacket(new StartRotation(getEffected().getObjectId(), getEffected().getHeading(), 1, 65535));
		getEffected().broadcastPacket(new StopRotation(getEffected().getObjectId(), getEffector().getHeading(), 65535));
		getEffected().setHeading(getEffector().getHeading());
		return true;
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}