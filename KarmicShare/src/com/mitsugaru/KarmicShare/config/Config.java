/**
 * Config file mimicking DiddiZ's Config class file in LB. Tailored for this
 * plugin.
 * 
 * @author Mitsugaru
 */
package com.mitsugaru.KarmicShare.config;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lib.Mitsugaru.SQLibrary.Database.Query;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.mitsugaru.KarmicShare.KarmicShare;
import com.mitsugaru.KarmicShare.config.Update.ZeroPointFourteenItemObject;
import com.mitsugaru.KarmicShare.config.Update.ZeroPointTwoSixTwoItemObject;
import com.mitsugaru.KarmicShare.config.Update.ZeroPointTwoSixTwoPlayerObject;
import com.mitsugaru.KarmicShare.database.Table;
import com.mitsugaru.KarmicShare.inventory.Item;

public class Config
{
	// Class variables
	private KarmicShare plugin;
	public String host, port, database, user, password;
	public static String tablePrefix;
	public boolean useMySQL, statickarma, effects, debugTime, karmaDisabled,
			chests, importSQL, economy, blacklist;
	public int upper, lower, listlimit, playerKarmaDefault, karmaChange;
	public double upperPercent, lowerPercent;
	public final Map<Item, Integer> karma = new HashMap<Item, Integer>();

	// TODO ability to change config in-game

	// IDEA Ability to change the colors for all parameters
	// such as item name, amount, data value, id value, enchantment name,
	// enchantment lvl, page numbers, maybe even header titles
	/**
	 * Constructor and initializer
	 * 
	 * @param KarmicShare
	 *            plugin
	 */
	public Config(KarmicShare ks)
	{
		plugin = ks;
		// Grab config
		final ConfigurationSection config = ks.getConfig();
		// Hashmap of defaults
		final Map<String, Object> defaults = new HashMap<String, Object>();
		defaults.put("version", ks.getDescription().getVersion());
		defaults.put("mysql.use", false);
		defaults.put("mysql.host", "localhost");
		defaults.put("mysql.port", 3306);
		defaults.put("mysql.database", "minecraft");
		defaults.put("mysql.user", "username");
		defaults.put("mysql.password", "pass");
		defaults.put("mysql.tablePrefix", "ks_");
		defaults.put("mysql.import", false);
		defaults.put("karma.upperlimit", 200);
		defaults.put("karma.upperPercent", 0.85);
		defaults.put("karma.lowerlimit", -200);
		defaults.put("karma.lowerPercent", 0.15);
		defaults.put("karma.playerDefault", 0);
		defaults.put("karma.changeDefault", 1);
		defaults.put("karma.static", false);
		defaults.put("karma.disabled", false);
		defaults.put("karma.useEconomy", false);
		defaults.put("effects", true);
		defaults.put("listlimit", 10);
		defaults.put("chests", true);
		// TODO defaults.put("blacklist", false);
		// Insert defaults into config file if they're not present
		for (final Entry<String, Object> e : defaults.entrySet())
		{
			if (!config.contains(e.getKey()))
			{
				config.set(e.getKey(), e.getValue());
			}
		}
		// Save config
		ks.saveConfig();
		// Load variables from config
		useMySQL = config.getBoolean("mysql.use", false);
		host = config.getString("mysql.host", "localhost");
		port = config.getString("mysql.port", "3306");
		database = config.getString("mysql.database", "minecraft");
		user = config.getString("mysql.user", "user");
		password = config.getString("mysql.password", "password");
		tablePrefix = config.getString("mysql.prefix", "ks_");
		importSQL = config.getBoolean("mysql.import", false);
		statickarma = config.getBoolean("karma.static", false);
		upper = config.getInt("karma.upperlimit", 200);
		lower = config.getInt("karma.lowerlimit", -200);
		upperPercent = config.getDouble("karma.upperPercent", 0.85);
		lowerPercent = config.getDouble("karma.lowerPercent", 0.15);
		playerKarmaDefault = config.getInt("karma.playerDefault", 0);
		karmaChange = config.getInt("karma.changeDefault", 1);
		effects = config.getBoolean("effects", true);
		chests = config.getBoolean("chests", true);
		listlimit = config.getInt("listlimit", 10);
		debugTime = config.getBoolean("debugTime", false);
		karmaDisabled = config.getBoolean("karma.disabled", false);
		economy = config.getBoolean("karma.useEconomy", false);
		// TODO blacklist = config.getBoolean("blacklist", false);
		// Load config for item specific karma if not using static karma
		if (!statickarma && !karmaDisabled)
		{
			this.loadKarmaMap();
		}
		if (blacklist)
		{
			this.loadBlacklist();
		}

		// Finally, do a bounds check on parameters to make sure they are legal
	}

	public void set(String path, Object o)
	{
		final ConfigurationSection config = plugin.getConfig();
		config.set(path, o);
		plugin.saveConfig();
	}

	/**
	 * Loads the per-item karma values into a hashmap for later usage
	 */
	private void loadKarmaMap()
	{
		// Load karma file
		final YamlConfiguration karmaFile = this.karmaFile();
		// Load custom karma file into map
		for (final String entry : karmaFile.getKeys(false))
		{
			try
			{
				// Attempt to parse the nodes
				int key = Integer.parseInt(entry);
				// If it has child nodes, parse those as well
				if (karmaFile.isConfigurationSection(entry))
				{
					ConfigurationSection sec = karmaFile
							.getConfigurationSection(entry);
					for (final String dataValue : sec.getKeys(false))
					{
						int secondKey = Integer.parseInt(dataValue);
						int secondValue = sec.getInt(dataValue);
						if (key != 373)
						{
							karma.put(
									new Item(key, Byte
											.parseByte("" + secondKey),
											(short) secondKey), secondValue);
						}
						else
						{
							karma.put(new Item(key, Byte.parseByte("" + 0),
									(short) secondKey), secondValue);
						}
					}
				}
				else
				{
					int value = karmaFile.getInt(entry);
					karma.put(new Item(key, Byte.valueOf("" + 0), (short) 0),
							value);
				}
			}
			catch (final NumberFormatException ex)
			{
				plugin.getLogger().warning("Non-integer value for: " + entry);
				ex.printStackTrace();
			}
		}
		plugin.getLogger().info("Loaded custom karma values");
	}

	private void loadBlacklist()
	{
		// final YamlConfiguration blacklistFile = blacklistFile();
		// Load info into set
		// TODO test
		// final List<String> list = (List<String>)
		// blacklistFile.getList("blacklist", new ArrayList<String>());
	}

	/**
	 * Check if updates are necessary
	 */
	public void checkUpdate()
	{
		// Check if need to update
		ConfigurationSection config = plugin.getConfig();
		if (Double.parseDouble(plugin.getDescription().getVersion()) > Double
				.parseDouble(config.getString("version")))
		{
			// Update to latest version
			plugin.getLogger().info(
					"Updating to v" + plugin.getDescription().getVersion());
			this.update();
		}
	}

	/**
	 * This method is called to make the appropriate changes, most likely only
	 * necessary for database schema modification, for a proper update.
	 */
	private void update()
	{
		// Grab current version
		final double ver = Double.parseDouble(plugin.getConfig().getString(
				"version"));
		String query = "";
		// Updates to alpha 0.08
		if (ver < 0.08)
		{
			// Add enchantments column
			plugin.getLogger().info(
					"Altering items table to add enchantments column.");
			query = "ALTER TABLE items ADD enchantments TEXT;";
			plugin.getDatabaseHandler().standardQuery(query);
		}
		if (ver < 0.09)
		{
			// Add back durability column
			plugin.getLogger().info(
					"Altering items table to add durability column.");
			query = "ALTER TABLE items ADD durability TEXT;";
			plugin.getDatabaseHandler().standardQuery(query);
		}
		if (ver < 0.14)
		{
			// Revamp item table
			try
			{
				plugin.getLogger().info("Revamping item table");
				query = "SELECT * FROM items;";
				final List<ZeroPointFourteenItemObject> fourteen = new ArrayList<ZeroPointFourteenItemObject>();
				Query rs = plugin.getDatabaseHandler().select(query);
				if (rs.getResult().next())
				{
					do
					{
						String enchantments = rs.getResult().getString(
								"enchantments");
						if (!rs.getResult().wasNull())
						{
							fourteen.add(new ZeroPointFourteenItemObject(rs
									.getResult().getInt("itemid"), rs
									.getResult().getInt("amount"), rs
									.getResult().getByte("data"), rs
									.getResult().getShort("durability"),
									enchantments));
						}
						else
						{
							fourteen.add(new ZeroPointFourteenItemObject(rs
									.getResult().getInt("itemid"), rs
									.getResult().getInt("amount"), rs
									.getResult().getByte("data"), rs
									.getResult().getShort("durability"), ""));
						}

					} while (rs.getResult().next());
				}
				rs.closeQuery();
				// Drop item table
				plugin.getDatabaseHandler().standardQuery("DROP TABLE items;");
				// Create new table
				plugin.getDatabaseHandler()
						.createTable(
								"CREATE TABLE `items` (`id` INTEGER PRIMARY KEY, `itemid` SMALLINT UNSIGNED,`amount` INT,`data` TEXT,`durability` TEXT,`enchantments` TEXT, `groups` TEXT);");
				// Add back items
				for (ZeroPointFourteenItemObject bak : fourteen)
				{
					String fourteenItemQuery = "";
					if (bak.enchantments.equals(""))
					{
						fourteenItemQuery = "INSERT INTO items (itemid,amount,data,durability,groups) VALUES ('"
								+ bak.itemid
								+ "','"
								+ bak.amount
								+ "','"
								+ bak.data
								+ "','"
								+ bak.durability
								+ "','global');";
					}
					else
					{
						fourteenItemQuery = "INSERT INTO items (itemid,amount,data,durability,enchantments,groups) VALUES ('"
								+ bak.itemid
								+ "','"
								+ bak.amount
								+ "','"
								+ bak.data
								+ "','"
								+ bak.durability
								+ "','"
								+ bak.enchantments + "','global');";
					}
					plugin.getDatabaseHandler()
							.standardQuery(fourteenItemQuery);
				}
			}
			catch (SQLException e)
			{
				plugin.getLogger().warning("SQL Exception");
				e.printStackTrace();
			}
			// Add groups to players table
			plugin.getLogger().info(
					"Altering player table to add groups column.");
			query = "ALTER TABLE players ADD groups TEXT;";
			plugin.getDatabaseHandler().standardQuery(query);
			// Add the GLOBAL group
			plugin.getLogger().info("Adding global group to groups table.");
			query = "INSERT INTO groups (groupname) VALUES ('global');";
			plugin.getDatabaseHandler().standardQuery(query);
		}
		if (ver < 0.2)
		{
			// Drop newly created tables
			plugin.getLogger().info("Dropping empty tables.");
			plugin.getDatabaseHandler().standardQuery(
					"DROP TABLE " + Table.ITEMS.getName() + ";");
			plugin.getDatabaseHandler().standardQuery(
					"DROP TABLE " + Table.PLAYERS.getName() + ";");
			plugin.getDatabaseHandler().standardQuery(
					"DROP TABLE " + Table.GROUPS.getName() + ";");
			// Update tables to have prefix
			plugin.getLogger().info(
					"Renaming items table to '" + Table.ITEMS.getName() + "'.");
			query = "ALTER TABLE items RENAME TO " + Table.ITEMS.getName()
					+ ";";
			plugin.getDatabaseHandler().standardQuery(query);
			plugin.getLogger().info(
					"Renaming players table to '" + Table.PLAYERS.getName()
							+ "'.");
			query = "ALTER TABLE players RENAME TO " + Table.PLAYERS.getName()
					+ ";";
			plugin.getDatabaseHandler().standardQuery(query);
			plugin.getLogger().info(
					"Renaming groups table to '" + Table.GROUPS.getName()
							+ "'.");
			query = "ALTER TABLE groups RENAME TO " + Table.GROUPS.getName()
					+ ";";
			plugin.getDatabaseHandler().standardQuery(query);
		}
		if (ver < 0.3)
		{
			/**
			 * Rebuild groups table
			 */
			plugin.getLogger().info("Rebuilding groups table...");
			// Save old groups
			final List<String> groups = new ArrayList<String>();
			//Add global to be known
			groups.add("global");
			try
			{
				Query rs = plugin.getDatabaseHandler().select(
						"SELECT * FROM " + Table.GROUPS.getName() + ";");
				if (rs.getResult().next())
				{
					do
					{
						groups.add(rs.getResult().getString("groupname"));
					} while (rs.getResult().next());
				}
				rs.closeQuery();
			}
			catch (SQLException sql)
			{
				plugin.getLogger().warning("SQL Exception");
				sql.printStackTrace();
			}
			// Drop previous table
			plugin.getDatabaseHandler().standardQuery(
					"DROP TABLE " + Table.GROUPS.getName() + ";");
			// Recreate table
			if (useMySQL)
			{
				plugin.getDatabaseHandler()
						.createTable(
								"CREATE TABLE "
										+ Table.GROUPS.getName()
										+ " (id INT UNSIGNED NOT NULL AUTO_INCREMENT, groupname varchar(32) NOT NULL, UNIQUE (groupname), PRIMARY KEY (id));");
			}
			else
			{
				plugin.getDatabaseHandler()
						.createTable(
								"CREATE TABLE "
										+ Table.GROUPS.getName()
										+ " (id INTEGER PRIMARY KEY, groupname TEXT NOT NULL, UNIQUE (groupname));");
			}
			// Add back in groups
			for (final String group : groups)
			{
				plugin.getDatabaseHandler().standardQuery(
						"INSERT INTO " + Table.GROUPS.getName()
								+ " (groupname) VALUES('" + group + "');");
			}
			/**
			 * Rebuild player table
			 */
			plugin.getLogger().info("Rebuilding player table...");
			// Save old players
			List<ZeroPointTwoSixTwoPlayerObject> playerList = new ArrayList<ZeroPointTwoSixTwoPlayerObject>();
			try
			{
				Query rs = plugin.getDatabaseHandler().select(
						"SELECT * FROM " + Table.PLAYERS.getName());
				if (rs.getResult().next())
				{
					do
					{
						final String playerGroups = rs.getResult().getString(
								"groups");
						playerList.add(new ZeroPointTwoSixTwoPlayerObject(rs
								.getResult().getString("playername"), rs
								.getResult().getInt("karma"), playerGroups));
					} while (rs.getResult().next());
				}
				rs.closeQuery();
			}
			catch (SQLException sql)
			{
				plugin.getLogger().warning("SQL Exception");
				sql.printStackTrace();
			}
			// Drop previous table
			plugin.getDatabaseHandler().standardQuery(
					"DROP TABLE " + Table.PLAYERS.getName() + ";");
			// Recreate table
			if (useMySQL)
			{
				plugin.getDatabaseHandler()
						.createTable(
								"CREATE TABLE "
										+ Table.PLAYERS.getName()
										+ " (id INT UNSIGNED NOT NULL AUTO_INCREMENT, playername varchar(32) NOT NULL,karma INT NOT NULL, groups TEXT, UNIQUE (playername), PRIMARY KEY (id));");
			}
			else
			{
				plugin.getDatabaseHandler()
						.createTable(
								"CREATE TABLE "
										+ Table.PLAYERS.getName()
										+ " (id INTEGER PRIMARY KEY, playername varchar(32) NOT NULL,karma INT NOT NULL, groups TEXT, UNIQUE (playername));");
			}
			// Add them back in
			for (final ZeroPointTwoSixTwoPlayerObject player : playerList)
			{
				if (player.groups == null)
				{
					query = "INSERT INTO " + Table.PLAYERS.getName()
							+ " (playername, karma) VALUES('"
							+ player.playername + "','" + player.karma + "','"
							+ "');";
				}
				else
				{
					// Translate groups into ids
					final StringBuilder sb = new StringBuilder();
					if (player.groups.contains("&"))
					{
						for (String s : player.groups.split("&"))
						{
							int id = plugin.getDatabaseHandler().getGroupId(s);
							sb.append(id + "&");
						}
						// Remove extra &
						sb.deleteCharAt(sb.length() - 1);
					}
					else
					{
						sb.append(plugin.getDatabaseHandler().getGroupId(
								player.groups));
					}
					query = "INSERT INTO " + Table.PLAYERS.getName()
							+ " (playername, karma, groups) VALUES ('"
							+ player.playername + "','" + player.karma + "','"
							+ sb.toString() + "');";
				}
				plugin.getDatabaseHandler().standardQuery(query);
			}
			/**
			 * Rebuild items table
			 */
			plugin.getLogger().info("Rebuilding items table...");
			// Save old items
			List<ZeroPointTwoSixTwoItemObject> itemList = new ArrayList<ZeroPointTwoSixTwoItemObject>();
			try
			{
				Query rs = plugin.getDatabaseHandler().select(
						"SELECT * FROM " + Table.ITEMS.getName() + ";");
				if (rs.getResult().next())
				{
					do
					{
						final int id = rs.getResult().getInt("itemid");
						final int amount = rs.getResult().getInt("amount");
						final byte data = rs.getResult().getByte("data");
						final short dur = rs.getResult().getShort("durability");
						final String enchantments = rs.getResult().getString(
								"enchantments");
						final String itemGroups = rs.getResult().getString(
								"groups");
						itemList.add(new ZeroPointTwoSixTwoItemObject(id,
								amount, data, dur, enchantments, itemGroups));
					} while (rs.getResult().next());
				}
			}
			catch (SQLException sql)
			{
				plugin.getLogger().warning("SQL Exception");
				sql.printStackTrace();
			}
			// Drop previous table
			plugin.getDatabaseHandler().standardQuery(
					"DROP TABLE " + Table.ITEMS.getName() + ";");
			// Recreate table
			if (useMySQL)
			{
				plugin.getDatabaseHandler()
						.createTable(
								"CREATE TABLE "
										+ Table.ITEMS.getName()
										+ " (id INT UNSIGNED NOT NULL AUTO_INCREMENT, itemid SMALLINT UNSIGNED, amount INT NOT NULL, data TINYTEXT, durability TINYTEXT, enchantments TEXT, groups TEXT NOT NULL, PRIMARY KEY (id));");
			}
			else
			{
				plugin.getDatabaseHandler()
						.createTable(
								"CREATE TABLE "
										+ Table.ITEMS.getName()
										+ " (id INTEGER PRIMARY KEY, itemid SMALLINT UNSIGNED,amount INT NOT NULL,data TEXT,durability TEXT,enchantments TEXT, groups TEXT NOT NULL);");
			}
			// Add them back in
			for (ZeroPointTwoSixTwoItemObject item : itemList)
			{
				final int groupid = plugin.getDatabaseHandler().getGroupId(
						item.groups);
				query = "INSERT INTO " + Table.ITEMS.getName() +" (itemid,amount,data,durability,enchantments,groups) VALUES ('"
						+ item.itemid
						+ "','"
						+ item.amount
						+ "','"
						+ item.data
						+ "','"
						+ item.durability
						+ "','"
						+ item.enchantments
						+ "','" + groupid + "');";
				plugin.getDatabaseHandler().standardQuery(query);
			}
		}
		// Update version number in config.yml
		plugin.getConfig().set("version", plugin.getDescription().getVersion());
		plugin.saveConfig();
		plugin.getLogger().info("Upgrade complete");
	}

	/**
	 * Reloads info from yaml file(s)
	 */
	public void reloadConfig()
	{
		// Initial relaod
		plugin.reloadConfig();
		// Grab config
		ConfigurationSection config = plugin.getConfig();
		upper = config.getInt("karma.upperlimit", 200);
		lower = config.getInt("karma.lowerlimit", -200);
		upperPercent = config.getDouble("karma.upperPercent", 0.85);
		lowerPercent = config.getDouble("karma.lowerPercent", 0.15);
		playerKarmaDefault = config.getInt("karma.playerDefault", 0);
		karmaChange = config.getInt("karma.changeDefault", 1);
		effects = config.getBoolean("effects", true);
		chests = config.getBoolean("chests", false);
		listlimit = config.getInt("listlimit", 10);
		debugTime = config.getBoolean("debugTime", false);
		karmaDisabled = config.getBoolean("karma.disabled", false);
		economy = config.getBoolean("karma.useEconomy", false);
		blacklist = config.getBoolean("blacklist", false);
		// Load config for item specific karma if not using static karma
		if (!statickarma && !karmaDisabled)
		{
			// Clear old mappings
			karma.clear();
			// Reload karma mappings
			this.loadKarmaMap();
		}
		if (blacklist)
		{
			this.loadBlacklist();
		}
		// Check bounds
		this.boundsCheck();
		plugin.getLogger().info("Config reloaded");
	}

	/**
	 * Check the bounds on the parameters to make sure that all config variables
	 * are legal and usable by the plugin
	 */
	private void boundsCheck()
	{
		// Check upper and lower limits
		if (upper < lower)
		{
			upper = 200;
			lower = -200;
			plugin.getLogger()
					.warning(
							"Upper limit is smaller than lower limit. Reverting to defaults.");
		}
		// Check that we don't go beyond what the database can handle, via
		// smallint
		else if (Math.abs(upper) >= 30000 || Math.abs(lower) >= 30000)
		{
			upper = 200;
			lower = -200;
			plugin.getLogger()
					.warning(
							"Upper/lower limit is beyond bounds. Reverting to defaults.");
		}
		// Check percentages
		if (upperPercent < lowerPercent)
		{
			upperPercent = 0.85;
			lowerPercent = 0.15;
			plugin.getLogger()
					.warning(
							"Upper %-age is smaller than lower %-age. Reverting to defaults.");
		}
		else if (upperPercent > 1.0 || lowerPercent < 0)
		{
			upperPercent = 0.85;
			lowerPercent = 0.15;
			plugin.getLogger()
					.warning(
							"Upper %-age and/or lower %-age are beyond bounds. Reverting to defaults.");
		}
		// Check that the default karma is actually in range.
		if (playerKarmaDefault < lower || playerKarmaDefault > upper)
		{
			// Average out valid bounds to create valid default
			playerKarmaDefault = upper - ((lower + upper) / 2);
			plugin.getLogger()
					.warning(
							"Player karma default is out of range. Using average of the two.");
		}
		// Check that default karma change is not negative.
		if (karmaChange < 0)
		{
			karmaChange = 1;
			plugin.getLogger().warning(
					"Default karma rate is negative. Using default.");
		}
		// Check that list is actually going to output something, based on limit
		// given
		if (listlimit < 1)
		{
			listlimit = 10;
			plugin.getLogger().warning(
					"List limit is lower than 1. Using default.");
		}
	}

	/**
	 * Loads the karma file. Contains default values If the karma file isn't
	 * there, or if its empty, then load defaults.
	 * 
	 * @return YamlConfiguration file
	 */
	private YamlConfiguration karmaFile()
	{
		final File file = new File(plugin.getDataFolder().getAbsolutePath()
				+ "/karma.yml");
		final YamlConfiguration karmaFile = YamlConfiguration
				.loadConfiguration(file);
		if (karmaFile.getKeys(false).isEmpty())
		{
			// Defaults
			karmaFile.set("14", 5);
			karmaFile.set("15", 2);
			karmaFile.set("17.0", 2);
			karmaFile.set("17.1", 2);
			karmaFile.set("17.2", 2);
			karmaFile.set("19", 10);
			karmaFile.set("20", 3);
			karmaFile.set("22", 36);
			karmaFile.set("24", 2);
			karmaFile.set("35.0", 2);
			karmaFile.set("35.1", 2);
			karmaFile.set("35.2", 2);
			karmaFile.set("35.3", 2);
			karmaFile.set("35.4", 2);
			karmaFile.set("35.5", 2);
			karmaFile.set("35.6", 2);
			karmaFile.set("35.7", 2);
			karmaFile.set("35.8", 2);
			karmaFile.set("35.9", 2);
			karmaFile.set("35.10", 2);
			karmaFile.set("35.11", 2);
			karmaFile.set("35.12", 2);
			karmaFile.set("35.13", 2);
			karmaFile.set("35.14", 2);
			karmaFile.set("35.15", 2);
			karmaFile.set("41", 54);
			karmaFile.set("45", 6);
			karmaFile.set("47", 6);
			karmaFile.set("49", 6);
			karmaFile.set("57", 225);
			karmaFile.set("89", 4);
			karmaFile.set("102", 12);
			karmaFile.set("264", 25);
			karmaFile.set("265", 3);
			karmaFile.set("266", 6);
			karmaFile.set("322", 10);
			karmaFile.set("331", 2);
			karmaFile.set("351.4", 4);
			// Insert defaults into config file if they're not present
			try
			{
				// Save the file
				karmaFile.save(file);
			}
			catch (IOException e1)
			{
				plugin.getLogger().warning(
						"File I/O Exception on saving karma list");
				e1.printStackTrace();
			}
		}
		return karmaFile;
	}

	@SuppressWarnings("unused")
	private YamlConfiguration blacklistFile()
	{
		final File file = new File(plugin.getDataFolder().getAbsolutePath()
				+ "/blacklist.yml");
		final YamlConfiguration blacklistFile = YamlConfiguration
				.loadConfiguration(file);
		if (!file.exists())
		{
			try
			{
				// Save the file
				blacklistFile.save(file);
			}
			catch (IOException e1)
			{
				plugin.getLogger().warning(
						"File I/O Exception on saving blacklist");
				e1.printStackTrace();
			}
		}
		return blacklistFile;
	}
}