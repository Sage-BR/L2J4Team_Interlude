package com.l2j4team.gameserver.model.itemcontainer.listeners;

import com.l2j4team.gameserver.model.actor.Playable;
import com.l2j4team.gameserver.model.item.instance.ItemInstance;

public interface OnEquipListener
{
	public void onEquip(int slot, ItemInstance item, Playable actor);
	
	public void onUnequip(int slot, ItemInstance item, Playable actor);
}