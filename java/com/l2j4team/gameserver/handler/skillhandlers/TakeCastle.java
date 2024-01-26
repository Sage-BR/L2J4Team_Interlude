package com.l2j4team.gameserver.handler.skillhandlers;

import com.l2j4team.gameserver.handler.ISkillHandler;
import com.l2j4team.gameserver.instancemanager.CastleManager;
import com.l2j4team.gameserver.model.L2Skill;
import com.l2j4team.gameserver.model.WorldObject;
import com.l2j4team.gameserver.model.actor.Creature;
import com.l2j4team.gameserver.model.actor.instance.Player;
import com.l2j4team.gameserver.model.entity.Castle;
import com.l2j4team.gameserver.templates.skills.L2SkillType;

/**
 * @author _drunk_
 */
public class TakeCastle implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.TAKECASTLE
	};

	@Override
	public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets)
	{
		if (activeChar == null || !(activeChar instanceof Player) || (targets.length == 0))
			return;

		final Player player = (Player) activeChar;
		if (!player.isClanLeader())
			return;

		final Castle castle = CastleManager.getInstance().getCastle(player);
		if (castle == null || !player.checkIfOkToCastSealOfRule(castle, true, skill, targets[0]))
			return;

		castle.engrave(player.getClan(), targets[0]);
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}