/**
 * KarmicShare
 * CraftBukkit plugin that allows for players to
 * share items via a community pool. Karma system
 * in place so that players cannot leech from the
 * item pool.
 *
 * @author Mitsugaru
 */
package com.mitsugaru.KarmicShare;

import java.util.Vector;
import java.util.logging.Logger;

import lib.PatPeter.SQLibrary.SQLite;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import de.diddiz.LogBlockQuestioner.LogBlockQuestionerPlayerListener;
import de.diddiz.LogBlockQuestioner.Question;
import de.diddiz.LogBlockQuestioner.QuestionsReaper;

public class KarmicShare extends JavaPlugin {
	// Class variables
	private SQLite database;
	private Logger syslog;
	public static final String prefix = "[KarmicShare]";
	private Commander commander;
	private Config config;
	private PermCheck perm;
	private int cleantask;
	public final Vector<Question> questions = new Vector<Question>();
	public boolean hasSpout;

	// IDEA Score board on karma?
	// TODO Mod commands to remove items

	/**
	 * Method that is called when plugin is disabled
	 */
	@Override
	public void onDisable() {
		// Save config
		this.saveConfig();
		// Stop cleaner task
		if (cleantask != -1)
		{
			getServer().getScheduler().cancelTask(cleantask);
		}
		// Disconnect from sql database
		if (database.checkConnection())
		{
			// Close connection
			database.close();
		}
		syslog.info(prefix + " Plugin disabled");

	}

	@Override
	public void onLoad() {
		// Logger
		syslog = this.getServer().getLogger();
		// Config
		config = new Config(this);
		// TODO MySQL support
		// Connect to sql database
		database = new SQLite(syslog, prefix, "pool", this.getDataFolder()
				.getAbsolutePath());
		// Check if item table exists
		if (!database.checkTable("items"))
		{
			syslog.info(prefix + " Created item table");
			database.createTable("CREATE TABLE `items` (`id` INTEGER PRIMARY KEY, `itemid` SMALLINT UNSIGNED,`amount` INT,`data` TEXT,`durability` TEXT,`enchantments` TEXT, `groups` TEXT);");
		}
		// Check if player table exists
		if (!database.checkTable("players"))
		{
			syslog.info(prefix + " Created player table");
			// Schema: playername, karma
			// Karma works with 0 being neutral, postive and negative :: good
			// and bad.
			// Past certain boundary, do not increase/decrease.
			// Boundary must be within 30000 high or low, as per SMALLINT
			database.createTable("CREATE TABLE `players` (`playername` varchar(32) NOT NULL,`karma` INT NOT NULL, `groups` TEXT, UNIQUE (`playername`));");
		}
		if (!database.checkTable("groups"))
		{
			syslog.info(prefix + " Created groups table");
			database.createTable("CREATE TABLE `groups` (`groupname` TEXT NOT NULL, UNIQUE (`groupname`));");
		}
	}

	/**
	 * Method that is called when plugin is enabled
	 */
	@Override
	public void onEnable() {
		// Config update
		config.checkUpdate();

		// Create permission handler
		perm = new PermCheck(this);

		// Grab Commander to handle commands
		commander = new Commander(this);
		getCommand("ks").setExecutor(commander);

		// Grab plugin manager
		final PluginManager pm = this.getServer().getPluginManager();

		// Use bundled package of logblockquestioner.
		this.getServer()
				.getPluginManager()
				.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS,
						new LogBlockQuestionerPlayerListener(questions),
						Priority.Normal, this);
		this.getServer()
				.getScheduler()
				.scheduleSyncRepeatingTask(this,
						new QuestionsReaper(questions), 15000, 15000);

		// Generate listeners
		KSBlockListener blockListener = new KSBlockListener(this);
		KSPlayerListener playerListener = new KSPlayerListener(this);
		pm.registerEvent(Event.Type.SIGN_CHANGE, blockListener,
				Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener,
				Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener,
				Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_PHYSICS, blockListener,
				Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener,
				Priority.Normal, this);
		if (config.chests)
		{
			// Check for Spout plugin
			if (pm.isPluginEnabled("Spout"))
			{
				KSInventoryListener invListener = new KSInventoryListener(this);
				pm.registerEvent(Event.Type.CUSTOM_EVENT, invListener,
						Priority.Normal, this);
				hasSpout = true;
			}
			else
			{
				hasSpout = false;
				syslog.warning(prefix
						+ " Spout not found. Cannot use physical chests.");
			}
		}
		// Create cleaner task
		cleantask = getServer().getScheduler().scheduleAsyncRepeatingTask(this,
				new CleanupTask(), 1200, 1200);
		if (cleantask == -1)
		{
			syslog.warning(prefix + " Could not create cleaner task.");
		}
		syslog.info(prefix + " KarmicShare v"
				+ this.getDescription().getVersion() + " enabled");
	}

	public Commander getCommander() {
		return commander;
	}

	public PermCheck getPermissionHandler() {
		return perm;
	}

	/**
	 * Returns the console log object
	 *
	 * @return Logger object
	 */
	public Logger getLogger() {
		return syslog;
	}

	/**
	 * Returns SQLite database
	 *
	 * @return SQLite database
	 */
	public SQLite getLiteDB() {
		return database;
	}

	/**
	 * Returns Config object
	 *
	 * @return Config object
	 */
	public Config getPluginConfig() {
		return config;
	}

	public String ask(Player respondent, String questionMessage,
			String ... answers) {
		final Question question = new Question(respondent, questionMessage,
				answers);
		questions.add(question);
		return question.ask();
	}

	class CleanupTask implements Runnable {

		public CleanupTask() {
		}

		@Override
		public void run() {
			// Drop bad entries
			getLiteDB().standardQuery("DELETE FROM items WHERE amount<='0';");
		}
	}
}