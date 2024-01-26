package com.l2j4team.gameserver.model.partymatching;

import com.l2j4team.gameserver.model.actor.instance.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gnacik
 */
public class PartyMatchWaitingList
{
	private final List<Player> _members;

	protected PartyMatchWaitingList()
	{
		_members = new ArrayList<>();
	}

	public void addPlayer(Player player)
	{
		if (!_members.contains(player))
			_members.add(player);
	}

	public void removePlayer(Player player)
	{
		if (_members.contains(player))
			_members.remove(player);
	}

	public List<Player> getPlayers()
	{
		return _members;
	}

	public static PartyMatchWaitingList getInstance()
	{
		return SingletonHolder._instance;
	}

	private static class SingletonHolder
	{
		protected static final PartyMatchWaitingList _instance = new PartyMatchWaitingList();
	}
}