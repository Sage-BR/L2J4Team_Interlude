package com.l2j4team.gameserver.scripting.tasks;

import com.l2j4team.gameserver.instancemanager.SevenSigns;
import com.l2j4team.gameserver.instancemanager.SevenSignsFestival;
import com.l2j4team.gameserver.scripting.Quest;

import com.l2j4team.commons.concurrent.ThreadPool;

public final class SevenSignsUpdate extends Quest implements Runnable
{
	public SevenSignsUpdate()
	{
		super(-1, "tasks");

		ThreadPool.scheduleAtFixedRate(this, 3600000, 3600000);
	}

	@Override
	public final void run()
	{
		if (!SevenSigns.getInstance().isSealValidationPeriod())
			SevenSignsFestival.getInstance().saveFestivalData(false);

		SevenSigns.getInstance().saveSevenSignsData();
		SevenSigns.getInstance().saveSevenSignsStatus();

		_log.info("SevenSigns: Data has been successfully saved.");
	}
}