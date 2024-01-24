package com.l2j4team.gameserver.handler.voicedcommandhandlers;

import com.l2j4team.Config;
import com.l2j4team.L2DatabaseFactory;
import com.l2j4team.gameserver.AutofarmManager;
import com.l2j4team.gameserver.handler.IVoicedCommandHandler;
import com.l2j4team.gameserver.model.actor.instance.Player;
import com.l2j4team.gameserver.network.serverpackets.NpcHtmlMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VoicedAutoFarm implements IVoicedCommandHandler
{
	
	public static final Logger _log = Logger.getLogger(VoicedAutoFarm.class.getName());
	private static final String[] VOICED_COMMANDS = new String[]
	{
		"autofarm"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		
		if (!Config.ENABLE_AUTO_FARM)
		{
			activeChar.sendMessage("Auto Farm Esta Desactivado");
			return false;
		}
		
		if (Config.NEED_VIP)
		{
			if (!activeChar.isVip())
			{
				activeChar.sendMessage("You are not VIP member.");
				return false;
			}
			
		}
		
		if (command.equals("autofarm"))
		{
			
			sendAutoFarmWindow(activeChar);
			
		}
		
		return true;
	}
	
	public static void sendAutoFarmWindow(Player activeChar)
	{
		String autofarmOn = "<button width=38 height=38 back=\"L2UI_NewTex.AutomaticPlay.CombatBTNOff_Over\" fore=\"L2UI_NewTex.AutomaticPlay.CombatBTNON_Normal\" action=\"bypass voiced_farm\" value=\"\">";
		String autofarmOff = "<button width=38 height=38 back=\"L2UI_NewTex.AutomaticPlay.CombatBTNON_Over\" fore=\"L2UI_NewTex.AutomaticPlay.CombatBTNOff_Normal\" action=\"bypass voiced_farm\" value=\"\">";
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(); PreparedStatement statement = con.prepareStatement("SELECT radiusfarm, skillbar, respecthunt FROM Characters_Autofarm WHERE obj_id=?"))
		{
			statement.setInt(1, activeChar.getObjectId());
			try (ResultSet resultSet = statement.executeQuery())
			{
				if (resultSet.next())
				{
					int radiusFarm = resultSet.getInt("radiusfarm");
					int skillBar = resultSet.getInt("skillbar");
					String respecthunt = resultSet.getString("respecthunt");
					String skillBarKey = "F" + (skillBar + 1);
					NpcHtmlMessage html = new NpcHtmlMessage(0);
					html.setFile("data/html/mods/menu/AutoFarm.htm");
					html.replace("%AutoFarmActived%", AutofarmManager.INSTANCE.isAutofarming(activeChar) ? "<img src=\"panel.online\" width=\"16\" height=\"16\">" : "<img src=\"panel.offline\" width=\"16\" height=\"16\">");
					html.replace("%autoFarmButton%", AutofarmManager.INSTANCE.isAutofarming(activeChar) ? autofarmOn : autofarmOff);
					html.replace("%setrespecthunt%", activeChar.isInRespectHunt() ? "checked" : "unable");
					html.replace("%skillbar%", skillBarKey);
					html.replace("%respecthunt%", respecthunt);
					html.replace("%radiusfarm%", String.valueOf(radiusFarm));
					activeChar.sendPacket(html);
				}
			}
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, "Failed to retrieve Autofarm data for character " + activeChar.getObjectId(), e);
		}
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
