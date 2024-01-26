package com.l2j4team.gameserver.model.itemcontainer.listeners;

import com.l2j4team.gameserver.model.actor.Playable;
import com.l2j4team.gameserver.model.item.instance.ItemInstance;

public class StatsListener implements OnEquipListener
{
	private static StatsListener instance = new StatsListener();

	public static StatsListener getInstance()
	{
		return instance;
	}

	@Override
	public void onEquip(int slot, ItemInstance item, Playable playable)
	{
		playable.addStatFuncs(item.getStatFuncs(playable));
	}

	@Override
	public void onUnequip(int slot, ItemInstance item, Playable playable)
	{
		playable.removeStatsByOwner(item);
	}
}