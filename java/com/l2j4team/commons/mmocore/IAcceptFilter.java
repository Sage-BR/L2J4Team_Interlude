package com.l2j4team.commons.mmocore;

import java.nio.channels.SocketChannel;

/**
 * @author KenM
 */
public interface IAcceptFilter
{
	public boolean accept(SocketChannel sc);
}