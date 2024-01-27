package com.l2j4team.gameserver.model.entity;

import com.l2j4team.gameserver.model.actor.Npc;
import com.l2j4team.gameserver.model.entity.Siege.SiegeSide;
import com.l2j4team.gameserver.model.pledge.Clan;

import java.util.Calendar;
import java.util.List;

public interface Siegable
{
	public void startSiege();
	
	public void endSiege();
	
	public List<Clan> getAttackerClans();
	
	public List<Clan> getDefenderClans();
	
	public boolean checkSide(Clan clan, SiegeSide type);
	
	public boolean checkSides(Clan clan, SiegeSide... types);
	
	public boolean checkSides(Clan clan);
	
	public Npc getFlag(Clan clan);
	
	public Calendar getSiegeDate();
}