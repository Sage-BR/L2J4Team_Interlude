package com.l2j4team.gameserver.network.clientpackets;

import com.l2j4team.gameserver.data.RecipeTable;
import com.l2j4team.gameserver.model.actor.instance.Player;
import com.l2j4team.gameserver.model.actor.instance.Player.StoreType;
import com.l2j4team.gameserver.network.FloodProtectors;
import com.l2j4team.gameserver.network.FloodProtectors.Action;

public final class RequestRecipeItemMakeSelf extends L2GameClientPacket
{
	private int _id;
	
	@Override
	protected void readImpl()
	{
		_id = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if (!FloodProtectors.performAction(getClient(), Action.MANUFACTURE))
			return;
		
		final Player activeChar = getClient().getActiveChar();
		if ((activeChar == null) || activeChar.getStoreType() == StoreType.MANUFACTURE || activeChar.isCrafting())
			return;
		
		RecipeTable.getInstance().requestMakeItem(activeChar, _id);
	}
}