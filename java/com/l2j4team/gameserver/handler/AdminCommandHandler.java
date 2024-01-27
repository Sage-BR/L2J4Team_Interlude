package com.l2j4team.gameserver.handler;

import com.l2j4team.gameserver.handler.admincommandhandlers.AdminAdmin;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminAiox;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminAnnouncements;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminAugment;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminBalancer;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminBan;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminBanHWID;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminBlock;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminBookmark;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminBuffs;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminCTFEngine;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminCamera;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminChatManager;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminClanFull;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminCreateItem;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminCursedWeapons;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminCustom;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminDelete;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminDoorControl;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminEditChar;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminEditNpc;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminEffects;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminEnchant;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminExpSp;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminGeoEngine;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminGiran;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminGm;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminGmChat;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminHeal;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminHelpPage;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminHero;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminInstance;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminInventory;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminInvul;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminKick;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminKnownlist;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminLevel;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminLocPhantom;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminMaintenance;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminMammon;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminManor;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminMenu;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminMovieMaker;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminOlympiad;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminPForge;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminPetition;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminPledge;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminPolymorph;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminRecallAll;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminRes;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminRideWyvern;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminSearch;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminSendDonate;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminShop;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminSiege;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminSkill;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminSpawn;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminTarget;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminTeleport;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminTvTEngine;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminVip;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminZone;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminZoneCreation;

import java.util.HashMap;
import java.util.Map;

public class AdminCommandHandler
{
	private final Map<Integer, IAdminCommandHandler> _datatable = new HashMap<>();
	
	public static AdminCommandHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected AdminCommandHandler()
	{
		registerAdminCommandHandler(new AdminVip());
		registerAdminCommandHandler(new AdminAiox());
		registerAdminCommandHandler(new AdminLocPhantom());
		registerAdminCommandHandler(new AdminBalancer());
		registerAdminCommandHandler(new AdminAdmin());
		registerAdminCommandHandler(new AdminAnnouncements());
		registerAdminCommandHandler(new AdminBan());
		registerAdminCommandHandler(new AdminBookmark());
		registerAdminCommandHandler(new AdminBuffs());
		registerAdminCommandHandler(new AdminCamera());
		registerAdminCommandHandler(new AdminCreateItem());
		registerAdminCommandHandler(new AdminCursedWeapons());
		registerAdminCommandHandler(new AdminDelete());
		registerAdminCommandHandler(new AdminDoorControl());
		registerAdminCommandHandler(new AdminEditChar());
		registerAdminCommandHandler(new AdminEditNpc());
		registerAdminCommandHandler(new AdminEffects());
		registerAdminCommandHandler(new AdminEnchant());
		registerAdminCommandHandler(new AdminExpSp());
		registerAdminCommandHandler(new AdminGeoEngine());
		registerAdminCommandHandler(new AdminGm());
		registerAdminCommandHandler(new AdminGmChat());
		registerAdminCommandHandler(new AdminHeal());
		registerAdminCommandHandler(new AdminHelpPage());
		registerAdminCommandHandler(new AdminInvul());
		registerAdminCommandHandler(new AdminKick());
		registerAdminCommandHandler(new AdminKnownlist());
		registerAdminCommandHandler(new AdminLevel());
		registerAdminCommandHandler(new AdminMaintenance());
		registerAdminCommandHandler(new AdminMammon());
		registerAdminCommandHandler(new AdminManor());
		registerAdminCommandHandler(new AdminMenu());
		registerAdminCommandHandler(new AdminMovieMaker());
		registerAdminCommandHandler(new AdminOlympiad());
		registerAdminCommandHandler(new AdminPetition());
		registerAdminCommandHandler(new AdminPForge());
		registerAdminCommandHandler(new AdminPledge());
		registerAdminCommandHandler(new AdminPolymorph());
		registerAdminCommandHandler(new AdminRes());
		registerAdminCommandHandler(new AdminRideWyvern());
		registerAdminCommandHandler(new AdminShop());
		registerAdminCommandHandler(new AdminSiege());
		registerAdminCommandHandler(new AdminSkill());
		registerAdminCommandHandler(new AdminSpawn());
		registerAdminCommandHandler(new AdminTarget());
		registerAdminCommandHandler(new AdminTeleport());
		registerAdminCommandHandler(new AdminZone());
		
		// custom
		registerAdminCommandHandler(new AdminZoneCreation());
		registerAdminCommandHandler(new AdminInventory());
		registerAdminCommandHandler(new AdminBlock());
		registerAdminCommandHandler(new AdminBanHWID());
		registerAdminCommandHandler(new AdminClanFull());
		registerAdminCommandHandler(new AdminHero());
		registerAdminCommandHandler(new AdminSendDonate());
		registerAdminCommandHandler(new AdminAugment());
		registerAdminCommandHandler(new AdminCustom());
		registerAdminCommandHandler(new AdminTvTEngine());
		registerAdminCommandHandler(new AdminInstance());
		registerAdminCommandHandler(new AdminCTFEngine());
		registerAdminCommandHandler(new AdminGiran());
		registerAdminCommandHandler(new AdminChatManager());
		registerAdminCommandHandler(new AdminRecallAll());
		registerAdminCommandHandler(new AdminSearch());
		
	}
	
	public void registerAdminCommandHandler(IAdminCommandHandler handler)
	{
		for (String id : handler.getAdminCommandList())
			_datatable.put(id.hashCode(), handler);
	}
	
	public IAdminCommandHandler getAdminCommandHandler(String adminCommand)
	{
		String command = adminCommand;
		
		if (adminCommand.indexOf(" ") != -1)
			command = adminCommand.substring(0, adminCommand.indexOf(" "));
		
		return _datatable.get(command.hashCode());
	}
	
	public int size()
	{
		return _datatable.size();
	}
	
	private static class SingletonHolder
	{
		protected static final AdminCommandHandler _instance = new AdminCommandHandler();
	}
}