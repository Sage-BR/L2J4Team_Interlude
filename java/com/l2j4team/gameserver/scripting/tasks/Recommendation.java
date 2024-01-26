package com.l2j4team.gameserver.scripting.tasks;

import com.l2j4team.gameserver.model.World;
import com.l2j4team.gameserver.model.actor.instance.Player;
import com.l2j4team.gameserver.network.serverpackets.UserInfo;
import com.l2j4team.gameserver.scripting.ScheduledQuest;

public final class Recommendation extends ScheduledQuest
{
	public Recommendation()
	{
		super(-1, "tasks");
	}

	@Override
	public final void onStart()
	{
		for (Player player : World.getInstance().getPlayers())
		{
			player.restartRecom();
			player.sendPacket(new UserInfo(player));
		}

		_log.config("Recommendation: Recommendation has been reset.");
	}

	@Override
	public final void onEnd()
	{
	}
}