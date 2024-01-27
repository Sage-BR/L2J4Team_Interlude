/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package Base.auction;

/**
 * @author Anarchy
 */
public class AuctionItem
{
	private final int auctionId;
	private final int ownerId;
	private final int itemId;
	private final int count;
	private final int enchant;
	private final int costId;
	private final int costCount;

	public AuctionItem(int auctionId, int ownerId, int itemId, int count, int enchant, int costId, int costCount)
	{
		this.auctionId = auctionId;
		this.ownerId = ownerId;
		this.itemId = itemId;
		this.count = count;
		this.enchant = enchant;
		this.costId = costId;
		this.costCount = costCount;
	}

	public int getAuctionId()
	{
		return auctionId;
	}

	public int getOwnerId()
	{
		return ownerId;
	}

	public int getItemId()
	{
		return itemId;
	}

	public int getCount()
	{
		return count;
	}

	public int getEnchant()
	{
		return enchant;
	}

	public int getCostId()
	{
		return costId;
	}

	public int getCostCount()
	{
		return costCount;
	}
}