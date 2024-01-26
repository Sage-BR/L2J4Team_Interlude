package com.l2j4team.gameserver.handler.itemhandlers;

import com.l2j4team.gameserver.handler.IItemHandler;
import com.l2j4team.gameserver.model.actor.Playable;
import com.l2j4team.gameserver.model.actor.instance.Player;
import com.l2j4team.gameserver.model.item.instance.ItemInstance;
import com.l2j4team.gameserver.network.SystemMessageId;
import com.l2j4team.gameserver.network.serverpackets.ChooseInventoryItem;

public class EnchantScrolls implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;

		final Player activeChar = (Player) playable;
		if (activeChar.isCastingNow())
			return;

		if (activeChar.getActiveEnchantItem() == null)
			activeChar.sendPacket(SystemMessageId.SELECT_ITEM_TO_ENCHANT);

		activeChar.setActiveEnchantItem(item);
		activeChar.sendPacket(new ChooseInventoryItem(item.getItemId()));
	}
}
