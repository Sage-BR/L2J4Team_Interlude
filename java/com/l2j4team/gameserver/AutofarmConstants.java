package com.l2j4team.gameserver;

import com.l2j4team.Config;

import java.util.Arrays;
import java.util.List;

public class AutofarmConstants
{
	public static Integer shortcutsPageIndex = Config.AUTO_FARM_BAR;
	public static Integer targetingRadius = Config.AUTO_FARM_RADIUS;
	public static Integer lowLifePercentageThreshold = 30;
	public static Integer useMpPotsPercentageThreshold = 30;
	public static Integer useHpPotsPercentageThreshold = 30;
	public static Integer mpPotItemId = 728;
	public static Integer hpPotItemId = 1539;
	public static Integer hpPotSkillId = 2037;

	public static List<Integer> attackSlots = Arrays.asList(0, 1, 2, 3);
	public static List<Integer> chanceSlots = Arrays.asList(4, 5);
	public static List<Integer> selfSlots = Arrays.asList(6, 7, 8, 9);
	public static List<Integer> lowLifeSlots = Arrays.asList(10, 11);
}