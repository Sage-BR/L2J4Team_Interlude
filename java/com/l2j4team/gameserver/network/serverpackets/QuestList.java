package com.l2j4team.gameserver.network.serverpackets;

import com.l2j4team.gameserver.model.actor.instance.Player;
import com.l2j4team.gameserver.scripting.Quest;
import com.l2j4team.gameserver.scripting.QuestState;

import java.util.List;

public class QuestList extends L2GameServerPacket
{
	private final List<Quest> _quests;
	private final Player _activeChar;
	
	public QuestList(Player player)
	{
		_activeChar = player;
		_quests = player.getAllQuests(true);
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x80);
		writeH(_quests.size());
		for (Quest q : _quests)
		{
			writeD(q.getQuestId());
			QuestState qs = _activeChar.getQuestState(q.getName());
			if (qs == null)
			{
				writeD(0);
				continue;
			}
			
			int states = qs.getInt("__compltdStateFlags");
			if (states != 0)
				writeD(states);
			else
				writeD(qs.getInt("cond"));
		}
	}
}