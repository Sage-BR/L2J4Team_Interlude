package com.l2j4team.gameserver.handler.itemhandlers;

import com.l2j4team.gameserver.handler.IItemHandler;
import com.l2j4team.gameserver.model.actor.Playable;
import com.l2j4team.gameserver.model.actor.instance.Player;
import com.l2j4team.gameserver.model.item.instance.ItemInstance;
import com.l2j4team.gameserver.network.FloodProtectors;
import com.l2j4team.gameserver.network.FloodProtectors.Action;
import com.l2j4team.gameserver.network.SystemMessageId;
import com.l2j4team.gameserver.network.serverpackets.Dice;
import com.l2j4team.gameserver.network.serverpackets.SystemMessage;
import com.l2j4team.gameserver.util.Broadcast;

import com.l2j4team.commons.random.Rnd;

public class RollingDice implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;
		
		final Player activeChar = (Player) playable;
		
		if (!FloodProtectors.performAction(activeChar.getClient(), Action.ROLL_DICE))
		{
			activeChar.sendPacket(SystemMessageId.YOU_MAY_NOT_THROW_THE_DICE_AT_THIS_TIME_TRY_AGAIN_LATER);
			return;
		}
		
		final int number = Rnd.get(1, 6);
		
		Broadcast.toSelfAndKnownPlayers(activeChar, new Dice(activeChar.getObjectId(), item.getItemId(), number, activeChar.getX() - 30, activeChar.getY() - 30, activeChar.getZ()));
		Broadcast.toSelfAndKnownPlayers(activeChar, SystemMessage.getSystemMessage(SystemMessageId.S1_ROLLED_S2).addCharName(activeChar).addNumber(number));
	}
}