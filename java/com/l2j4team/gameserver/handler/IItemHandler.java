package com.l2j4team.gameserver.handler;

import com.l2j4team.gameserver.model.actor.Playable;
import com.l2j4team.gameserver.model.item.instance.ItemInstance;

import java.util.logging.Logger;

/**
 * Mother class of all itemHandlers.
 */
public interface IItemHandler
{
	public static Logger _log = Logger.getLogger(IItemHandler.class.getName());
	
	/**
	 * Launch task associated to the item.
	 * @param playable L2Playable designating the player
	 * @param item ItemInstance designating the item to use
	 * @param forceUse ctrl hold on item use
	 */
	public void useItem(Playable playable, ItemInstance item, boolean forceUse);
}