package com.l2j4team.commons.mmocore;

/**
 * @author KenM
 * @param <T>
 */
public interface IMMOExecutor<T extends MMOClient<?>>
{
	public void execute(ReceivablePacket<T> packet);
}