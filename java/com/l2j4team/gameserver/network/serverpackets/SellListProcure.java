package com.l2j4team.gameserver.network.serverpackets;

import com.l2j4team.gameserver.instancemanager.CastleManorManager;
import com.l2j4team.gameserver.model.actor.instance.Player;
import com.l2j4team.gameserver.model.item.instance.ItemInstance;
import com.l2j4team.gameserver.model.manor.CropProcure;

import java.util.HashMap;
import java.util.Map;

public class SellListProcure extends L2GameServerPacket
{
	private final Map<ItemInstance, Integer> _sellList;
	
	private final int _money;
	
	public SellListProcure(Player player, int castleId)
	{
		_money = player.getAdena();
		_sellList = new HashMap<>();
		
		for (CropProcure c : CastleManorManager.getInstance().getCropProcure(castleId, false))
		{
			final ItemInstance item = player.getInventory().getItemByItemId(c.getId());
			if (item != null && c.getAmount() > 0)
				_sellList.put(item, c.getAmount());
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xE9);
		writeD(_money);
		writeD(0x00);
		writeH(_sellList.size());
		
		for (Map.Entry<ItemInstance, Integer> itemEntry : _sellList.entrySet())
		{
			final ItemInstance item = itemEntry.getKey();
			
			writeH(item.getItem().getType1());
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD(itemEntry.getValue());
			writeH(item.getItem().getType2());
			writeH(0);
			writeD(0);
		}
	}
}