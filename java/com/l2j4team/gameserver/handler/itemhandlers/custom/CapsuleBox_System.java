package com.l2j4team.gameserver.handler.itemhandlers.custom;

import com.l2j4team.gameserver.handler.IItemHandler;
import com.l2j4team.gameserver.idfactory.IdFactory;
import com.l2j4team.gameserver.model.actor.Playable;
import com.l2j4team.gameserver.model.actor.instance.Player;
import com.l2j4team.gameserver.model.item.instance.ItemInstance;
import com.l2j4team.gameserver.network.serverpackets.MagicSkillUse;

import com.l2j4team.commons.random.Rnd;

import Base.CapsuleBox.CapsuleBoxData;
import Base.CapsuleBox.CapsuleBoxItem;
import Base.CapsuleBox.CapsuleBoxItem.Item;

public class CapsuleBox_System implements IItemHandler {

    @Override
    public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
        if (!(playable instanceof Player))
            return;

        final Player activeChar = (Player) playable;
        final int itemId = item.getItemId();

        CapsuleBoxItem capsuleBoxItem = CapsuleBoxData.getInstance().getCapsuleBoxItemById(itemId);
        if (capsuleBoxItem != null) {
            if (activeChar.getLevel() < capsuleBoxItem.getPlayerLevel()) {
                activeChar.sendMessage("Para Usar Esta Capsule Box Necesitas El LvL." + capsuleBoxItem.getPlayerLevel());
                return;
            }

            ItemInstance toGive = null;
            for (Item boxItem : capsuleBoxItem.getItems()) {
                toGive = new ItemInstance(IdFactory.getInstance().getNextId(), boxItem.getItemId());
                int random = Rnd.get(100);
                if (random < boxItem.getChance()) {
                    if (!toGive.isStackable()) {
                        toGive.setEnchantLevel(boxItem.getEnchantLevel());
                        activeChar.addItem("CapsuleBox", toGive, activeChar, true);
                    } else {
                        activeChar.addItem("CapsuleBox", boxItem.getItemId(), boxItem.getAmount(), activeChar, true);
                    }
                } else {
                    
                }
                MagicSkillUse MSU = new MagicSkillUse(activeChar, activeChar, 2024, 1, 1, 0);
                activeChar.broadcastPacket(MSU);
               
            }
           
        } else {
            activeChar.sendMessage("This Capsule box expired or is invalid!");
        }

        playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
    }
}