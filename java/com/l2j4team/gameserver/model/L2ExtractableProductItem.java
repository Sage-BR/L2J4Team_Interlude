package com.l2j4team.gameserver.model;

import com.l2j4team.gameserver.model.holder.IntIntHolder;

import java.util.List;

/**
 * @author -Nemesiss-, Zoey76
 */
public class L2ExtractableProductItem
{
	private final List<IntIntHolder> _items;
	private final double _chance;

	public L2ExtractableProductItem(List<IntIntHolder> items, double chance)
	{
		_items = items;
		_chance = chance;
	}

	public List<IntIntHolder> getItems()
	{
		return _items;
	}

	public double getChance()
	{
		return _chance;
	}
}