package com.l2j4team.gameserver.network.clientpackets;

import com.l2j4team.Config;
import com.l2j4team.events.TvT;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminCustom;
import com.l2j4team.gameserver.handler.voicedcommandhandlers.VoicedMission;
import com.l2j4team.gameserver.instancemanager.BotsPreventionManager;
import com.l2j4team.gameserver.instancemanager.BotsPvPPreventionManager;
import com.l2j4team.gameserver.instancemanager.VoteZoneCommands;
import com.l2j4team.gameserver.model.actor.instance.ClassMaster;
import com.l2j4team.gameserver.model.actor.instance.Player;
import com.l2j4team.gameserver.scripting.QuestState;

public class RequestTutorialLinkHtml extends L2GameClientPacket
{
	String _bypass;
	
	@Override
	protected void readImpl()
	{
		_bypass = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		BotsPreventionManager.Link(player, _bypass);
		
		BotsPvPPreventionManager.Link(player, _bypass);
		VoteZoneCommands.Link(player, _bypass);
		TvT.Link(player, _bypass);
		
		ClassMaster.onTutorialLink(player, _bypass);
		AdminCustom.onVIPLink(player, _bypass);
		
		if (Config.ACTIVE_MISSION)
			VoicedMission.linkMission(player, _bypass);
		
		QuestState qs = player.getQuestState("Tutorial");
		if (qs != null)
			qs.getQuest().notifyEvent(_bypass, null, player);
	}
}