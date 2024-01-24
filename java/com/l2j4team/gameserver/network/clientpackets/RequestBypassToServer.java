package com.l2j4team.gameserver.network.clientpackets;

import com.l2j4team.Config;
import com.l2j4team.L2DatabaseFactory;
import com.l2j4team.events.CTF;
import com.l2j4team.events.TvT;
import com.l2j4team.gameserver.BalancerEdit;
import com.l2j4team.gameserver.communitybbs.CommunityBoard;
import com.l2j4team.gameserver.data.ItemTable;
import com.l2j4team.gameserver.data.PlayerNameTable;
import com.l2j4team.gameserver.data.cache.HtmCache;
import com.l2j4team.gameserver.data.sql.ClanTable;
import com.l2j4team.gameserver.data.xml.AdminData;
import com.l2j4team.gameserver.handler.AdminCommandHandler;
import com.l2j4team.gameserver.handler.CustomBypassHandler;
import com.l2j4team.gameserver.handler.IAdminCommandHandler;
import com.l2j4team.gameserver.handler.IVoicedCommandHandler;
import com.l2j4team.gameserver.handler.VoicedCommandHandler;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminBalancer;
import com.l2j4team.gameserver.handler.admincommandhandlers.AdminEditChar;
import com.l2j4team.gameserver.instancemanager.VoteZone;
import com.l2j4team.gameserver.model.L2Skill;
import com.l2j4team.gameserver.model.World;
import com.l2j4team.gameserver.model.WorldObject;
import com.l2j4team.gameserver.model.actor.Npc;
import com.l2j4team.gameserver.model.actor.instance.Gatekeeper;
import com.l2j4team.gameserver.model.actor.instance.OlympiadManagerNpc;
import com.l2j4team.gameserver.model.actor.instance.Player;
import com.l2j4team.gameserver.model.base.Sex;
import com.l2j4team.gameserver.model.entity.Hero;
import com.l2j4team.gameserver.model.item.instance.ItemInstance;
import com.l2j4team.gameserver.model.item.kind.Item;
import com.l2j4team.gameserver.model.item.type.WeaponType;
import com.l2j4team.gameserver.model.olympiad.Olympiad;
import com.l2j4team.gameserver.model.olympiad.OlympiadManager;
import com.l2j4team.gameserver.model.pledge.Clan;
import com.l2j4team.gameserver.model.zone.ZoneId;
import com.l2j4team.gameserver.network.FloodProtectors;
import com.l2j4team.gameserver.network.FloodProtectors.Action;
import com.l2j4team.gameserver.network.SystemMessageId;
import com.l2j4team.gameserver.network.serverpackets.ActionFailed;
import com.l2j4team.gameserver.network.serverpackets.CreatureSay;
import com.l2j4team.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2j4team.gameserver.network.serverpackets.HennaInfo;
import com.l2j4team.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2j4team.gameserver.network.serverpackets.PlaySound;
import com.l2j4team.gameserver.network.serverpackets.SystemMessage;
import com.l2j4team.gameserver.templates.StatsSet;
import com.l2j4team.gameserver.util.ChangeAllyNameLog;
import com.l2j4team.gameserver.util.ChangeClanNameLog;
import com.l2j4team.gameserver.util.ChangeNameLog;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.l2j4team.commons.concurrent.ThreadPool;
import com.l2j4team.commons.lang.StringUtil;
import com.l2j4team.commons.random.Rnd;

import Base.Skin.DressMeData;
import Base.Skin.SkinPackage;

public final class RequestBypassToServer extends L2GameClientPacket
{
	private static final Logger GMAUDIT_LOG = Logger.getLogger("gmaudit");
	
	private String _command;
	
	@Override
	protected void readImpl()
	{
		_command = readS();
	}
	
	static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
	
	@Override
	protected void runImpl()
	{
		if (!FloodProtectors.performAction(getClient(), Action.SERVER_BYPASS) && !_command.startsWith("voiced_getbuff"))
			return;
		
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (_command.isEmpty())
		{
			_log.info(activeChar.getName() + " sent an empty requestBypass packet.");
			activeChar.logout();
			return;
		}
		
		try
		{
			if (_command.startsWith("admin_"))
			{
				String command = _command.split(" ")[0];
				
				IAdminCommandHandler ach = AdminCommandHandler.getInstance().getAdminCommandHandler(command);
				if (ach == null)
				{
					if (activeChar.isGM())
						activeChar.sendMessage("The command " + command.substring(6) + " doesn't exist.");
					
					_log.warning("No handler registered for admin command '" + command + "'");
					return;
				}
				
				if (!AdminData.getInstance().hasAccess(command, activeChar.getAccessLevel()))
				{
					activeChar.sendMessage("You don't have the access rights to use this command.");
					_log.warning(activeChar.getName() + " tried to use admin command " + command + " without proper Access Level.");
					return;
				}
				
				if (Config.GMAUDIT)
					GMAUDIT_LOG.info(activeChar.getName() + " [" + activeChar.getObjectId() + "] used '" + _command + "' command on: " + ((activeChar.getTarget() != null) ? activeChar.getTarget().getName() : "none"));
				
				ach.useAdminCommand(_command, activeChar);
			}
			else if (_command.startsWith("voiced_"))
			{
				String command = _command.split(" ")[0];
				
				IVoicedCommandHandler ach = VoicedCommandHandler.getInstance().getHandler(_command.substring(7));
				
				if (ach == null)
				{
					activeChar.sendMessage("The command " + command.substring(7) + " does not exist!");
					_log.warning("No handler registered for command '" + _command + "'");
					return;
				}
				
				ach.useVoicedCommand(_command.substring(7), activeChar, null);
			}
			else if (_command.startsWith("player_help "))
			{
				playerHelp(activeChar, _command.substring(12));
			}
			else if (_command.startsWith("tele_tournament"))
			{
				if (activeChar.isOlympiadProtection())
				{
					activeChar.sendMessage("Are you participating in the Olympiad..");
					return;
				}
				
				for (Gatekeeper knownChar : activeChar.getKnownTypeInRadius(Gatekeeper.class, 300))
				{
					if (knownChar != null)
					{
						activeChar.teleToLocation(Config.Tournament_locx + Rnd.get(-100, 100), Config.Tournament_locy + Rnd.get(-100, 100), Config.Tournament_locz, 0);
						activeChar.setTournamentTeleport(true);
					}
				}
			}
			
			else if (_command.startsWith("custom_"))
			{
				Player activeChar2 = getClient().getActiveChar();
				CustomBypassHandler.getInstance().handleBypass(activeChar2, _command);
			}
			
			else if (_command.startsWith("dressme"))
			{
				if (!Config.ALLOW_DRESS_ME_IN_OLY && activeChar.isInOlympiadMode())
				{
					activeChar.sendMessage("DressMe can't be used on The Olympiad game.");
					return;
				}
				
				StringTokenizer st = new StringTokenizer(_command, " ");
				st.nextToken();
				if (!st.hasMoreTokens())
				{
					showDressMeMainPage(activeChar);
					return;
				}
				int page = Integer.parseInt(st.nextToken());
				
				if (!st.hasMoreTokens())
				{
					showDressMeMainPage(activeChar);
					return;
				}
				String next = st.nextToken();
				if (next.startsWith("skinlist"))
				{
					String type = st.nextToken();
					showSkinList(activeChar, type, page);
				}
				else if (next.startsWith("myskinlist"))
				{
					
					showMySkinList(activeChar, page);
				}
				if (next.equals("clean"))
				{
					String type = st.nextToken();
					
					if (activeChar.isTryingSkin())
					{
						activeChar.sendMessage("You can't do this while trying a skin.");
						activeChar.sendPacket(new ExShowScreenMessage("You can't do this while trying a skin.", 2000));
						activeChar.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
						
						return;
					}
					
					switch (type.toLowerCase())
					{
						case "all":
							activeChar.setArmorSkinOption(0);
							activeChar.setWeaponSkinOption(0);
							activeChar.setHairSkinOption(0);
							activeChar.setFaceSkinOption(0);
							activeChar.setShieldSkinOption(0);
							showTrySkinHtml(activeChar);
							break;
						case "armor":
							activeChar.setArmorSkinOption(0);
							break;
						case "weapon":
							activeChar.setWeaponSkinOption(0);
							break;
						case "hair":
							activeChar.setHairSkinOption(0);
							showTrySkinHtml(activeChar);
							break;
						case "face":
							activeChar.setFaceSkinOption(0);
							break;
						case "shield":
							activeChar.setShieldSkinOption(0);
							break;
					}
					
					activeChar.broadcastUserInfo();
					
				}
				else if (next.startsWith("buyskin"))
				{
					if (!st.hasMoreTokens())
					{
						showDressMeMainPage(activeChar);
						return;
					}
					
					int skinId = Integer.parseInt(st.nextToken());
					String type = st.nextToken();
					int itemId = Integer.parseInt(st.nextToken());
					if (!activeChar.isInsideZone(ZoneId.TOWN))
					{
						
						activeChar.sendMessage("Solo Puedes Comprar Skin En Ciudad.");
						activeChar.sendPacket(new ExShowScreenMessage("Solo Puedes Comprar Skin En Ciudad.", 2000));
						activeChar.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
						showSkinList(activeChar, type, page);
						return;
					}
					
					SkinPackage sp = null;
					
					switch (type.toLowerCase())
					{
						case "armor":
							sp = DressMeData.getInstance().getArmorSkinsPackage(skinId);
							break;
						case "weapon":
							sp = DressMeData.getInstance().getWeaponSkinsPackage(skinId);
							
							if (activeChar.getActiveWeaponItem() == null)
							{
								activeChar.sendMessage("You can't buy this skin without a weapon.");
								activeChar.sendPacket(new ExShowScreenMessage("You can't buy this skin without a weapon.", 2000));
								activeChar.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
								showSkinList(activeChar, type, page);
								return;
							}
							
							ItemInstance skinWeapon = null;
							if (ItemTable.getInstance().getTemplate(itemId) != null)
							{
								skinWeapon = ItemTable.getInstance().createDummyItem(itemId);
								
								if (!checkWeapons(activeChar, skinWeapon, WeaponType.BOW, WeaponType.BOW) //
									|| !checkWeapons(activeChar, skinWeapon, WeaponType.SWORD, WeaponType.SWORD) //
									|| !checkWeapons(activeChar, skinWeapon, WeaponType.BLUNT, WeaponType.BLUNT) //
									|| !checkWeapons(activeChar, skinWeapon, WeaponType.DAGGER, WeaponType.DAGGER) //
									|| !checkWeapons(activeChar, skinWeapon, WeaponType.POLE, WeaponType.POLE) //
									|| !checkWeapons(activeChar, skinWeapon, WeaponType.DUAL, WeaponType.DUAL) //
									|| !checkWeapons(activeChar, skinWeapon, WeaponType.DUALFIST, WeaponType.DUALFIST) //
									|| !checkWeapons(activeChar, skinWeapon, WeaponType.BIGSWORD, WeaponType.BIGSWORD) //
									|| !checkWeapons(activeChar, skinWeapon, WeaponType.FIST, WeaponType.FIST) //
									|| !checkWeapons(activeChar, skinWeapon, WeaponType.BIGBLUNT, WeaponType.BIGBLUNT))
								{
									activeChar.sendMessage("This skin is not suitable for your weapon type.");
									activeChar.sendPacket(new ExShowScreenMessage("This skin is not suitable for your weapon type.", 2000));
									activeChar.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
									showSkinList(activeChar, type, page);
									return;
								}
							}
							break;
						case "hair":
							sp = DressMeData.getInstance().getHairSkinsPackage(skinId);
							break;
						case "face":
							sp = DressMeData.getInstance().getFaceSkinsPackage(skinId);
							break;
						case "shield":
							sp = DressMeData.getInstance().getShieldSkinsPackage(skinId);
							if (activeChar.getActiveWeaponItem() == null)
							{
								activeChar.sendMessage("You can't buy this skin without a weapon.");
								activeChar.sendPacket(new ExShowScreenMessage("You can't buy this skin without a weapon.", 2000));
								activeChar.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
								showSkinList(activeChar, type, page);
								return;
							}
							
							ItemInstance skinShield = null;
							if (ItemTable.getInstance().getTemplate(itemId) != null)
							{
								skinShield = ItemTable.getInstance().createDummyItem(itemId);
								
								if (!checkWeapons(activeChar, skinShield, WeaponType.BOW, WeaponType.BOW) //
									|| !checkWeapons(activeChar, skinShield, WeaponType.POLE, WeaponType.POLE) //
									|| !checkWeapons(activeChar, skinShield, WeaponType.DUAL, WeaponType.DUAL) //
									|| !checkWeapons(activeChar, skinShield, WeaponType.DUALFIST, WeaponType.DUALFIST) //
									|| !checkWeapons(activeChar, skinShield, WeaponType.BIGSWORD, WeaponType.BIGSWORD) //
									|| !checkWeapons(activeChar, skinShield, WeaponType.FIST, WeaponType.FIST) //
									|| !checkWeapons(activeChar, skinShield, WeaponType.BIGBLUNT, WeaponType.BIGBLUNT))
								{
									activeChar.sendMessage("This skin is not suitable for your weapon type.");
									activeChar.sendPacket(new ExShowScreenMessage("This skin is not suitable for your weapon type.", 2000));
									activeChar.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
									showSkinList(activeChar, type, page);
									return;
								}
							}
							break;
					}
					
					if (sp == null)
					{
						activeChar.sendMessage("There is no such skin.");
						activeChar.sendPacket(new ExShowScreenMessage("There is no such skin.", 2000));
						activeChar.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
						showSkinList(activeChar, type, page);
						return;
					}
					
					if (activeChar.destroyItemByItemId("dressme", sp.getPriceId(), sp.getPriceCount(), activeChar, true))
					{
						activeChar.sendMessage("You have successfully purchased " + sp.getName() + " skin.");
						activeChar.sendPacket(new ExShowScreenMessage("You have successfully purchased " + sp.getName() + " skin.", 2000));
						
						switch (type.toLowerCase())
						{
							case "armor":
								activeChar.buyArmorSkin(skinId);
								activeChar.setArmorSkinOption(skinId);
								break;
							case "weapon":
								activeChar.buyWeaponSkin(skinId);
								activeChar.setWeaponSkinOption(skinId);
								break;
							case "hair":
								activeChar.buyHairSkin(skinId);
								activeChar.setHairSkinOption(skinId);
								break;
							case "face":
								activeChar.buyFaceSkin(skinId);
								activeChar.setFaceSkinOption(skinId);
								break;
							case "shield":
								activeChar.buyShieldSkin(skinId);
								activeChar.setShieldSkinOption(skinId);
								break;
						}
						
						activeChar.broadcastUserInfo();
					}
					showSkinList(activeChar, type, page);
				}
				else if (next.startsWith("tryskin"))
				{
					
					int skinId = Integer.parseInt(st.nextToken());
					
					String type = st.nextToken();
					
					if (activeChar.isTryingSkin())
					{
						activeChar.sendMessage("You are already trying a skin.");
						activeChar.sendPacket(new ExShowScreenMessage("You are already trying a skin.", 2000));
						activeChar.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
						showSkinList(activeChar, type, page);
						return;
					}
					
					activeChar.setIsTryingSkin(true);
					
					int oldArmorSkinId = activeChar.getArmorSkinOption();
					int oldWeaponSkinId = activeChar.getWeaponSkinOption();
					int oldHairSkinId = activeChar.getHairSkinOption();
					int oldFaceSkinId = activeChar.getFaceSkinOption();
					int oldShieldSkinId = activeChar.getShieldSkinOption();
					
					switch (type.toLowerCase())
					{
						case "armor":
							activeChar.setArmorSkinOption(skinId);
							break;
						case "weapon":
							activeChar.setWeaponSkinOption(skinId);
							break;
						case "hair":
							activeChar.setHairSkinOption(skinId);
							break;
						case "face":
							activeChar.setFaceSkinOption(skinId);
							break;
						case "shield":
							
							activeChar.setShieldSkinOption(skinId);
							
							break;
					}
					
					activeChar.broadcastUserInfo();
					showSkinList(activeChar, type, page);
					
					ThreadPool.schedule(() -> {
						switch (type.toLowerCase())
						{
							case "armor":
								activeChar.setArmorSkinOption(oldArmorSkinId);
								break;
							case "weapon":
								activeChar.setWeaponSkinOption(oldWeaponSkinId);
								break;
							case "hair":
								activeChar.setHairSkinOption(oldHairSkinId);
								break;
							case "face":
								activeChar.setFaceSkinOption(oldFaceSkinId);
								break;
							case "shield":
								activeChar.setShieldSkinOption(oldShieldSkinId);
								break;
						}
						
						activeChar.broadcastUserInfo();
						activeChar.setIsTryingSkin(false);
					}, 5000);
				}
				else if (next.startsWith("setskin"))
				{
					int id = Integer.parseInt(st.nextToken());
					String type = st.nextToken();
					int itemId = Integer.parseInt(st.nextToken());
					
					if (activeChar.isTryingSkin())
					{
						activeChar.sendMessage("You can't do this while trying skins.");
						activeChar.sendPacket(new ExShowScreenMessage("You can't do this while trying skins.", 2000));
						activeChar.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
						showTrySkinHtml(activeChar);
						return;
					}
					
					if (Config.VIP_SKINS)
					{
						if (!activeChar.isVip())
						{
							activeChar.sendMessage("Tienes Que Ser Vip Para Usar Skins.");
							activeChar.sendPacket(new ExShowScreenMessage("Tienes Que Ser Vip Para Usar Skins.", 2000));
							activeChar.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
							showTrySkinHtml(activeChar);
							return;
						}
						
					}
					
					if (!activeChar.isInsideZone(ZoneId.TOWN))
					{
						
						activeChar.sendMessage("Solo Puedes Equiparte La Skin En Ciudad.");
						activeChar.sendPacket(new ExShowScreenMessage("Solo Puedes Equiparte La Skin En Ciudad.", 2000));
						activeChar.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
						showTrySkinHtml(activeChar);
						return;
					}
					
					if (type.toLowerCase().contains("armor") && activeChar.hasEquippedArmorSkin(String.valueOf(id)) || type.toLowerCase().contains("weapon") && activeChar.hasEquippedWeaponSkin(String.valueOf(id)) || type.toLowerCase().contains("hair") && activeChar.hasEquippedHairSkin(String.valueOf(id)) || type.toLowerCase().contains("face") && activeChar.hasEquippedFaceSkin(String.valueOf(id)))
					{
						activeChar.sendMessage("You are already equipped this skin.");
						activeChar.sendPacket(new ExShowScreenMessage("You are already equipped this skin.", 2000));
						activeChar.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
						showTrySkinHtml(activeChar);
						return;
					}
					
					switch (type.toLowerCase())
					{
						case "all":
							activeChar.setArmorSkinOption(id);
							activeChar.setHairSkinOption(id);
							showTrySkinHtml(activeChar);
							break;
						case "armor":
							activeChar.setArmorSkinOption(id);
							showTrySkinHtml(activeChar);
							break;
						case "weapon":
							if (activeChar.getActiveWeaponItem() == null)
							{
								activeChar.sendMessage("You can't use this skin without a weapon.");
								activeChar.sendPacket(new ExShowScreenMessage("You can't use this skin without a weapon.", 2000));
								activeChar.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
								showTrySkinHtml(activeChar);
								return;
							}
							
							ItemInstance skinWeapon = null;
							if (ItemTable.getInstance().getTemplate(itemId) != null)
							{
								skinWeapon = ItemTable.getInstance().createDummyItem(itemId);
								
								if (!checkWeapons(activeChar, skinWeapon, WeaponType.BOW, WeaponType.BOW) //
									|| !checkWeapons(activeChar, skinWeapon, WeaponType.SWORD, WeaponType.SWORD) //
									|| !checkWeapons(activeChar, skinWeapon, WeaponType.BLUNT, WeaponType.BLUNT) //
									|| !checkWeapons(activeChar, skinWeapon, WeaponType.DAGGER, WeaponType.DAGGER) //
									|| !checkWeapons(activeChar, skinWeapon, WeaponType.POLE, WeaponType.POLE) //
									|| !checkWeapons(activeChar, skinWeapon, WeaponType.DUAL, WeaponType.DUAL) //
									|| !checkWeapons(activeChar, skinWeapon, WeaponType.DUALFIST, WeaponType.DUALFIST) //
									|| !checkWeapons(activeChar, skinWeapon, WeaponType.BIGSWORD, WeaponType.BIGSWORD) //
									|| !checkWeapons(activeChar, skinWeapon, WeaponType.FIST, WeaponType.FIST) //
									|| !checkWeapons(activeChar, skinWeapon, WeaponType.BIGBLUNT, WeaponType.BIGBLUNT))
								{
									activeChar.sendMessage("This skin is not suitable for your weapon type.");
									activeChar.sendPacket(new ExShowScreenMessage("This skin is not suitable for your weapon type.", 2000));
									activeChar.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
									
									return;
								}
								
								activeChar.setWeaponSkinOption(id);
								showTrySkinHtml(activeChar);
							}
							break;
						case "hair":
							activeChar.setHairSkinOption(id);
							showTrySkinHtml(activeChar);
							break;
						case "face":
							activeChar.setFaceSkinOption(id);
							showTrySkinHtml(activeChar);
							break;
						case "shield":
							if (activeChar.getActiveWeaponItem() == null)
							{
								activeChar.sendMessage("You can't use this skin without a weapon.");
								activeChar.sendPacket(new ExShowScreenMessage("You can't use this skin without a weapon.", 2000));
								activeChar.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
								showTrySkinHtml(activeChar);
								return;
							}
							
							ItemInstance skinShield = null;
							if (ItemTable.getInstance().getTemplate(itemId) != null)
							{
								skinShield = ItemTable.getInstance().createDummyItem(itemId);
								
								if (!checkWeapons(activeChar, skinShield, WeaponType.BOW, WeaponType.BOW) //
									|| !checkWeapons(activeChar, skinShield, WeaponType.POLE, WeaponType.POLE) //
									|| !checkWeapons(activeChar, skinShield, WeaponType.DUAL, WeaponType.DUAL) //
									|| !checkWeapons(activeChar, skinShield, WeaponType.DUALFIST, WeaponType.DUALFIST) //
									|| !checkWeapons(activeChar, skinShield, WeaponType.BIGSWORD, WeaponType.BIGSWORD) //
									|| !checkWeapons(activeChar, skinShield, WeaponType.FIST, WeaponType.FIST) //
									|| !checkWeapons(activeChar, skinShield, WeaponType.BIGBLUNT, WeaponType.BIGBLUNT))
								{
									activeChar.sendMessage("This skin is not suitable for your weapon type.");
									activeChar.sendPacket(new ExShowScreenMessage("This skin is not suitable for your weapon type.", 2000));
									activeChar.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
									
									return;
								}
								
								activeChar.setShieldSkinOption(id);
								showTrySkinHtml(activeChar);
							}
							
							break;
					}
					
					activeChar.broadcastUserInfo();
					
				}
				
			}
			
			else if (_command.startsWith("npc_"))
			{
				if (!activeChar.validateBypass(_command))
					return;
				
				int endOfId = _command.indexOf('_', 5);
				String id;
				if (endOfId > 0)
					id = _command.substring(4, endOfId);
				else
					id = _command.substring(4);
				
				try
				{
					final WorldObject object = World.getInstance().getObject(Integer.parseInt(id));
					if (_command.substring(endOfId + 1).startsWith("tvt_player_join "))
					{
						final String teamName = _command.substring(endOfId + 1).substring(16);
						
						if (TvT.is_joining())
							TvT.addPlayer(activeChar, teamName);
						else
							activeChar.sendMessage("The event is already started. You can not join now!");
					}
					else if (_command.substring(endOfId + 1).startsWith("tvt_player_leave"))
					{
						if (TvT.is_joining())
							TvT.removePlayer(activeChar);
						else
							activeChar.sendMessage("The event is already started. You can not leave now!");
					}
					else if (_command.substring(endOfId + 1).startsWith("tvt_watch"))
					{
						if (activeChar._inEventTvT)
							return;
						else if (TvT.is_teleport() || TvT.is_started())
						{
							activeChar.setEventObserver(true);
							activeChar.enterTvTObserverMode(Config.TVT_OBSERVER_X, Config.TVT_OBSERVER_Y, Config.TVT_OBSERVER_Z);
						}
						else
							activeChar.sendMessage("The event is Is offline.");
					}
					else if (_command.substring(endOfId + 1).startsWith("ctf_watch"))
					{
						if (activeChar._inEventCTF)
							return;
						else if (CTF.is_teleport() || CTF.is_started())
						{
							activeChar.setEventObserver(true);
							activeChar.enterTvTObserverMode(Config.CTF_OBSERVER_X, Config.CTF_OBSERVER_Y, Config.CTF_OBSERVER_Z);
						}
						else
							activeChar.sendMessage("The event is Is offline.");
					}
					else if (_command.substring(endOfId + 1).startsWith("ctf_player_join "))
					{
						final String teamName = _command.substring(endOfId + 1).substring(16);
						
						if (CTF.is_joining())
							CTF.addPlayer(activeChar, teamName);
						else
							activeChar.sendMessage("The event is already started. You can not join now!");
					}
					else if (_command.substring(endOfId + 1).startsWith("ctf_player_leave"))
					{
						if (CTF.is_joining())
							CTF.removePlayer(activeChar);
						else
							activeChar.sendMessage("The event is already started. You can not leave now!");
					}
					
					else if (object != null && object instanceof Npc && endOfId > 0 && ((Npc) object).canInteract(activeChar))
						((Npc) object).onBypassFeedback(activeChar, _command.substring(endOfId + 1));
					
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				}
				catch (NumberFormatException nfe)
				{
				}
			}
			else if (_command.startsWith("droplist"))
			{
				StringTokenizer st = new StringTokenizer(_command, " ");
				st.nextToken();
				
				int npcId = Integer.parseInt(st.nextToken());
				int page = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
				
				Npc.sendNpcDrop(activeChar, npcId, page);
			}
			else if (_command.startsWith("pvpzone"))
			{
				if (!activeChar.isGM())
					return;
				
				if (VoteZone.is_zone_1())
					activeChar.teleToLocation(Config.ZONE_1X + Rnd.get(-80, 80), Config.ZONE_1Y + Rnd.get(-80, 80), Config.ZONE_1Z, 0);
				else if (VoteZone.is_zone_2())
					activeChar.teleToLocation(Config.ZONE_2X + Rnd.get(-80, 80), Config.ZONE_2Y + Rnd.get(-80, 80), Config.ZONE_2Z, 0);
				else if (VoteZone.is_zone_3())
					activeChar.teleToLocation(Config.ZONE_3X + Rnd.get(-80, 80), Config.ZONE_3Y + Rnd.get(-80, 80), Config.ZONE_3Z, 0);
				else if (VoteZone.is_zone_4())
					activeChar.teleToLocation(Config.ZONE_4X + Rnd.get(-80, 80), Config.ZONE_4Y + Rnd.get(-80, 80), Config.ZONE_4Z, 0);
				else if (VoteZone.is_zone_5())
					activeChar.teleToLocation(Config.ZONE_5X + Rnd.get(-80, 80), Config.ZONE_5Y + Rnd.get(-80, 80), Config.ZONE_5Z, 0);
				else if (VoteZone.is_zone_6())
					activeChar.teleToLocation(Config.ZONE_6X + Rnd.get(-80, 80), Config.ZONE_6Y + Rnd.get(-80, 80), Config.ZONE_6Z, 0);
				else if (VoteZone.is_zone_7())
					activeChar.teleToLocation(Config.ZONE_7X + Rnd.get(-80, 80), Config.ZONE_7Y + Rnd.get(-80, 80), Config.ZONE_7Z, 0);
			}
			else if (_command.startsWith("tournament"))
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(0);
				html.setFile("data/html/mods/tournament_zone.htm");
				html.replace("%time%", sdf.format(new Date(System.currentTimeMillis())));
				activeChar.sendPacket(html);
			}
			else if (_command.startsWith("name_change"))
			{
				final String clientInfo = activeChar.getClient().toString();
				final String ip = clientInfo.substring(clientInfo.indexOf(" - IP: ") + 7, clientInfo.lastIndexOf("]"));
				
				try
				{
					String name = _command.substring(12);
					
					if (name.length() > 16)
					{
						activeChar.sendMessage("The chosen name cannot exceed 16 characters in length.");
						return;
					}
					
					if (name.length() < 3)
					{
						activeChar.sendMessage("Your name can not be mention that 3 characters in length.");
						return;
					}
					
					if (!StringUtil.isValidPlayerName(name))
					{
						activeChar.sendMessage("The new name doesn't fit with the regex pattern.");
						return;
					}
					
					if (PlayerNameTable.getInstance().getPlayerObjectId(name) > 0)
					{
						activeChar.sendMessage("The chosen name already exists.");
						return;
					}
					
					if (activeChar.destroyItemByItemId("Name Change", activeChar.getNameChangeItemId(), 1, null, true))
					{
						ChangeNameLog.auditGMAction(activeChar.getObjectId(), activeChar.getName(), name, ip);
						
						for (Player gm : World.getAllGMs())
							gm.sendPacket(new CreatureSay(0, Say2.SHOUT, "[Name]", activeChar.getName() + " mudou o nome para [" + name + "]"));
						
						activeChar.setName(name);
						PlayerNameTable.getInstance().updatePlayerData(activeChar, false);
						activeChar.sendPacket(new ExShowScreenMessage("Congratulations. Your name has been changed.", 6000, 0x02, true));
						activeChar.broadcastUserInfo();
						activeChar.store();
						activeChar.sendPacket(new PlaySound("ItemSound.quest_finish"));
					}
				}
				catch (Exception e)
				{
					activeChar.sendMessage("Fill out the field correctly.");
				}
			}
			else if (_command.startsWith("_clan_name_"))
			{
				final String clientInfo = activeChar.getClient().toString();
				final String ip = clientInfo.substring(clientInfo.indexOf(" - IP: ") + 7, clientInfo.lastIndexOf("]"));
				
				try
				{
					
					String name = _command.substring(12);
					
					if (name.length() < 2 || name.length() > 16)
					{
						activeChar.sendPacket(SystemMessageId.CLAN_NAME_LENGTH_INCORRECT);
						return;
					}
					
					if (!StringUtil.isAlphaNumeric(name))
					{
						activeChar.sendPacket(SystemMessageId.CLAN_NAME_INVALID);
						return;
					}
					
					if (ClanTable.getInstance().getClanByName(name) != null)
					{
						// clan name is already taken
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_EXISTS).addString(name));
						return;
					}
					final Clan clanold = activeChar.getClan();
					
					if (activeChar.destroyItemByItemId("", activeChar.getClanNameChangeItemId(), 1, null, true))
					{
						ChangeClanNameLog.auditGMAction(activeChar.getObjectId(), clanold.getName(), name, ip);
						
						for (Player gm : World.getAllGMs())
							gm.sendPacket(new CreatureSay(0, Say2.SHOUT, "[ClanName]", activeChar.getName() + " mudou o nome do clan para [" + name + "]"));
						
						for (Clan clan : ClanTable.getInstance().getClans())
						{
							clan.setClanName(name);
							clan.updateClanInDB();
							
						}
						activeChar.broadcastUserInfo();
						activeChar.store();
						activeChar.sendPacket(new ExShowScreenMessage("Congratulations. Your Clan name has been changed.", 6000, 0x02, true));
						activeChar.sendPacket(new PlaySound("ItemSound.quest_finish"));
					}
				}
				catch (Exception e)
				{
					activeChar.sendMessage("Fill out the field correctly.");
				}
			}
			else if (_command.startsWith("_ally_name_"))
			{
				final String clientInfo = activeChar.getClient().toString();
				final String ip = clientInfo.substring(clientInfo.indexOf(" - IP: ") + 7, clientInfo.lastIndexOf("]"));
				
				try
				{
					
					String name = _command.substring(12);
					
					if (!StringUtil.isAlphaNumeric(name))
					{
						activeChar.sendPacket(SystemMessageId.INCORRECT_ALLIANCE_NAME);
						return;
					}
					
					if (name.length() > 16 || name.length() < 2)
					{
						activeChar.sendPacket(SystemMessageId.INCORRECT_ALLIANCE_NAME_LENGTH);
						return;
					}
					
					if (ClanTable.getInstance().isAllyExists(name))
					{
						activeChar.sendPacket(SystemMessageId.ALLIANCE_ALREADY_EXISTS);
						return;
					}
					final Clan clanold = activeChar.getClan();
					
					if (activeChar.destroyItemByItemId("", activeChar.getAllyNameChangeItemId(), 1, null, true))
					{
						ChangeAllyNameLog.auditGMAction(activeChar.getObjectId(), clanold.getAllyName(), name, ip);
						
						for (Player gm : World.getAllGMs())
							gm.sendPacket(new CreatureSay(0, Say2.SHOUT, "[AllyName]", activeChar.getName() + " mudou o nome da Ally para [" + name + "]"));
						
						for (Clan clan : ClanTable.getInstance().getClans())
						{
							clan.setAllyName(name);
							clan.updateClanInDB();
							
						}
						activeChar.broadcastUserInfo();
						activeChar.store();
						activeChar.sendPacket(new ExShowScreenMessage("Congratulations. Your Ally name has been changed.", 6000, 0x02, true));
						activeChar.sendPacket(new PlaySound("ItemSound.quest_finish"));
					}
				}
				catch (Exception e)
				{
					activeChar.sendMessage("Fill out the field correctly.");
				}
			}
			else if (this._command.startsWith("classe_change"))
			{
				StringTokenizer st = new StringTokenizer(this._command);
				st.nextToken();
				String type = null;
				type = st.nextToken();
				try
				{
					if (activeChar.getBaseClass() != activeChar.getClassId().getId())
					{
						activeChar.sendMessage("SYS: Voce precisa estar com sua Classe Base para usar este item.");
						activeChar.sendPacket(new ExShowScreenMessage("You is not with its base class.", 6000, 2, true));
						return;
					}
					if (activeChar.isInOlympiadMode())
					{
						activeChar.sendMessage("This Item Cannot Be Used On Olympiad Games.");
						return;
					}
					ClassChangeCoin(activeChar, type);
				}
				catch (StringIndexOutOfBoundsException e)
				{
				}
			}
			else if (_command.startsWith("classe_index"))
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(0);
				html.setFile("data/html/mods/Coin Custom/classes.htm");
				activeChar.sendPacket(html);
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			}
			else if (_command.startsWith("change_sex"))
			{
				if (activeChar.destroyItemByItemId("Sex Change", activeChar.getSexChangeItemId(), 1, null, true))
				{
					Sex male = Sex.MALE;
					Sex female = Sex.FEMALE;
					
					if (activeChar.getAppearance().getSex() == male)
					{
						activeChar.getAppearance().setSex(female);
						activeChar.sendPacket(new ExShowScreenMessage("Congratulations. Your Sex has been changed.", 6000, 0x02, true));
						activeChar.broadcastUserInfo();
						activeChar.decayMe();
						activeChar.spawnMe();
					}
					else if (activeChar.getAppearance().getSex() == female)
					{
						activeChar.getAppearance().setSex(male);
						activeChar.sendPacket(new ExShowScreenMessage("Congratulations. Your Sex has been changed.", 6000, 0x02, true));
						activeChar.broadcastUserInfo();
						activeChar.decayMe();
						activeChar.spawnMe();
					}
					
					for (Player gm : World.getAllGMs())
						gm.sendPacket(new CreatureSay(0, Say2.SHOUT, "SYS", activeChar.getName() + " acabou de trocar de Sexo."));
					
					ThreadPool.schedule(new Runnable()
					{
						@Override
						public void run()
						{
							activeChar.logout();
						}
					}, 2000);
				}
				
			}
			// Navigate throught Manor windows
			else if (_command.startsWith("manor_menu_select?"))
			{
				WorldObject object = activeChar.getTarget();
				if (object instanceof Npc)
					((Npc) object).onBypassFeedback(activeChar, _command);
			}
			else if (_command.startsWith("bbs_") || _command.startsWith("_bbs") || _command.startsWith("_friend") || _command.startsWith("_mail") || _command.startsWith("_block"))
			{
				CommunityBoard.getInstance().handleCommands(getClient(), _command);
			}
			else if (_command.startsWith("Quest "))
			{
				if (!activeChar.validateBypass(_command))
					return;
				
				String[] str = _command.substring(6).trim().split(" ", 2);
				if (str.length == 1)
					activeChar.processQuestEvent(str[0], "");
				else
					activeChar.processQuestEvent(str[0], str[1]);
			}
			else if (_command.startsWith("bp_balance"))
			{
				String bp = _command.substring(11);
				StringTokenizer st = new StringTokenizer(bp);
				
				if (st.countTokens() != 1)
				{
					return;
				}
				
				int classId = Integer.parseInt(st.nextToken());
				
				AdminBalancer.sendBalanceWindow(classId, activeChar);
			}
			
			else if (_command.startsWith("bp_add"))
			{
				String bp = _command.substring(7);
				StringTokenizer st = new StringTokenizer(bp);
				
				if (st.countTokens() != 3)
				{
					return;
				}
				
				String stat = st.nextToken();
				int classId = Integer.parseInt(st.nextToken()), value = Integer.parseInt(st.nextToken());
				
				BalancerEdit.editStat(stat, classId, value, true);
				
				AdminBalancer.sendBalanceWindow(classId, activeChar);
			}
			
			else if (_command.startsWith("bp_rem"))
			{
				String bp = _command.substring(7);
				StringTokenizer st = new StringTokenizer(bp);
				
				if (st.countTokens() != 3)
				{
					return;
				}
				
				String stat = st.nextToken();
				int classId = Integer.parseInt(st.nextToken()), value = Integer.parseInt(st.nextToken());
				
				BalancerEdit.editStat(stat, classId, value, false);
				
				AdminBalancer.sendBalanceWindow(classId, activeChar);
			}
			else if (_command.startsWith("_match"))
			{
				String params = _command.substring(_command.indexOf("?") + 1);
				StringTokenizer st = new StringTokenizer(params, "&");
				int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
				int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
				int heroid = Hero.getInstance().getHeroByClass(heroclass);
				if (heroid > 0)
					Hero.getInstance().showHeroFights(activeChar, heroclass, heroid, heropage);
			}
			else if (_command.startsWith("_diary"))
			{
				String params = _command.substring(_command.indexOf("?") + 1);
				StringTokenizer st = new StringTokenizer(params, "&");
				int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
				int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
				int heroid = Hero.getInstance().getHeroByClass(heroclass);
				if (heroid > 0)
					Hero.getInstance().showHeroDiary(activeChar, heroclass, heroid, heropage);
			}
			else if (_command.startsWith("arenachange")) // change
			{
				final boolean isManager = activeChar.getCurrentFolkNPC() instanceof OlympiadManagerNpc;
				if (!isManager)
				{
					// Without npc, command can be used only in observer mode on arena
					if (!activeChar.isInObserverMode() || activeChar.isInOlympiadMode() || activeChar.getOlympiadGameId() < 0)
						return;
				}
				
				if (OlympiadManager.getInstance().isRegisteredInComp(activeChar))
				{
					activeChar.sendPacket(SystemMessageId.WHILE_YOU_ARE_ON_THE_WAITING_LIST_YOU_ARE_NOT_ALLOWED_TO_WATCH_THE_GAME);
					return;
				}
				
				final int arenaId = Integer.parseInt(_command.substring(12).trim());
				activeChar.enterOlympiadObserverMode(arenaId);
			}
			else if (_command.startsWith("tournament_observe"))
			{
				if (activeChar._inEventTvT || activeChar._inEventCTF)
				{
					activeChar.sendMessage("You already participated in the event tvt/ctf/pvp event!");
					return;
				}
				
				StringTokenizer st = new StringTokenizer(_command);
				st.nextToken();
				
				final int x = Integer.parseInt(st.nextToken());
				final int y = Integer.parseInt(st.nextToken());
				final int z = Integer.parseInt(st.nextToken());
				// if ((x == -114413 && y == -213241 && z == -3331) || (x == -81748 && y == -245950 && z == -3331) || (x == -120324 && y == -225077 && z == -3331) && (activeChar.isInsideZone(ZoneId.TOURNAMENT) || activeChar.isInsideZone(ZoneId.ARENA_EVENT)))
				// {
				activeChar.setArenaObserv(true);
				activeChar.enterTvTObserverMode(x, y, z);
				// }
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Bad RequestBypassToServer: " + e, e);
		}
	}
	
	public static void Incorrect_item(Player activeChar)
	{
		activeChar.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/mods/Coin Custom/NoItem.htm";
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(filename);
		activeChar.sendPacket(html);
	}
	
	private static void playerHelp(Player activeChar, String path)
	{
		if (path.indexOf("..") != -1)
			return;
		
		final StringTokenizer st = new StringTokenizer(path);
		final String[] cmd = st.nextToken().split("#");
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/help/" + cmd[0]);
		if (cmd.length > 1)
			html.setItemId(Integer.parseInt(cmd[1]));
		html.disableValidation();
		activeChar.sendPacket(html);
	}
	
	private static void ClassChangeCoin(Player player, String command)
	{
		String nameclasse = player.getTemplate().getClassName();
		String type = command;
		if (type.equals("---SELECIONE---"))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setFile("data/html/mods/Coin Custom/classes.htm");
			player.sendPacket(html);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			player.sendMessage("Por favor, Selecione a Classe desejada para continuar.");
		}
		if (type.equals("Duelist"))
		{
			if (player.getClassId().getId() == 88)
			{
				player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
				return;
			}
			RemoverSkills(player);
			player.setClassId(88);
			if (!player.isSubClassActive())
				player.setBaseClass(88);
			Finish(player);
		}
		if (type.equals("DreadNought"))
		{
			if (player.getClassId().getId() == 89)
			{
				player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
				return;
			}
			RemoverSkills(player);
			player.setClassId(89);
			if (!player.isSubClassActive())
				player.setBaseClass(89);
			Finish(player);
		}
		if (type.equals("Phoenix_Knight"))
		{
			if (player.getClassId().getId() == 90)
			{
				player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
				return;
			}
			RemoverSkills(player);
			player.setClassId(90);
			if (!player.isSubClassActive())
				player.setBaseClass(90);
			Finish(player);
		}
		if (type.equals("Hell_Knight"))
		{
			if (player.getClassId().getId() == 91)
			{
				player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
				return;
			}
			RemoverSkills(player);
			player.setClassId(91);
			if (!player.isSubClassActive())
				player.setBaseClass(91);
			Finish(player);
		}
		if (type.equals("Sagittarius"))
		{
			if (player.getClassId().getId() == 92)
			{
				player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
				return;
			}
			RemoverSkills(player);
			player.setClassId(92);
			if (!player.isSubClassActive())
				player.setBaseClass(92);
			Finish(player);
		}
		if (type.equals("Adventurer"))
		{
			if (player.getClassId().getId() == 93)
			{
				player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
				return;
			}
			RemoverSkills(player);
			player.setClassId(93);
			if (!player.isSubClassActive())
				player.setBaseClass(93);
			Finish(player);
		}
		if (type.equals("Archmage"))
		{
			if (player.getClassId().getId() == 94)
			{
				player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
				return;
			}
			RemoverSkills(player);
			player.setClassId(94);
			if (!player.isSubClassActive())
				player.setBaseClass(94);
			Finish(player);
		}
		if (type.equals("Soultaker"))
		{
			if (player.getClassId().getId() == 95)
			{
				player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
				return;
			}
			RemoverSkills(player);
			player.setClassId(95);
			if (!player.isSubClassActive())
				player.setBaseClass(95);
			Finish(player);
		}
		if (type.equals("Arcana_Lord"))
		{
			if (player.getClassId().getId() == 96)
			{
				player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
				return;
			}
			RemoverSkills(player);
			player.setClassId(96);
			if (!player.isSubClassActive())
				player.setBaseClass(96);
			Finish(player);
		}
		if (type.equals("Cardinal"))
		{
			if (player.getClassId().getId() == 97)
			{
				player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
				return;
			}
			RemoverSkills(player);
			player.setClassId(97);
			if (!player.isSubClassActive())
				player.setBaseClass(97);
			Finish(player);
		}
		if (type.equals("Hierophant"))
		{
			if (player.getClassId().getId() == 98)
			{
				player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
				return;
			}
			RemoverSkills(player);
			player.setClassId(98);
			if (!player.isSubClassActive())
				player.setBaseClass(98);
			Finish(player);
		}
		if (type.equals("Eva_Templar"))
		{
			if (player.getClassId().getId() == 99)
			{
				player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
				return;
			}
			RemoverSkills(player);
			player.setClassId(99);
			if (!player.isSubClassActive())
				player.setBaseClass(99);
			Finish(player);
		}
		if (type.equals("Sword_Muse"))
		{
			if (player.getClassId().getId() == 100)
			{
				player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
				return;
			}
			RemoverSkills(player);
			player.setClassId(100);
			if (!player.isSubClassActive())
				player.setBaseClass(100);
			Finish(player);
		}
		if (type.equals("Wind_Rider"))
		{
			if (player.getClassId().getId() == 101)
			{
				player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
				return;
			}
			RemoverSkills(player);
			player.setClassId(101);
			if (!player.isSubClassActive())
				player.setBaseClass(101);
			Finish(player);
		}
		if (type.equals("Moonli_Sentinel"))
		{
			if (player.getClassId().getId() == 102)
			{
				player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
				return;
			}
			RemoverSkills(player);
			player.setClassId(102);
			if (!player.isSubClassActive())
				player.setBaseClass(102);
			Finish(player);
		}
		if (type.equals("Mystic_Muse"))
		{
			if (player.getClassId().getId() == 103)
			{
				player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
				return;
			}
			RemoverSkills(player);
			player.setClassId(103);
			if (!player.isSubClassActive())
				player.setBaseClass(103);
			Finish(player);
		}
		if (type.equals("Elemental_Master"))
		{
			if (player.getClassId().getId() == 104)
			{
				player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
				return;
			}
			RemoverSkills(player);
			player.setClassId(104);
			if (!player.isSubClassActive())
				player.setBaseClass(104);
			Finish(player);
		}
		if (type.equals("Eva_Saint"))
		{
			if (player.getClassId().getId() == 105)
			{
				player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
				return;
			}
			RemoverSkills(player);
			player.setClassId(105);
			if (!player.isSubClassActive())
				player.setBaseClass(105);
			Finish(player);
		}
		if (type.equals("Shillien_Templar"))
		{
			if (player.getClassId().getId() == 106)
			{
				player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
				return;
			}
			RemoverSkills(player);
			player.setClassId(106);
			if (!player.isSubClassActive())
				player.setBaseClass(106);
			Finish(player);
		}
		if (type.equals("Spectral_Dancer"))
		{
			if (player.getClassId().getId() == 107)
			{
				player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
				return;
			}
			RemoverSkills(player);
			player.setClassId(107);
			if (!player.isSubClassActive())
				player.setBaseClass(107);
			Finish(player);
		}
		if (type.equals("Ghost_Hunter"))
		{
			if (player.getClassId().getId() == 108)
			{
				player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
				return;
			}
			RemoverSkills(player);
			player.setClassId(108);
			if (!player.isSubClassActive())
				player.setBaseClass(108);
			Finish(player);
		}
		if (type.equals("Ghost_Sentinel"))
		{
			if (player.getClassId().getId() == 109)
			{
				player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
				return;
			}
			RemoverSkills(player);
			player.setClassId(109);
			if (!player.isSubClassActive())
				player.setBaseClass(109);
			Finish(player);
		}
		if (type.equals("Storm_Screamer"))
		{
			if (player.getClassId().getId() == 110)
			{
				player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
				return;
			}
			RemoverSkills(player);
			player.setClassId(110);
			if (!player.isSubClassActive())
				player.setBaseClass(110);
			Finish(player);
		}
		if (type.equals("Spectral_Master"))
		{
			if (player.getClassId().getId() == 111)
			{
				player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
				return;
			}
			RemoverSkills(player);
			player.setClassId(111);
			if (!player.isSubClassActive())
				player.setBaseClass(111);
			Finish(player);
		}
		if (type.equals("Shillen_Saint"))
		{
			if (player.getClassId().getId() == 112)
			{
				player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
				return;
			}
			RemoverSkills(player);
			player.setClassId(112);
			if (!player.isSubClassActive())
				player.setBaseClass(112);
			Finish(player);
		}
		if (type.equals("Titan"))
		{
			if (player.getClassId().getId() == 113)
			{
				player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
				return;
			}
			RemoverSkills(player);
			player.setClassId(113);
			if (!player.isSubClassActive())
				player.setBaseClass(113);
			Finish(player);
		}
		if (type.equals("Grand_Khauatari"))
		{
			if (player.getClassId().getId() == 114)
			{
				player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
				return;
			}
			RemoverSkills(player);
			player.setClassId(114);
			if (!player.isSubClassActive())
				player.setBaseClass(114);
			Finish(player);
		}
		if (type.equals("Dominator"))
		{
			if (player.getClassId().getId() == 115)
			{
				player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
				return;
			}
			RemoverSkills(player);
			player.setClassId(115);
			if (!player.isSubClassActive())
				player.setBaseClass(115);
			Finish(player);
		}
		if (type.equals("Doomcryer"))
		{
			if (player.getClassId().getId() == 116)
			{
				player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
				return;
			}
			RemoverSkills(player);
			player.setClassId(116);
			if (!player.isSubClassActive())
				player.setBaseClass(116);
			Finish(player);
		}
		if (type.equals("Fortune_Seeker"))
		{
			if (player.getClassId().getId() == 117)
			{
				player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
				return;
			}
			RemoverSkills(player);
			player.setClassId(117);
			if (!player.isSubClassActive())
				player.setBaseClass(117);
			Finish(player);
		}
		if (type.equals("Maestro"))
		{
			if (player.getClassId().getId() == 118)
			{
				player.sendMessage("Desculpe, voce ja esta com a Classe " + nameclasse + ".");
				return;
			}
			RemoverSkills(player);
			player.setClassId(118);
			if (!player.isSubClassActive())
				player.setBaseClass(118);
			Finish(player);
		}
	}
	
	public static String getItemNameById(int itemId)
	{
		Item item = ItemTable.getInstance().getTemplate(itemId);
		
		String itemName = "NoName";
		
		if (itemId != 0)
		{
			itemName = item.getName();
		}
		
		return itemName;
	}
	
	public static void showDressMeMainPage(Player player)
	{
		NpcHtmlMessage htm = new NpcHtmlMessage(1);
		String text = HtmCache.getInstance().getHtm("data/html/dressme/index.htm");
		
		htm.setHtml(text);
		
		{
			htm.replace("%time%", sdf.format(new Date(System.currentTimeMillis())));
			htm.replace("%dat%", (new SimpleDateFormat("dd/MM/yyyy")).format(new Date(System.currentTimeMillis())));
			
		}
		
		player.sendPacket(htm);
	}
	
	private static void showSkinList(Player player, String type, int page)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		
		html.setFile("data/html/dressme/allskins.htm");
		
		html.replace("%time%", sdf.format(new Date(System.currentTimeMillis())));
		html.replace("%dat%", (new SimpleDateFormat("dd/MM/yyyy")).format(new Date(System.currentTimeMillis())));
		
		final int ITEMS_PER_PAGE = 8;
		
		int myPage = 1;
		int i = 0;
		int shown = 0;
		boolean hasMore = false;
		int itemId = 0;
		
		final StringBuilder sb = new StringBuilder();
		
		List<SkinPackage> tempList = null;
		switch (type.toLowerCase())
		{
			case "armor":
				tempList = DressMeData.getInstance().getArmorSkinOptions().values().stream().filter(s -> !player.hasArmorSkin(s.getId())).collect(Collectors.toList());
				break;
			case "weapon":
				tempList = DressMeData.getInstance().getWeaponSkinOptions().values().stream().filter(s -> !player.hasWeaponSkin(s.getId())).collect(Collectors.toList());
				break;
			case "hair":
				tempList = DressMeData.getInstance().getHairSkinOptions().values().stream().filter(s -> !player.hasHairSkin(s.getId())).collect(Collectors.toList());
				break;
			case "face":
				tempList = DressMeData.getInstance().getFaceSkinOptions().values().stream().filter(s -> !player.hasFaceSkin(s.getId())).collect(Collectors.toList());
				break;
			case "shield":
				tempList = DressMeData.getInstance().getShieldSkinOptions().values().stream().filter(s -> !player.hasShieldSkin(s.getId())).collect(Collectors.toList());
				break;
		}
		
		if (tempList != null && !tempList.isEmpty())
		{
			for (SkinPackage sp : tempList)
			{
				if (sp == null)
				{
					continue;
				}
				
				if (shown == ITEMS_PER_PAGE)
				{
					hasMore = true;
					break;
				}
				
				if (myPage != page)
				{
					i++;
					if (i == ITEMS_PER_PAGE)
					{
						myPage++;
						i = 0;
					}
					continue;
				}
				
				if (shown == ITEMS_PER_PAGE)
				{
					hasMore = true;
					break;
				}
				
				switch (type.toLowerCase())
				{
					case "armor":
						itemId = sp.getChestId();
						break;
					case "weapon":
						itemId = sp.getWeaponId();
						break;
					case "hair":
						itemId = sp.getHairId();
						break;
					case "face":
						itemId = sp.getFaceId();
						break;
					case "shield":
						itemId = sp.getShieldId();
						break;
				}
				
				sb.append("<table border=0 cellspacing=0 cellpadding=2 height=36><tr>");
				sb.append("<td width=32 align=center>" + "<button width=32 height=32 back=" + Item.getItemIcon(itemId) + " fore=" + Item.getItemIcon(itemId) + ">" + "</td>");
				sb.append("<td width=124>" + sp.getName() + "<br1> <font color=999999>Price:</font> <font color=339966>" + Item.getItemNameById(sp.getPriceId()) + "</font> (<font color=LEVEL>" + sp.getPriceCount() + "</font>)</td>");
				sb.append("<td align=center width=65>" + "<button value=\"Buy\" action=\"bypass -h dressme " + page + " buyskin  " + sp.getId() + " " + type + " " + itemId + "\" width=65 height=19 back=L2UI_ch3.smallbutton2_over fore=L2UI_ch3.smallbutton2>" + "</td>");
				sb.append("<td align=center width=65>" + "<button value=\"Try\" action=\"bypass -h dressme " + page + " tryskin  " + sp.getId() + " " + type + "\" width=65 height=19 back=L2UI_ch3.smallbutton2_over fore=L2UI_ch3.smallbutton2>" + "</td>");
				
				sb.append("</tr></table>");
				sb.append("<img src=\"L2UI.Squaregray\" width=\"300\" height=\"1\">");
				shown++;
			}
		}
		
		sb.append("<table width=300><tr>");
		sb.append("<td align=center width=70>" + (page > 1 ? "<button value=\"< PREV\" action=\"bypass -h dressme " + (page - 1) + " skinlist " + type + "\" width=65 height=19 back=L2UI_ch3.smallbutton2_over fore=L2UI_ch3.smallbutton2>" : "") + "</td>");
		sb.append("<td align=center width=140>Page: " + page + "</td>");
		sb.append("<td align=center width=70>" + (hasMore ? "<button value=\"NEXT >\" action=\"bypass -h dressme " + (page + 1) + " skinlist " + type + "\" width=65 height=19 back=L2UI_ch3.smallbutton2_over fore=L2UI_ch3.smallbutton2>" : "") + "</td>");
		sb.append("</tr></table>");
		
		html.replace("%showList%", sb.toString());
		player.sendPacket(html);
	}
	
	private static void showMySkinList(Player player, int page)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile("data/html/dressme/myskins.htm");
		
		html.replace("%time%", sdf.format(new Date(System.currentTimeMillis())));
		html.replace("%dat%", (new SimpleDateFormat("dd/MM/yyyy")).format(new Date(System.currentTimeMillis())));
		
		final int ITEMS_PER_PAGE = 8;
		int itemId = 0;
		
		int myPage = 1;
		int i = 0;
		int shown = 0;
		boolean hasMore = false;
		
		final StringBuilder sb = new StringBuilder();
		
		List<SkinPackage> armors = DressMeData.getInstance().getArmorSkinOptions().values().stream().filter(s -> player.hasArmorSkin(s.getId())).collect(Collectors.toList());
		List<SkinPackage> weapons = DressMeData.getInstance().getWeaponSkinOptions().values().stream().filter(s -> player.hasWeaponSkin(s.getId())).collect(Collectors.toList());
		List<SkinPackage> hairs = DressMeData.getInstance().getHairSkinOptions().values().stream().filter(s -> player.hasHairSkin(s.getId())).collect(Collectors.toList());
		List<SkinPackage> faces = DressMeData.getInstance().getFaceSkinOptions().values().stream().filter(s -> player.hasFaceSkin(s.getId())).collect(Collectors.toList());
		List<SkinPackage> shield = DressMeData.getInstance().getShieldSkinOptions().values().stream().filter(s -> player.hasShieldSkin(s.getId())).collect(Collectors.toList());
		
		List<SkinPackage> list = Stream.concat(armors.stream(), weapons.stream()).collect(Collectors.toList());
		shield.stream().collect(Collectors.toList());
		List<SkinPackage> list2 = Stream.concat(hairs.stream(), shield.stream()).collect(Collectors.toList());
		List<SkinPackage> list3 = faces.stream().collect(Collectors.toList());
		
		List<SkinPackage> allLists = Stream.concat(list.stream(), Stream.concat(list2.stream(), list3.stream())).collect(Collectors.toList());
		
		if (!allLists.isEmpty())
		{
			for (SkinPackage sp : allLists)
			{
				if (sp == null)
				{
					continue;
				}
				
				if (shown == ITEMS_PER_PAGE)
				{
					hasMore = true;
					break;
				}
				
				if (myPage != page)
				{
					i++;
					if (i == ITEMS_PER_PAGE)
					{
						myPage++;
						i = 0;
					}
					continue;
				}
				
				if (shown == ITEMS_PER_PAGE)
				{
					hasMore = true;
					break;
				}
				
				switch (sp.getType().toLowerCase())
				{
					case "armor":
						itemId = sp.getChestId();
						break;
					case "weapon":
						itemId = sp.getWeaponId();
						break;
					case "hair":
						itemId = sp.getHairId();
						break;
					case "face":
						itemId = sp.getFaceId();
						break;
					case "shield":
						itemId = sp.getShieldId();
						break;
				}
				
				sb.append("<table border=0 cellspacing=0 cellpadding=2 height=36><tr>");
				sb.append("<td width=32 align=center>" + "<button width=32 height=32 back=" + Item.getItemIcon(itemId) + " fore=" + Item.getItemIcon(itemId) + ">" + "</td>");
				sb.append("<td width=124>" + sp.getName() + "</td>");
				sb.append("<td align=center width=65>" + "<button value=\"Equip\" action=\"bypass -h dressme " + page + " setskin " + sp.getId() + " " + sp.getType() + " " + itemId + "\" width=65 height=19 back=L2UI_ch3.smallbutton2_over fore=L2UI_ch3.smallbutton2>" + "</td>");
				sb.append("<td align=center width=65>" + "<button value=\"Remove\" action=\"bypass -h dressme " + page + " clean " + sp.getType() + "\" width=65 height=19 back=L2UI_ch3.smallbutton2_over fore=L2UI_ch3.smallbutton2>" + "</td>");
				sb.append("</tr></table>");
				sb.append("<img src=\"L2UI.Squaregray\" width=\"300\" height=\"1\">");
				shown++;
			}
		}
		
		sb.append("<table width=300><tr>");
		sb.append("<td align=center width=70>" + (page > 1 ? "<button value=\"< PREV\" action=\"bypass -h dressme " + (page - 1) + " myskinlist\" width=65 height=19 back=L2UI_ch3.smallbutton2_over fore=L2UI_ch3.smallbutton2>" : "") + "</td>");
		sb.append("<td align=center width=140>Page: " + page + "</td>");
		sb.append("<td align=center width=70>" + (hasMore ? "<button value=\"NEXT >\" action=\"bypass -h dressme " + (page + 1) + " myskinlist\" width=65 height=19 back=L2UI_ch3.smallbutton2_over fore=L2UI_ch3.smallbutton2>" : "") + "</td>");
		sb.append("</tr></table>");
		
		html.replace("%showList%", sb.toString());
		player.sendPacket(html);
	}
	
	public static boolean checkWeapons(Player player, ItemInstance skin, WeaponType weapon1, WeaponType weapon2)
	{
		if (player.getActiveWeaponItem().getItemType() == weapon1 && skin.getItem().getItemType() != weapon2)
		{
			return false;
		}
		
		return true;
	}
	
	private static void RemoverSkills(Player activeChar)
	{
		
		for (L2Skill s : activeChar.getSkills().values())
			activeChar.removeSkill(s);
		
		activeChar.destroyItemByItemId("Classe Change", activeChar.getClassChangeItemId(), 1, null, true);
	}
	
	private static void showTrySkinHtml(Player activeChar)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/mods/menu/trySkin.htm");
		activeChar.sendPacket(html);
	}
	
	public static void Finish(Player activeChar)
	{
		String newclass = activeChar.getTemplate().getClassName();
		
		activeChar.sendMessage(activeChar.getName() + " is now a " + newclass + ".");
		activeChar.sendPacket(new ExShowScreenMessage("Congratulations. You is now a " + newclass + ".", 6000, 0x02, true));
		
		activeChar.refreshOverloaded();
		activeChar.store();
		activeChar.sendPacket(new HennaInfo(activeChar));
		activeChar.sendSkillList();
		activeChar.broadcastUserInfo();
		
		activeChar.sendPacket(new PlaySound("ItemSound.quest_finish"));
		
		for (Player gm : World.getAllGMs())
			gm.sendPacket(new CreatureSay(0, Say2.SHOUT, "Chat Manager", activeChar.getName() + " acabou de trocar sua Classe Base."));
		
		if (activeChar.isNoble())
		{
			StatsSet playerStat = Olympiad.getNobleStats(activeChar.getObjectId());
			if (!(playerStat == null))
			{
				AdminEditChar.updateClasse(activeChar);
				AdminEditChar.DeleteHero(activeChar);
				activeChar.sendMessage("You now has " + Olympiad.getInstance().getNoblePoints(activeChar.getObjectId()) + " Olympiad points.");
			}
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// Remove all henna info stored for this sub-class.
			PreparedStatement statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, activeChar.getObjectId());
			statement.setInt(2, 0);
			statement.execute();
			statement.close();
			
		}
		catch (Exception e)
		{
			_log.warning("Class Item: " + e);
		}
		
		activeChar.logout(true);
		
	}
}