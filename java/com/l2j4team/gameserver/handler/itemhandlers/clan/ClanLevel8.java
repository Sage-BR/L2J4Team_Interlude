/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2j4team.gameserver.handler.itemhandlers.clan;

import com.l2j4team.gameserver.handler.IItemHandler;
import com.l2j4team.gameserver.model.actor.Playable;
import com.l2j4team.gameserver.model.actor.instance.Player;
import com.l2j4team.gameserver.model.item.instance.ItemInstance;

public class ClanLevel8 implements IItemHandler
{
    private final byte LEVEL = 8;
    
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;

		Player activeChar = (Player) playable;

        if (activeChar.isClanLeader() && activeChar.getClan().getLevel() == 7)
        {
            if (activeChar.getClan().getLevel() == 8)
            {
                activeChar.sendMessage("Your clan is already maximum level!");
                return;
            }
                     
            activeChar.getClan().changeLevel(LEVEL);

            activeChar.getClan().updateClanInDB();            
            activeChar.sendMessage("Your clan has evolved to the level " + LEVEL +".");      
            playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
            activeChar.broadcastUserInfo();
        }
        else
            activeChar.sendMessage("You are not the clan leader or your clan not is level 7.");  

       return;
	}
}