package com.l2j4team.gameserver.scripting.tasks;

import com.l2j4team.gameserver.data.sql.ClanTable;
import com.l2j4team.gameserver.scripting.ScheduledQuest;

public final class ClansLadder extends ScheduledQuest
{
	public ClansLadder()
	{
		super(-1, "tasks");
	}

	@Override
	public final void onStart()
	{
		ClanTable.getInstance().refreshClansLadder(true);
	}

	@Override
	public final void onEnd()
	{
	}
}