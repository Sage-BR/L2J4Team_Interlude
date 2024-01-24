package com.l2j4team.gameserver.network.serverpackets;

import com.l2j4team.Config;
import com.l2j4team.L2DatabaseFactory;
import com.l2j4team.gameserver.data.sql.ClanTable;
import com.l2j4team.gameserver.model.CharSelectInfoPackage;
import com.l2j4team.gameserver.model.itemcontainer.Inventory;
import com.l2j4team.gameserver.model.pledge.Clan;
import com.l2j4team.gameserver.network.L2GameClient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import Base.Skin.DressMeData;
import Base.Skin.SkinPackage;

public class CharSelectInfo extends L2GameServerPacket
{
	private final String _loginName;
	private final int _sessionId;
	private int _activeId;
	private final CharSelectInfoPackage[] _characterPackages;
	
	public CharSelectInfo(String loginName, int sessionId)
	{
		_sessionId = sessionId;
		_loginName = loginName;
		_characterPackages = loadCharacterSelectInfo(_loginName);
		_activeId = -1;
	}
	
	public CharSelectInfo(String loginName, int sessionId, int activeId)
	{
		_sessionId = sessionId;
		_loginName = loginName;
		_characterPackages = loadCharacterSelectInfo(_loginName);
		_activeId = activeId;
	}
	
	public CharSelectInfoPackage[] getCharInfo()
	{
		return _characterPackages;
	}
	
	@Override
	protected final void writeImpl()
	{
		// passkey
		getClient().sendPacket(TutorialCloseHtml.STATIC_PACKET);
		
		int size = (_characterPackages.length);
		
		writeC(0x13);
		writeD(size);
		
		long lastAccess = 0L;
		
		if (_activeId == -1)
		{
			for (int i = 0; i < size; i++)
				if (lastAccess < _characterPackages[i].getLastAccess())
				{
					lastAccess = _characterPackages[i].getLastAccess();
					_activeId = i;
				}
		}
		
		for (int i = 0; i < size; i++)
		{
			CharSelectInfoPackage charInfoPackage = _characterPackages[i];
			
			writeS(charInfoPackage.getName());
			writeD(charInfoPackage.getCharId());
			writeS(_loginName);
			writeD(_sessionId);
			writeD(charInfoPackage.getClanId());
			writeD(0x00); // ??
			
			writeD(charInfoPackage.getSex());
			writeD(charInfoPackage.getRace());
			
			if (charInfoPackage.getClassId() == charInfoPackage.getBaseClassId())
				writeD(charInfoPackage.getClassId());
			else
				writeD(charInfoPackage.getBaseClassId());
			
			writeD(0x01); // active ??
			
			writeD(charInfoPackage.getX());
			writeD(charInfoPackage.getY());
			writeD(charInfoPackage.getZ());
			
			writeF(charInfoPackage.getCurrentHp());
			writeF(charInfoPackage.getCurrentMp());
			
			writeD(charInfoPackage.getSp());
			writeQ(charInfoPackage.getExp());
			writeD(charInfoPackage.getLevel());
			
			writeD(charInfoPackage.getKarma());
			writeD(charInfoPackage.getPkKills());
			
			writeD(charInfoPackage.getPvPKills());
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_HAIRALL));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_REAR));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_LEAR));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_NECK));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_RFINGER));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_LFINGER));
			writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_HEAD));
			if (Config.ALLOW_DRESS_ME_SYSTEM)
			{
				if (charInfoPackage.getWeaponSkinOption() > 0 && getWeaponOption(charInfoPackage.getWeaponSkinOption()) != null)
				{
					writeD(getWeaponOption(charInfoPackage.getWeaponSkinOption()).getWeaponId() != 0 ? getWeaponOption(charInfoPackage.getWeaponSkinOption()).getWeaponId() : charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
				}
				else
				{
					writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
				}
				
				writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_LHAND));
				
				if (charInfoPackage.getArmorSkinOption() > 0 && getArmorOption(charInfoPackage.getArmorSkinOption()) != null)
				{
					writeD(getArmorOption(charInfoPackage.getArmorSkinOption()).getGlovesId() != 0 ? getArmorOption(charInfoPackage.getArmorSkinOption()).getGlovesId() : charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES));
					writeD(getArmorOption(charInfoPackage.getArmorSkinOption()).getChestId() != 0 ? getArmorOption(charInfoPackage.getArmorSkinOption()).getChestId() : charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_CHEST));
					writeD(getArmorOption(charInfoPackage.getArmorSkinOption()).getLegsId() != 0 ? getArmorOption(charInfoPackage.getArmorSkinOption()).getLegsId() : charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_LEGS));
					writeD(getArmorOption(charInfoPackage.getArmorSkinOption()).getFeetId() != 0 ? getArmorOption(charInfoPackage.getArmorSkinOption()).getFeetId() : charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_FEET));
				}
				else
				{
					writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES));
					writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_CHEST));
					writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_LEGS));
					writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_FEET));
				}
				
				writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_BACK));
				
				if (charInfoPackage.getWeaponSkinOption() > 0 && getWeaponOption(charInfoPackage.getWeaponSkinOption()) != null)
				{
					writeD(getWeaponOption(charInfoPackage.getWeaponSkinOption()).getWeaponId() != 0 ? getWeaponOption(charInfoPackage.getWeaponSkinOption()).getWeaponId() : charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
				}
				else
				{
					writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
				}
				
				if (charInfoPackage.getHairSkinOption() > 0 && getHairOption(charInfoPackage.getHairSkinOption()) != null)
				{
					writeD(getHairOption(charInfoPackage.getHairSkinOption()).getHairId() != 0 ? getHairOption(charInfoPackage.getHairSkinOption()).getHairId() : charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_HAIR));
				}
				else
				{
					writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_HAIR));
				}
				
				if (charInfoPackage.getFaceSkinOption() > 0 && getFaceOption(charInfoPackage.getFaceSkinOption()) != null)
				{
					writeD(getFaceOption(charInfoPackage.getFaceSkinOption()).getFaceId() != 0 ? getFaceOption(charInfoPackage.getFaceSkinOption()).getFaceId() : charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_FACE));
				}
				else
				{
					writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_FACE));
				}
			}
			else
			{
				writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
				writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_LHAND));
				writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES));
				writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_CHEST));
				writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_LEGS));
				writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_FEET));
				writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_BACK));
				writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
				writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_HAIR));
				writeD(charInfoPackage.getPaperdollObjectId(Inventory.PAPERDOLL_FACE));
			}
			
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_HAIRALL));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_REAR));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_LEAR));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_NECK));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_RFINGER));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_LFINGER));
			writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
			
			if (Config.ALLOW_DRESS_ME_SYSTEM)
			{
				if (charInfoPackage.getWeaponSkinOption() > 0 && getWeaponOption(charInfoPackage.getWeaponSkinOption()) != null)
				{
					writeD(getWeaponOption(charInfoPackage.getWeaponSkinOption()).getWeaponId() != 0 ? getWeaponOption(charInfoPackage.getWeaponSkinOption()).getWeaponId() : charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
				}
				else
				{
					writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
				}
				
				writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
				
				if (charInfoPackage.getArmorSkinOption() > 0 && getArmorOption(charInfoPackage.getArmorSkinOption()) != null)
				{
					writeD(getArmorOption(charInfoPackage.getArmorSkinOption()).getGlovesId() != 0 ? getArmorOption(charInfoPackage.getArmorSkinOption()).getGlovesId() : charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
					writeD(getArmorOption(charInfoPackage.getArmorSkinOption()).getChestId() != 0 ? getArmorOption(charInfoPackage.getArmorSkinOption()).getChestId() : charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
					writeD(getArmorOption(charInfoPackage.getArmorSkinOption()).getLegsId() != 0 ? getArmorOption(charInfoPackage.getArmorSkinOption()).getLegsId() : charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
					writeD(getArmorOption(charInfoPackage.getArmorSkinOption()).getFeetId() != 0 ? getArmorOption(charInfoPackage.getArmorSkinOption()).getFeetId() : charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_FEET));
				}
				else
				{
					writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
					writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
					writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
					writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_FEET));
				}
				
				writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_BACK));
				
				if (charInfoPackage.getWeaponSkinOption() > 0 && getWeaponOption(charInfoPackage.getWeaponSkinOption()) != null)
				{
					writeD(getWeaponOption(charInfoPackage.getWeaponSkinOption()).getWeaponId() != 0 ? getWeaponOption(charInfoPackage.getWeaponSkinOption()).getWeaponId() : charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
				}
				else
				{
					writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
				}
				
				if (charInfoPackage.getHairSkinOption() > 0 && getHairOption(charInfoPackage.getHairSkinOption()) != null)
				{
					writeD(getHairOption(charInfoPackage.getHairSkinOption()).getHairId() != 0 ? getHairOption(charInfoPackage.getHairSkinOption()).getHairId() : charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
				}
				else
				{
					writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
				}
				
				if (charInfoPackage.getFaceSkinOption() > 0 && getFaceOption(charInfoPackage.getFaceSkinOption()) != null)
				{
					writeD(getFaceOption(charInfoPackage.getFaceSkinOption()).getFaceId() != 0 ? getFaceOption(charInfoPackage.getFaceSkinOption()).getFaceId() : charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_FACE));
				}
				else
				{
					writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_FACE));
				}
			}
			else
			{
				writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
				writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
				writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
				writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
				writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
				writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_FEET));
				writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_BACK));
				writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
				writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
				writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_FACE));
			}
			
			writeD(charInfoPackage.getHairStyle());
			writeD(charInfoPackage.getHairColor());
			writeD(charInfoPackage.getFace());
			
			writeF(charInfoPackage.getMaxHp());
			writeF(charInfoPackage.getMaxMp());
			
			writeD((charInfoPackage.getAccessLevel() > -100) ? ((charInfoPackage.getDeleteTimer() > 0) ? (int) ((charInfoPackage.getDeleteTimer() - System.currentTimeMillis()) / 1000) : 0) : -1);
			writeD(charInfoPackage.getClassId());
			writeD((i == _activeId) ? 0x01 : 0x00);
			writeC((charInfoPackage.getEnchantEffect() > 127) ? 127 : charInfoPackage.getEnchantEffect());
			writeD(charInfoPackage.getAugmentationId());
		}
		getClient().setCharSelection(getCharInfo());
	}
	
	private static CharSelectInfoPackage[] loadCharacterSelectInfo(String loginName)
	{
		CharSelectInfoPackage charInfopackage;
		List<CharSelectInfoPackage> characterList = new ArrayList<>();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT account_name, obj_Id, char_name, level, maxHp, curHp, maxMp, curMp, face, hairStyle, hairColor, sex, heading, x, y, z, exp, sp, karma, pvpkills, pkkills, clanid, race, classid, deletetime, cancraft, title, accesslevel, online, char_slot, lastAccess, base_class FROM characters WHERE account_name=?");
			statement.setString(1, loginName);
			ResultSet charList = statement.executeQuery();
			
			while (charList.next())// fills the package
			{
				charInfopackage = restoreChar(charList);
				if (charInfopackage != null)
					characterList.add(charInfopackage);
			}
			
			charList.close();
			statement.close();
			
			return characterList.toArray(new CharSelectInfoPackage[characterList.size()]);
		}
		catch (Exception e)
		{
			LOGGER.error(Level.WARNING, "Could not restore char info: " + e.getMessage(), e);
		}
		
		return new CharSelectInfoPackage[0];
	}
	
	private static void loadCharacterSubclassInfo(CharSelectInfoPackage charInfopackage, int ObjectId, int activeClassId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT exp, sp, level FROM character_subclasses WHERE char_obj_id=? && class_id=? ORDER BY char_obj_id");
			statement.setInt(1, ObjectId);
			statement.setInt(2, activeClassId);
			ResultSet charList = statement.executeQuery();
			
			if (charList.next())
			{
				charInfopackage.setExp(charList.getLong("exp"));
				charInfopackage.setSp(charList.getInt("sp"));
				charInfopackage.setLevel(charList.getInt("level"));
			}
			
			charList.close();
			statement.close();
			
		}
		catch (Exception e)
		{
			LOGGER.error(Level.WARNING, "Could not restore char subclass info: " + e.getMessage(), e);
		}
	}
	
	private static CharSelectInfoPackage restoreChar(ResultSet chardata) throws Exception
	{
		int objectId = chardata.getInt("obj_id");
		String name = chardata.getString("char_name");
		
		// See if the char must be deleted
		long deletetime = chardata.getLong("deletetime");
		if (deletetime > 0)
		{
			if (System.currentTimeMillis() > deletetime)
			{
				Clan clan = ClanTable.getInstance().getClan(chardata.getInt("clanid"));
				if (clan != null)
					clan.removeClanMember(objectId, 0);
				
				L2GameClient.deleteCharByObjId(objectId);
				return null;
			}
		}
		
		CharSelectInfoPackage charInfopackage = new CharSelectInfoPackage(objectId, name);
		charInfopackage.setAccessLevel(chardata.getInt("accesslevel"));
		charInfopackage.setLevel(chardata.getInt("level"));
		charInfopackage.setMaxHp(chardata.getInt("maxhp"));
		charInfopackage.setCurrentHp(chardata.getDouble("curhp"));
		charInfopackage.setMaxMp(chardata.getInt("maxmp"));
		charInfopackage.setCurrentMp(chardata.getDouble("curmp"));
		charInfopackage.setKarma(chardata.getInt("karma"));
		charInfopackage.setPkKills(chardata.getInt("pkkills"));
		charInfopackage.setPvPKills(chardata.getInt("pvpkills"));
		charInfopackage.setFace(chardata.getInt("face"));
		charInfopackage.setHairStyle(chardata.getInt("hairstyle"));
		charInfopackage.setHairColor(chardata.getInt("haircolor"));
		charInfopackage.setSex(chardata.getInt("sex"));
		
		charInfopackage.setExp(chardata.getLong("exp"));
		charInfopackage.setSp(chardata.getInt("sp"));
		charInfopackage.setClanId(chardata.getInt("clanid"));
		
		charInfopackage.setRace(chardata.getInt("race"));
		
		final int baseClassId = chardata.getInt("base_class");
		final int activeClassId = chardata.getInt("classid");
		
		charInfopackage.setX(chardata.getInt("x"));
		charInfopackage.setY(chardata.getInt("y"));
		charInfopackage.setZ(chardata.getInt("z"));
		
		// if is in subclass, load subclass exp, sp, lvl info
		if (baseClassId != activeClassId)
			loadCharacterSubclassInfo(charInfopackage, objectId, activeClassId);
		
		charInfopackage.setClassId(activeClassId);
		loadCharacterDressMeInfo(charInfopackage, objectId);
		
		// Get the augmentation id for equipped weapon
		int weaponObjId = charInfopackage.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND);
		if (weaponObjId < 1)
			weaponObjId = charInfopackage.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND);
		
		if (weaponObjId > 0)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				PreparedStatement statement = con.prepareStatement("SELECT attributes FROM augmentations WHERE item_id=?");
				statement.setInt(1, weaponObjId);
				ResultSet result = statement.executeQuery();
				
				if (result.next())
				{
					int augment = result.getInt("attributes");
					charInfopackage.setAugmentationId(augment == -1 ? 0 : augment);
				}
				
				result.close();
				statement.close();
			}
			catch (Exception e)
			{
				LOGGER.error(Level.WARNING, "Could not restore augmentation info: " + e.getMessage(), e);
			}
		}
		
		/*
		 * Check if the base class is set to zero and alse doesn't match with the current active class, otherwise send the base class ID. This prevents chars created before base class was introduced from being displayed incorrectly.
		 */
		if (baseClassId == 0 && activeClassId > 0)
			charInfopackage.setBaseClassId(activeClassId);
		else
			charInfopackage.setBaseClassId(baseClassId);
		
		charInfopackage.setDeleteTimer(deletetime);
		charInfopackage.setLastAccess(chardata.getLong("lastAccess"));
		
		return charInfopackage;
	}
	
	private static void loadCharacterDressMeInfo(final CharSelectInfoPackage charInfopackage, final int objectId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT obj_Id, armor_skins, armor_skin_option, weapon_skins, weapon_skin_option, hair_skins, hair_skin_option, face_skins, face_skin_option FROM characters_dressme_data WHERE obj_id=?"))
		{
			
			statement.setInt(1, objectId);
			try (ResultSet chardata = statement.executeQuery())
			{
				if (chardata.next())
				{
					charInfopackage.setArmorSkinOption(chardata.getInt("armor_skin_option"));
					charInfopackage.setWeaponSkinOption(chardata.getInt("weapon_skin_option"));
					charInfopackage.setHairSkinOption(chardata.getInt("hair_skin_option"));
					charInfopackage.setFaceSkinOption(chardata.getInt("face_skin_option"));
				}
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public SkinPackage getArmorOption(int option)
	{
		return (DressMeData.getInstance().getArmorSkinsPackage(option));
	}
	
	public SkinPackage getWeaponOption(int option)
	{
		return DressMeData.getInstance().getWeaponSkinsPackage(option);
	}
	
	public SkinPackage getHairOption(int option)
	{
		return DressMeData.getInstance().getHairSkinsPackage(option);
	}
	
	public SkinPackage getFaceOption(int option)
	{
		return DressMeData.getInstance().getFaceSkinsPackage(option);
	}
}