package com.l2j4team.gameserver;

import com.l2j4team.gameserver.handler.voicedcommandhandlers.VoicedMenu;
import com.l2j4team.gameserver.model.actor.instance.Player;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import com.l2j4team.commons.concurrent.ThreadPool;

public enum AutofarmManager
{
	
	INSTANCE;
	
	public static final Logger _log = Logger.getLogger(AutofarmManager.class.getName());
	private final Long iterationSpeedMs = 450L;
	
	private final ConcurrentHashMap<Integer, AutofarmPlayerRoutine> activeFarmers = new ConcurrentHashMap<>();
	private final ScheduledFuture<?> onUpdateTask = ThreadPool.scheduleAtFixedRate(onUpdate(), 1000, iterationSpeedMs);
	
	private Runnable onUpdate()
	{
		return () -> activeFarmers.forEach((integer, autofarmPlayerRoutine) -> autofarmPlayerRoutine.executeRoutine());
	}
	
	public void startFarm(Player player)
	{
		if (isAutofarming(player))
		{
			player.sendMessage("You are already autofarming");
			return;
		}
		
		activeFarmers.put(player.getObjectId(), new AutofarmPlayerRoutine(player));
		player.sendMessage("Autofarming activated");
		
	}
	
	public void stopFarm(Player player)
	{
		if (!isAutofarming(player))
		{
			player.sendMessage("You are not autofarming");
			return;
		}
		
		activeFarmers.remove(player.getObjectId());
		player.sendMessage("Autofarming deactivated");
	}
	
	public void toggleFarm(Player player)
	{
		if (isAutofarming(player))
		{
			stopFarm(player);
			VoicedMenu.sendVipWindow(player);
			return;
		}
		
		startFarm(player);
		VoicedMenu.sendVipWindow(player);
	}
	
	public Boolean isAutofarming(Player player)
	{
		return activeFarmers.containsKey(player.getObjectId());
	}
	
	public void onPlayerLogout(Player player)
	{
		stopFarm(player);
	}
	
	public void onDeath(Player player)
	{
		if (isAutofarming(player))
		{
			activeFarmers.remove(player.getObjectId());
		}
	}
	
	public ScheduledFuture<?> getOnUpdateTask()
	{
		return onUpdateTask;
	}
}