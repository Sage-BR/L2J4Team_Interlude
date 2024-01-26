package com.l2j4team.gameserver.model.actor.instance;

import com.l2j4team.events.bossevent.BossEvent;
import com.l2j4team.events.bossevent.BossEvent.EventState;
import com.l2j4team.gameserver.model.actor.Npc;
import com.l2j4team.gameserver.model.actor.ai.CtrlIntention;
import com.l2j4team.gameserver.model.actor.template.NpcTemplate;
import com.l2j4team.gameserver.network.serverpackets.ActionFailed;
import com.l2j4team.gameserver.network.serverpackets.MoveToPawn;
import com.l2j4team.gameserver.network.serverpackets.MyTargetSelected;
import com.l2j4team.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2j4team.gameserver.network.serverpackets.ValidateLocation;

import com.l2j4team.commons.random.Rnd;

/**
 * @author Zaun
 */
public class BossEventNpc extends Folk
{
	/**
	 * @param objectId
	 * @param template
	 */
	public BossEventNpc(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onAction(Player player)
	{
		if (this != player.getTarget())
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			if (!canInteract(player))
				player.getAI().setIntention(CtrlIntention.INTERACT, this);
			else
			{
				// Rotate the player to face the instance
				player.sendPacket(new MoveToPawn(player, this, Npc.INTERACTION_DISTANCE));

				if (hasRandomAnimation())
					onRandomAnimation(Rnd.get(8));

				showMainWindow(player);

				// Send ActionFailed to the player in order to avoid he stucks
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}

	private void showMainWindow(Player player)
	{

		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile("data/html/mods/BossEvent/BossEventJoin.htm");
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		html.replace("%regCount%", String.valueOf(BossEvent.getInstance().eventPlayers.size()));
		player.sendPacket(html);

		if (BossEvent.getInstance().isRegistered(player))
		{
			html.setFile("data/html/mods/BossEvent/BossEventCancel.htm");
			html.replace("%objectId%", String.valueOf(getObjectId()));
			html.replace("%npcname%", getName());
			html.replace("%regCount%", String.valueOf(BossEvent.getInstance().eventPlayers.size()));
			player.sendPacket(html);
		}
	}

	@Override
	public void onBypassFeedback(Player activeChar, String command)
	{

		super.onBypassFeedback(activeChar, command);
		if (command.startsWith("register"))
		{
			if (BossEvent.getInstance().getState() != EventState.REGISTRATION)
			{
				activeChar.sendMessage("Boss Event is not running!");
				return;
			}
			if (!BossEvent.getInstance().isRegistered(activeChar))
			{
				if (BossEvent.getInstance().addPlayer(activeChar))
				{
					activeChar.sendMessage("You have been successfully registered in Boss Event!");
				}

			}
			else
			{
				if (BossEvent.getInstance().removePlayer(activeChar))
				{
					activeChar.sendMessage("You have been successfully removed of Boss Event!");
				}
			}
		}
	}

}