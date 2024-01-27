package com.l2j4team.gameserver.handler.itemhandlers;

import com.l2j4team.gameserver.handler.IItemHandler;
import com.l2j4team.gameserver.model.actor.Playable;
import com.l2j4team.gameserver.model.actor.instance.Player;
import com.l2j4team.gameserver.model.item.instance.ItemInstance;
import com.l2j4team.gameserver.network.serverpackets.ShowMiniMap;

/**
 * This class provides handling for items that should display a map when double clicked.
 */
public class Maps implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;
		
		playable.sendPacket(new ShowMiniMap(item.getItemId()));
	}
}