package com.l2j4team.gameserver.network.serverpackets;

import com.l2j4team.gameserver.model.actor.instance.Player;
import com.l2j4team.gameserver.model.location.SpawnLocation;

public class StopMoveInVehicle extends L2GameServerPacket
{
	private final int _objectId;
	private final int _boatId;
	private final SpawnLocation _loc;
	
	public StopMoveInVehicle(Player player, int boatId)
	{
		_objectId = player.getObjectId();
		_boatId = boatId;
		_loc = player.getVehiclePosition();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x72);
		writeD(_objectId);
		writeD(_boatId);
		writeD(_loc.getX());
		writeD(_loc.getY());
		writeD(_loc.getZ());
		writeD(_loc.getHeading());
	}
}