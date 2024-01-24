package com.l2j4team.gameserver;

import com.l2j4team.Config;
import com.l2j4team.L2DatabaseFactory;
import com.l2j4team.gameserver.geoengine.GeoEngine;
import com.l2j4team.gameserver.handler.IItemHandler;
import com.l2j4team.gameserver.handler.ItemHandler;
import com.l2j4team.gameserver.model.L2ShortCut;
import com.l2j4team.gameserver.model.L2Skill;
import com.l2j4team.gameserver.model.WorldObject;
import com.l2j4team.gameserver.model.WorldRegion;
import com.l2j4team.gameserver.model.actor.Creature;
import com.l2j4team.gameserver.model.actor.Summon;
import com.l2j4team.gameserver.model.actor.ai.CtrlEvent;
import com.l2j4team.gameserver.model.actor.ai.CtrlIntention;
import com.l2j4team.gameserver.model.actor.ai.NextAction;
import com.l2j4team.gameserver.model.actor.instance.Monster;
import com.l2j4team.gameserver.model.actor.instance.Pet;
import com.l2j4team.gameserver.model.actor.instance.Player;
import com.l2j4team.gameserver.model.holder.IntIntHolder;
import com.l2j4team.gameserver.model.item.instance.ItemInstance;
import com.l2j4team.gameserver.model.item.kind.Item;
import com.l2j4team.gameserver.model.item.type.ActionType;
import com.l2j4team.gameserver.model.item.type.EtcItemType;
import com.l2j4team.gameserver.model.item.type.WeaponType;
import com.l2j4team.gameserver.model.itemcontainer.Inventory;
import com.l2j4team.gameserver.network.SystemMessageId;
import com.l2j4team.gameserver.network.serverpackets.ActionFailed;
import com.l2j4team.gameserver.network.serverpackets.ItemList;
import com.l2j4team.gameserver.network.serverpackets.PetItemList;
import com.l2j4team.gameserver.network.serverpackets.SystemMessage;
import com.l2j4team.gameserver.scripting.Quest;
import com.l2j4team.gameserver.scripting.QuestState;
import com.l2j4team.gameserver.templates.skills.L2SkillType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.l2j4team.commons.concurrent.ThreadPool;
import com.l2j4team.commons.math.MathUtil;

public class AutofarmPlayerRoutine
{
	public static final Logger _log = Logger.getLogger(AutofarmPlayerRoutine.class.getName());
	private final Player player;
	private Creature committedTarget = null;
	
	public AutofarmPlayerRoutine(Player player)
	{
		this.player = player;
	}
	
	public void executeRoutine()
	{
		checkSpoil();
		targetEligibleCreature();
		checkManaPots();
		checkHealthPots();
		attack();
		checkSpoil();
	}
	
	private void attack()
	{
		boolean shortcutsContainAttack = shotcutsContainAttack();
		if (shortcutsContainAttack)
		{
			physicalAttack();
		}
		
		useAppropriateSpell();
		
		if (shortcutsContainAttack)
		{
			physicalAttack();
		}
	}
	
	private void useAppropriateSpell()
	{
		
		L2Skill chanceSkill = nextAvailableSkill(getChanceSpells(), AutofarmSpellType.Chance);
		
		if (chanceSkill != null)
		{
			useMagicSkill(chanceSkill, false);
			return;
		}
		
		L2Skill lowLifeSkill = nextAvailableSkill(getLowLifeSpells(), AutofarmSpellType.LowLife);
		
		if (lowLifeSkill != null)
		{
			useMagicSkill(lowLifeSkill, false);
			return;
		}
		
		L2Skill selfSkills = nextAvailableSkill(getSelfSpells(), AutofarmSpellType.Self);
		
		if (selfSkills != null)
		{
			useMagicSkill(selfSkills, true);
			return;
		}
		
		L2Skill attackSkill = nextAvailableSkill(getAttackSpells(), AutofarmSpellType.Attack);
		
		if (attackSkill != null)
		{
			useMagicSkill(attackSkill, false);
			return;
		}
	}
	
	public L2Skill nextAvailableSkill(List<Integer> skillIds, AutofarmSpellType spellType)
	{
		for (Integer skillId : skillIds)
		{
			L2Skill skill = player.getSkill(skillId);
			
			if (skill == null)
				continue;
			
			if (!player.checkDoCastConditions(skill))
				continue;
			
			if (spellType == AutofarmSpellType.Chance && getMonsterTarget() != null)
			{
				if (isSpoil(skillId))
				{
					if (monsterIsAlreadySpoiled())
					{
						continue;
					}
					return skill;
				}
				
				if (getMonsterTarget().getFirstEffect(skillId) == null)
				{
					return skill;
				}
				continue;
			}
			
			if (spellType == AutofarmSpellType.LowLife && getMonsterTarget() != null && getHpPercentage() > AutofarmConstants.lowLifePercentageThreshold)
			{
				break;
			}
			
			if (spellType == AutofarmSpellType.Self)
			{
				if (skill.isToggle() && player.getFirstEffect(skillId) == null)
					return skill;
				
				if (player.getFirstEffect(skillId) == null)
				{
					return skill;
				}
				
				continue;
			}
			
			return skill;
		}
		
		return null;
	}
	
	private void checkHealthPots()
	{
		if (getHpPercentage() <= AutofarmConstants.useHpPotsPercentageThreshold)
		{
			if (player.getFirstEffect(AutofarmConstants.hpPotSkillId) != null)
			{
				return;
			}
			
			ItemInstance hpPots = player.getInventory().getItemByItemId(AutofarmConstants.hpPotItemId);
			if (hpPots != null)
			{
				useItem(hpPots);
			}
		}
	}
	
	private void checkManaPots()
	{
		
		if (getMpPercentage() <= AutofarmConstants.useMpPotsPercentageThreshold)
		{
			ItemInstance mpPots = player.getInventory().getItemByItemId(AutofarmConstants.mpPotItemId);
			if (mpPots != null)
			{
				useItem(mpPots);
			}
		}
	}
	
	private void checkSpoil()
	{
		if (canBeSweepedByMe() && getMonsterTarget().isDead())
		{
			L2Skill sweeper = player.getSkill(42);
			if (sweeper == null)
				return;
			
			useMagicSkill(sweeper, false);
		}
	}
	
	private Double getHpPercentage()
	{
		return player.getCurrentHp() * 100.0f / player.getMaxHp();
	}
	
	private Double getMpPercentage()
	{
		return player.getCurrentMp() * 100.0f / player.getMaxMp();
	}
	
	private boolean canBeSweepedByMe()
	{
		return getMonsterTarget() != null && getMonsterTarget().isDead() && getMonsterTarget().getSpoilerId() == player.getObjectId();
	}
	
	private boolean monsterIsAlreadySpoiled()
	{
		return getMonsterTarget() != null && getMonsterTarget().getSpoilerId() != 0;
	}
	
	private static boolean isSpoil(Integer skillId)
	{
		return skillId == 254 || skillId == 302;
	}
	
	private List<Integer> getAttackSpells()
	{
		return getSpellsInSlots(AutofarmConstants.attackSlots);
	}
	
	private List<Integer> getSpellsInSlots(List<Integer> attackSlots)
	{
		int skillbar = getAutofarmSkillbar(player.getObjectId());
		
		return Arrays.stream(player.getAllShortCuts()).filter(shortcut -> shortcut.getPage() == skillbar && shortcut.getType() == L2ShortCut.TYPE_SKILL && attackSlots.contains(shortcut.getSlot())).map(L2ShortCut::getId).collect(Collectors.toList());
	}
	
	private List<Integer> getChanceSpells()
	{
		return getSpellsInSlots(AutofarmConstants.chanceSlots);
	}
	
	private List<Integer> getSelfSpells()
	{
		return getSpellsInSlots(AutofarmConstants.selfSlots);
	}
	
	private List<Integer> getLowLifeSpells()
	{
		return getSpellsInSlots(AutofarmConstants.lowLifeSlots);
	}
	
	private boolean shotcutsContainAttack()
	{
		int skillbar = getAutofarmSkillbar(player.getObjectId());
		return Arrays.stream(player.getAllShortCuts()).anyMatch(shortcut -> shortcut.getPage() == skillbar && shortcut.getType() == L2ShortCut.TYPE_ACTION && shortcut.getId() == 2);
	}
	
	private void castSpellWithAppropriateTarget(L2Skill skill, Boolean forceOnSelf)
	{
		if (forceOnSelf)
		{
			WorldObject oldTarget = player.getTarget();
			player.setTarget(player);
			player.useMagic(skill, false, false);
			player.setTarget(oldTarget);
			return;
		}
		if (!player.isInRespectHunt() && !isMonsterBeingAttacked((Monster) player.getTarget()))
		{
			player.useMagic(skill, false, false);
		}
	}
	
	private void physicalAttack()
	{
		if (!(player.getTarget() instanceof Monster))
		{
			return;
		}
		
		Creature target = (Monster) player.getTarget();
		
		if (target.isAutoAttackable(player))
		{
			if (GeoEngine.getInstance().canSeeTarget(player, target))
			{
				
				if (isRespectHuntEnabled(player.getObjectId()))
				{
					
					if (isMonsterBeingAttacked(target))
					{
						player.setTarget(null);
						return;
					}
				}
				player.getAI().setIntention(CtrlIntention.ATTACK, target);
				player.onActionRequest();
			}
		}
		else
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			
			if (GeoEngine.getInstance().canSeeTarget(player, target))
				player.getAI().setIntention(CtrlIntention.FOLLOW, target);
		}
	}
	
	public void targetEligibleCreature()
	{
		if (committedTarget != null)
		{
			if (!committedTarget.isDead() && GeoEngine.getInstance().canSeeTarget(player, committedTarget)/* && !player.isMoving() */)
			{
				return;
			}
			committedTarget = null;
			player.setTarget(null);
		}
		
		int playerObjectId = player.getObjectId();
		int radiusFarm = getAutofarmRadius(playerObjectId);
		
		List<Monster> targets = getKnownMonstersInRadius(player, radiusFarm, creature -> GeoEngine.getInstance().canMoveToTarget(player.getX(), player.getY(), player.getZ(), creature.getX(), creature.getY(), creature.getZ()) && !creature.isDead());
		
		if (isRespectHuntEnabled(player.getObjectId()))
		{
			targets = targets.stream().filter(monster -> !isMonsterBeingAttacked(monster)).collect(Collectors.toList());
			
			if (targets.isEmpty())
			{
				return;
			}
		}
		
		Optional<Monster> closestTarget = targets.stream().min((o1, o2) -> (int) MathUtil.calculateDistance(o1, o2, false));
		if (closestTarget.isPresent())
		{
			committedTarget = closestTarget.get();
			player.setTarget(closestTarget.get());
		}
		else
		{
			return;
		}
	}
	
	private boolean isMonsterBeingAttacked(Creature creature)
	{
		if (!(creature instanceof Monster))
		{
			return false;
		}
		
		Monster monster = (Monster) creature;
		
		for (Creature otherPlayer : monster.getAttackByList())
		{
			if (!otherPlayer.isDead() && !otherPlayer.equals(player))
			{
				return true;
			}
		}
		return false;
	}
	
	private static boolean isRespectHuntEnabled(int charId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(); PreparedStatement statement = con.prepareStatement("SELECT respecthunt FROM Characters_Autofarm WHERE obj_id=?"))
		{
			statement.setInt(1, charId);
			try (ResultSet resultSet = statement.executeQuery())
			{
				if (resultSet.next())
				{
					String respectHuntStatus = resultSet.getString("respecthunt");
					return respectHuntStatus.equalsIgnoreCase("On");
				}
			}
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, "Failed to retrieve RespectHunt status for character " + charId, e);
		}
		return false;
	}
	
	@SuppressWarnings("static-method")
	public final List<Monster> getKnownMonstersInRadius(Player player, int radius, Function<Monster, Boolean> condition)
	{
		final WorldRegion region = player.getRegion();
		if (region == null)
			return Collections.emptyList();
		
		final List<Monster> result = new ArrayList<>();
		
		for (WorldRegion reg : region.getSurroundingRegions())
		{
			for (WorldObject obj : reg.getObjects())
			{
				if (!(obj instanceof Monster) || !MathUtil.checkIfInRange(radius, player, obj, true) || !condition.apply((Monster) obj))
					continue;
				
				result.add((Monster) obj);
			}
		}
		
		return result;
	}
	
	public Monster getMonsterTarget()
	{
		if (!(player.getTarget() instanceof Monster))
		{
			return null;
		}
		
		return (Monster) player.getTarget();
	}
	
	private void useMagicSkill(L2Skill skill, Boolean forceOnSelf)
	{
		if (skill.getSkillType() == L2SkillType.RECALL && !Config.KARMA_PLAYER_CAN_TELEPORT && player.getKarma() > 0)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (skill.isToggle() && player.isMounted())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isOutOfControl())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isAttackingNow())
			player.getAI().setNextAction(new NextAction(CtrlEvent.EVT_READY_TO_ACT, CtrlIntention.CAST, () -> castSpellWithAppropriateTarget(skill, forceOnSelf)));
		else
		{
			castSpellWithAppropriateTarget(skill, forceOnSelf);
		}
	}
	
	public void useItem(ItemInstance item)
	{
		if (player.isInStoreMode())
		{
			player.sendPacket(SystemMessageId.ITEMS_UNAVAILABLE_FOR_STORE_MANUFACTURE);
			return;
		}
		
		if (player.getActiveTradeList() != null)
		{
			player.sendPacket(SystemMessageId.CANNOT_PICKUP_OR_USE_ITEM_WHILE_TRADING);
			return;
		}
		
		if (item == null)
			return;
		
		if (item.getItem().getType2() == Item.TYPE2_QUEST)
		{
			player.sendPacket(SystemMessageId.CANNOT_USE_QUEST_ITEMS);
			return;
		}
		
		if (player.isAlikeDead() || player.isStunned() || player.isSleeping() || player.isParalyzed() || player.isAfraid())
			return;
		
		if (!Config.KARMA_PLAYER_CAN_TELEPORT && player.getKarma() > 0)
		{
			final IntIntHolder[] sHolders = item.getItem().getSkills();
			if (sHolders != null)
			{
				for (IntIntHolder sHolder : sHolders)
				{
					final L2Skill skill = sHolder.getSkill();
					if (skill != null && (skill.getSkillType() == L2SkillType.TELEPORT || skill.getSkillType() == L2SkillType.RECALL))
						return;
				}
			}
		}
		
		if (player.isFishing() && item.getItem().getDefaultAction() != ActionType.fishingshot)
		{
			player.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
			return;
		}
		
		if (item.isPetItem())
		{
			if (!player.hasPet())
			{
				player.sendPacket(SystemMessageId.CANNOT_EQUIP_PET_ITEM);
				return;
			}
			
			final Summon pet = (player.getPet());
			
			/*
			 * if (!pet.canWear(item.getItem())) { player.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM); return; }
			 */
			
			if (pet.isDead())
			{
				player.sendPacket(SystemMessageId.CANNOT_GIVE_ITEMS_TO_DEAD_PET);
				return;
			}
			
			if (!pet.getInventory().validateCapacity(item))
			{
				player.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS);
				return;
			}
			
			if (!pet.getInventory().validateWeight(item, 1))
			{
				player.sendPacket(SystemMessageId.UNABLE_TO_PLACE_ITEM_YOUR_PET_IS_TOO_ENCUMBERED);
				return;
			}
			
			player.transferItem("Transfer", item.getObjectId(), 1, pet.getInventory(), pet);
			
			if (item.isEquipped())
			{
				pet.getInventory().unEquipItemInSlot(item.getLocationSlot());
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_TOOK_OFF_S1).addItemName(item));
			}
			else
			{
				pet.getInventory().equipPetItem(item);
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_PUT_ON_S1).addItemName(item));
			}
			
			player.sendPacket(new PetItemList((Pet) pet));
			pet.updateAndBroadcastStatus(1);
			return;
		}
		
		if (!item.isEquipped())
		{
			if (!item.getItem().checkCondition(player, player, true))
				return;
		}
		
		if (item.isEquipable())
		{
			if (player.isCastingNow() || player.isCastingSimultaneouslyNow())
			{
				player.sendPacket(SystemMessageId.CANNOT_USE_ITEM_WHILE_USING_MAGIC);
				return;
			}
			
			switch (item.getItem().getBodyPart())
			{
				case Item.SLOT_LR_HAND:
				case Item.SLOT_L_HAND:
				case Item.SLOT_R_HAND:
				{
					if (player.isMounted())
					{
						player.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
						return;
					}
					
					if (player.isCursedWeaponEquipped())
						return;
					
					break;
				}
			}
			
			if (player.isCursedWeaponEquipped() && item.getItemId() == 6408)
				return;
			
			if (player.isAttackingNow())
				ThreadPool.schedule(() -> {
					final ItemInstance itemToTest = player.getInventory().getItemByObjectId(item.getObjectId());
					if (itemToTest == null)
						return;
					
					player.useEquippableItem(itemToTest, false);
				}, player.getAttackEndTime() - System.currentTimeMillis());
			else
				player.useEquippableItem(item, true);
		}
		else
		{
			if (player.isCastingNow() && !(item.isPotion() || item.isElixir()))
				return;
			
			if (player.getAttackType() == WeaponType.FISHINGROD && item.getItem().getItemType() == EtcItemType.LURE)
			{
				player.getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, item);
				player.broadcastUserInfo();
				
				player.sendPacket(new ItemList(player, false));
				return;
			}
			
			final IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getEtcItem());
			if (handler != null)
				handler.useItem(player, item, false);
			
			for (Quest quest : item.getQuestEvents())
			{
				QuestState state = player.getQuestState(quest.getName());
				if (state == null || !state.isStarted())
					continue;
				
				quest.notifyItemUse(item, player, player.getTarget());
			}
		}
	}
	
	private static int getAutofarmSkillbar(int charId)
	{
		int skillbar = Config.AUTO_FARM_BAR;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(); PreparedStatement statement = con.prepareStatement("SELECT skillbar FROM Characters_Autofarm WHERE obj_id=?"))
		{
			statement.setInt(1, charId);
			try (ResultSet resultSet = statement.executeQuery())
			{
				if (resultSet.next())
				{
					skillbar = resultSet.getInt("skillbar");
				}
			}
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, "Failed to retrieve Autofarm skillbar for character " + charId, e);
		}
		
		return skillbar;
	}
	
	private static int getAutofarmRadius(int charId)
	{
		int radiusFarm = Config.AUTO_FARM_RADIUS;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(); PreparedStatement statement = con.prepareStatement("SELECT radiusfarm FROM Characters_Autofarm WHERE obj_id=?"))
		{
			statement.setInt(1, charId);
			try (ResultSet resultSet = statement.executeQuery())
			{
				if (resultSet.next())
				{
					radiusFarm = resultSet.getInt("radiusfarm");
				}
			}
		}
		catch (SQLException e)
		{
			_log.log(Level.WARNING, "Failed to retrieve Autofarm radius for character " + charId, e);
		}
		
		return radiusFarm;
	}
	
}