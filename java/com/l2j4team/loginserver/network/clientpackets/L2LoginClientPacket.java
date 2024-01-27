package com.l2j4team.loginserver.network.clientpackets;

import com.l2j4team.loginserver.L2LoginClient;

import java.util.logging.Logger;

import com.l2j4team.commons.mmocore.ReceivablePacket;

public abstract class L2LoginClientPacket extends ReceivablePacket<L2LoginClient>
{
	private static Logger _log;
	
	@Override
	protected final boolean read()
	{
		try
		{
			return readImpl();
		}
		catch (Exception e)
		{
			L2LoginClientPacket._log.severe("ERROR READING: " + this.getClass().getSimpleName());
			e.printStackTrace();
			return false;
		}
	}
	
	protected abstract boolean readImpl();
	
	static
	{
		L2LoginClientPacket._log = Logger.getLogger(L2LoginClientPacket.class.getName());
	}
}
