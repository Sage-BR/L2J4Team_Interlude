package com.l2j4team.gameserver.model.actor.instance;

import com.l2j4team.gameserver.model.actor.template.NpcTemplate;
import com.l2j4team.gameserver.model.base.ClassId;
import com.l2j4team.gameserver.model.base.ClassRace;

public final class VillageMasterDwarf extends VillageMaster
{
	public VillageMasterDwarf(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	protected final boolean checkVillageMasterRace(ClassId pclass)
	{
		if (pclass == null)
			return false;
		
		return pclass.getRace() == ClassRace.DWARF;
	}
}