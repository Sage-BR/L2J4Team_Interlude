package com.l2j4team.gameserver.handler.itemhandlers;

import com.l2j4team.Config;
import com.l2j4team.gameserver.handler.IItemHandler;
import com.l2j4team.gameserver.model.ShotType;
import com.l2j4team.gameserver.model.actor.Playable;
import com.l2j4team.gameserver.model.actor.instance.Player;
import com.l2j4team.gameserver.model.holder.IntIntHolder;
import com.l2j4team.gameserver.model.item.instance.ItemInstance;
import com.l2j4team.gameserver.model.item.kind.Weapon;
import com.l2j4team.gameserver.network.SystemMessageId;
import com.l2j4team.gameserver.network.serverpackets.MagicSkillUse;
import com.l2j4team.gameserver.util.Broadcast;

public class SpiritShot implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;
		
		final Player activeChar = (Player) playable;
		final ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		final Weapon weaponItem = activeChar.getActiveWeaponItem();
		final int itemId = item.getItemId();
		
		// Check if sps can be used
		if (weaponInst == null || weaponItem.getSpiritShotCount() == 0)
		{
			if (!activeChar.getAutoSoulShot().contains(itemId))
				activeChar.sendPacket(SystemMessageId.CANNOT_USE_SPIRITSHOTS);
			return;
		}
		
		// Check if sps is already active
		if (activeChar.isChargedShot(ShotType.SPIRITSHOT))
			return;
		
		if (weaponItem.getCrystalType() != item.getItem().getCrystalType())
		{
			if (!activeChar.getAutoSoulShot().contains(itemId))
				activeChar.sendPacket(SystemMessageId.SPIRITSHOTS_GRADE_MISMATCH);
			
			return;
		}
		
		// Consume sps if player has enough of them
		if (!Config.INFINITY_SS && !activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), weaponItem.getSpiritShotCount(), null, false))
		{
			if (!activeChar.disableAutoShot(itemId))
				activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_SPIRITSHOTS);
			return;
		}
		
		final IntIntHolder[] skills = item.getItem().getSkills();
		
		activeChar.sendPacket(SystemMessageId.ENABLED_SPIRITSHOT);
		activeChar.setChargedShot(ShotType.SPIRITSHOT, true);
		Broadcast.toSelfAndKnownPlayersInRadius(activeChar, new MagicSkillUse(activeChar, activeChar, skills[0].getId(), 1, 0, 0), 600);
	}
}