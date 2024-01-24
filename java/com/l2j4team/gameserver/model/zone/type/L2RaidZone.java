/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <[url="http://www.gnu.org/licenses/>."]http://www.gnu.org/licenses/>.[/url]
 */
package com.l2j4team.gameserver.model.zone.type;

import com.l2j4team.Config;
import com.l2j4team.gameserver.instancemanager.RaidZoneManager;
import com.l2j4team.gameserver.model.World;
import com.l2j4team.gameserver.model.actor.Creature;
import com.l2j4team.gameserver.model.actor.instance.Player;
import com.l2j4team.gameserver.model.zone.L2SpawnZone;
import com.l2j4team.gameserver.model.zone.ZoneId;
import com.l2j4team.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2j4team.gameserver.taskmanager.PvpFlagTaskManager;

public class L2RaidZone extends L2SpawnZone
{
	private int _maxClanMembers;
	private int _maxAllyMembers;
	private int _minPartyMembers;
	private boolean _checkParty;
	private boolean _checkClan;
	private boolean _checkAlly;
	
	public L2RaidZone(int id)
	{
		super(id);
		
		_maxClanMembers = 0;
		_maxAllyMembers = 0;
		_minPartyMembers = 0;
		_checkParty = false;
		_checkClan = false;
		_checkAlly = false;
	}

	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("MaxClanMembers"))
			_maxClanMembers = Integer.parseInt(value);
		else if (name.equals("MaxAllyMembers"))
			_maxAllyMembers = Integer.parseInt(value);
		else if (name.equals("MinPartyMembers"))
			_minPartyMembers = Integer.parseInt(value);
		else if (name.equals("checkParty"))
			_checkParty = Boolean.parseBoolean(value);
		else if (name.equals("checkClan"))
			_checkClan = Boolean.parseBoolean(value);
		else if (name.equals("checkAlly"))
			_checkAlly = Boolean.parseBoolean(value);
		else
			super.setParameter(name, value);
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		character.setInsideZone(ZoneId.RAID, true);
		
		if (character instanceof Player)
		{
			final Player activeChar = (Player) character;
			
			//activeChar.sendPacket(new ExShowScreenMessage("You have entered a Boss Zone!", 4000, ExShowScreenMessage.SMPOS.MIDDLE_LEFT, false));
			activeChar.sendMessage("You have entered a Boss Zone!");
			
			if (Config.ENABLE_FLAGZONE)
			{
				
				if (!activeChar.isInObserverMode())
				{
					if (activeChar.getPvpFlag() > 0)
						PvpFlagTaskManager.getInstance().remove(activeChar);
					
					activeChar.updatePvPFlag(1);
					
					if (!activeChar.isGM())
						activeChar.getAppearance().setVisible();
					
				}
			}
			
			if (_checkParty)
			{
				if (!activeChar.isInParty() || activeChar.getParty().getMemberCount() < _minPartyMembers)
				{
					activeChar.sendPacket(new ExShowScreenMessage("Your party does not have " + _minPartyMembers + " members to enter on this zone!", 6 * 1000));
					RaidZoneManager.getInstance().RandomTeleport(activeChar);
				}
			}
			

			 if(!activeChar.isPhantom())
				 RaidZoneManager.getInstance().checkPlayersArea_ip(activeChar, Integer.valueOf(2), World.getInstance().getPlayers(), Boolean.valueOf(true)); 

			
			if (Config.BOSSZONE_HWID_PROTECT && !activeChar.isPhantom())
				MaxPlayersOnArea(activeChar);

			if (_checkClan)
				MaxClanMembersOnArea(activeChar);

			if (_checkAlly)
				MaxAllyMembersOnArea(activeChar);
		}
	}
	
	public boolean MaxPlayersOnArea(Player activeChar)
	{
		return RaidZoneManager.getInstance().checkPlayersArea(activeChar, Config.MAX_BOX_IN_BOSSZONE, true);
	}
	
	public boolean MaxClanMembersOnArea(Player activeChar)
	{
		return RaidZoneManager.getInstance().checkClanArea(activeChar, _maxClanMembers, true);
	}
	
	public boolean MaxAllyMembersOnArea(Player activeChar)
	{
		return RaidZoneManager.getInstance().checkAllyArea(activeChar, _maxAllyMembers, World.getInstance().getPlayers(), true);
	}
	
	@Override
	protected void onExit(Creature character)
	{
		character.setInsideZone(ZoneId.RAID, false);
		
		if (character instanceof Player)
		{
			final Player activeChar = (Player) character;
			
			activeChar.sendMessage("You have left a Boss Zone!");
			
			if(Config.ENABLE_FLAGZONE)
			{
				PvpFlagTaskManager.getInstance().add(activeChar, Config.PVP_NORMAL_TIME);
			}
		}
	}

	@Override
	public void onDieInside(Creature character)
	{
	}

	@Override
	public void onReviveInside(Creature character)
	{
	}
}