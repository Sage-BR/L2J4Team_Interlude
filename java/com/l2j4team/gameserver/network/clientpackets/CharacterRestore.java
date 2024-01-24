package com.l2j4team.gameserver.network.clientpackets;

import com.l2j4team.gameserver.network.FloodProtectors;
import com.l2j4team.gameserver.network.FloodProtectors.Action;
import com.l2j4team.gameserver.network.serverpackets.CharSelectInfo;

public final class CharacterRestore extends L2GameClientPacket
{
	private int _slot;
	
	@Override
	protected void readImpl()
	{
		_slot = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if (!FloodProtectors.performAction(getClient(), Action.CHARACTER_SELECT))
			return;
		
		getClient().markRestoredChar(_slot);
		
		final CharSelectInfo csi = new CharSelectInfo(getClient().getAccountName(), getClient().getSessionId().playOkID1, 0);
		sendPacket(csi);
		getClient().setCharSelection(csi.getCharInfo());		
	}
}