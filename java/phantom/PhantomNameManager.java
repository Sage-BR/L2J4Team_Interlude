package phantom;

import com.l2j4team.gameserver.data.PlayerNameTable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2j4team.commons.random.Rnd;

public enum PhantomNameManager
{
	INSTANCE;
	
	public static final Logger _log = Logger.getLogger(PhantomNameManager.class.getName());
	private List<String> _fakePlayerNames;
	
	public void initialise()
	{
		loadWordlist();
	}
	
	public String getRandomAvailableName()
	{
		String name = getRandomNameFromWordlist();
		
		while (nameAlreadyExists(name))
		{
			name = getRandomNameFromWordlist();
		}
		
		return name;
	}
	
	private String getRandomNameFromWordlist()
	{
		return _fakePlayerNames.get(Rnd.get(0, _fakePlayerNames.size() - 1));
	}
	
	public List<String> getFakePlayerNames()
	{
		return _fakePlayerNames;
	}
	
	private void loadWordlist()
	{
		try (LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(new File("./config/custom/phantom/phantom_name.ini"))));)
		{
			String line;
			ArrayList<String> playersList = new ArrayList<>();
			while ((line = lnr.readLine()) != null)
			{
				if (line.trim().length() == 0 || line.startsWith("#"))
					continue;
				playersList.add(line);
			}
			_fakePlayerNames = playersList;
			_log.log(Level.INFO, String.format("Loaded %s fake player names.", _fakePlayerNames.size()));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static boolean nameAlreadyExists(String name)
	{
		return PlayerNameTable.getInstance().getPlayerObjectId(name) > 0;
	}
}
