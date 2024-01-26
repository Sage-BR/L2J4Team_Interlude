package com.l2j4team.loginserver;

import com.l2j4team.loginserver.network.clientpackets.AuthGameGuard;
import com.l2j4team.loginserver.network.clientpackets.RequestAuthLogin;
import com.l2j4team.loginserver.network.clientpackets.RequestServerList;
import com.l2j4team.loginserver.network.clientpackets.RequestServerLogin;

import java.nio.ByteBuffer;

import com.l2j4team.commons.mmocore.IPacketHandler;
import com.l2j4team.commons.mmocore.ReceivablePacket;

public final class L2LoginPacketHandler implements IPacketHandler<L2LoginClient>
{
	@Override
	public ReceivablePacket<L2LoginClient> handlePacket(final ByteBuffer buf, final L2LoginClient client)
	{
		final int opcode = buf.get() & 0xFF;
		ReceivablePacket<L2LoginClient> packet = null;
		final L2LoginClient.LoginClientState state = client.getState();
		switch (state)
		{
			case CONNECTED:
			{
				if (opcode == 7)
				{
					packet = new AuthGameGuard();
					break;
				}
				debugOpcode(opcode, state);
				break;
			}
			case AUTHED_GG:
			{
				if (opcode == 0)
				{
					packet = new RequestAuthLogin();
					break;
				}
				debugOpcode(opcode, state);
				break;
			}
			case AUTHED_LOGIN:
			{
				if (opcode == 5)
				{
					packet = new RequestServerList();
					break;
				}
				if (opcode == 2)
				{
					packet = new RequestServerLogin();
					break;
				}
				debugOpcode(opcode, state);
				break;
			}
		}
		return packet;
	}

	private static void debugOpcode(final int opcode, final L2LoginClient.LoginClientState state)
	{
		System.out.println("Unknown Opcode: " + opcode + " for state: " + state.name());
	}
}
