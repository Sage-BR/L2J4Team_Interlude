package com.l2j4team.gameserver.communitybbs.Manager;

import com.l2j4team.gameserver.model.actor.instance.Player;

import java.util.StringTokenizer;

public class TopBBSManager extends BaseBBSManager
{
	protected TopBBSManager()
	{
	}

	public static TopBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}

	@Override
	public void parseCmd(String command, Player activeChar)
	{
		if (command.equals("_bbshome"))
		{
			loadStaticHtm("index.htm", activeChar);
		}
		else if (command.startsWith("_bbshome;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();

			loadStaticHtm(st.nextToken(), activeChar);
		}
		else
			super.parseCmd(command, activeChar);
	}

	@Override
	protected String getFolder()
	{
		return "top/";
	}

	private static class SingletonHolder
	{
		protected static final TopBBSManager _instance = new TopBBSManager();
	}
}