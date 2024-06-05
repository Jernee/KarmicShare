package com.mitsugaru.KarmicShare.logic;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.mitsugaru.KarmicShare.KarmicShare;
import com.mitsugaru.KarmicShare.config.KarmaConfig;
import com.mitsugaru.KarmicShare.config.RootConfig;
import com.mitsugaru.KarmicShare.config.ConfigNode;
import com.mitsugaru.KarmicShare.database.Table;
import com.mitsugaru.KarmicShare.database.SQLibrary.Query;
import com.mitsugaru.KarmicShare.inventory.ComparableEnchantment;
import com.mitsugaru.KarmicShare.inventory.Item;
import com.mitsugaru.KarmicShare.permissions.PermissionHandler;
import com.mitsugaru.KarmicShare.permissions.PermissionNode;

public class ItemLogic {
    private static KarmicShare plugin;

    public static void init(KarmicShare ks) {
	plugin = ks;
    }

    public static boolean hasItem(Player player, ItemStack item, String group) {
	// Check if pool contains item requested + amount
	boolean has = false;
	final int groupId = Karma.getGroupId(group);
	if (groupId == -1) {
	    if (RootConfig.getBoolean(ConfigNode.DEBUG_ITEM)) {
		plugin.getLogger().warning("Unknown group: " + group);
	    }
	    player.sendMessage(ChatColor.RED + KarmicShare.TAG
		    + " Unknown group '" + ChatColor.GOLD + group
		    + ChatColor.RED + "'");
	    return has;
	}
	// SQL query to see if item is in pool
	// Create temp item to check if its a tool
	final Item temp = new Item(item);
	String query = "";
	int poolAmount = 0;
	if (temp.isTool()) {
	    if (RootConfig.getBoolean(ConfigNode.DEBUG_ITEM)) {
		plugin.getLogger().info("Tool item");
	    }
	    // Handle tools
	    // Grab all entries of the same tool id
	    String toolQuery = "SELECT * FROM " + Table.ITEMS.getName()
		    + " WHERE itemid='" + item.getTypeId() + "' AND groups='"
		    + groupId + "';";
	    Query toolRS = plugin.getDatabaseHandler().select(toolQuery);
	    try {
		if (toolRS.getResult().next()) {
		    do {
			poolAmount += toolRS.getResult().getInt("amount");
		    } while (toolRS.getResult().next());
		    if (poolAmount >= item.getAmount()) {
			// We have enough in pool that
			// was requested
			has = true;
		    }
		} else {
		    has = false;
		}
		toolRS.closeQuery();
	    } catch (SQLException e) {
		if (RootConfig.getBoolean(ConfigNode.DEBUG_ITEM)) {
		    plugin.getLogger().warning(
			    "SQLException on hasItem(" + player.getName() + ","
				    + item.toString() + "," + group + ")");
		}
		player.sendMessage(ChatColor.RED + KarmicShare.TAG
			+ "Could not retrieve item in pool!");
		e.printStackTrace();
		return false;
	    }
	} else if (temp.isPotion()) {
	    if (RootConfig.getBoolean(ConfigNode.DEBUG_ITEM)) {
		plugin.getLogger().info("Potion item");
	    }
	    // Separate check to see if its a potion and handle it
	    // via the durability info
	    query = "SELECT * FROM " + Table.ITEMS.getName()
		    + " WHERE itemid='" + item.getTypeId()
		    + "' AND durability='" + item.getDurability()
		    + "' AND groups='" + groupId + "';";
	    Query rs = plugin.getDatabaseHandler().select(query);

	    // Check ResultSet
	    try {
		if (rs.getResult().next()) {
		    // Item already in pool, check
		    // amount
		    poolAmount = rs.getResult().getInt("amount");
		    if (poolAmount >= item.getAmount()) {
			// We have enough in pool that
			// was requested
			has = true;
		    }
		} else {
		    // Item not in database
		    has = false;
		}
		rs.closeQuery();
	    } catch (SQLException e) {
		if (RootConfig.getBoolean(ConfigNode.DEBUG_ITEM)) {
		    plugin.getLogger().warning(
			    "SQLException on hasItem(" + player.getName() + ","
				    + item.toString() + "," + group + ")");
		}
		player.sendMessage(ChatColor.RED + KarmicShare.TAG
			+ "Could not retrieve item in pool!");
		e.printStackTrace();
		return false;
	    }
	} else {
	    if (RootConfig.getBoolean(ConfigNode.DEBUG_ITEM)) {
		plugin.getLogger().info("Normal item");
	    }
	    // Not a tool or potion
	    query = "SELECT * FROM " + Table.ITEMS.getName()
		    + " WHERE itemid='" + item.getTypeId() + "' AND data='"
		    + item.getData().getData() + "' AND groups='" + groupId
		    + "';";
	    Query rs = plugin.getDatabaseHandler().select(query);

	    // Check ResultSet
	    try {
		if (rs.getResult().next()) {
		    // Item already in pool, check
		    // amount
		    poolAmount = rs.getResult().getInt("amount");
		    if (poolAmount >= item.getAmount()) {
			// We have enough in pool that
			// was requested
			has = true;
		    }
		} else {
		    // Item not in database
		    has = false;
		}
		rs.closeQuery();
	    } catch (SQLException e) {
		if (RootConfig.getBoolean(ConfigNode.DEBUG_ITEM)) {
		    plugin.getLogger().warning(
			    "SQLException on hasItem(" + player.getName() + ","
				    + item.toString() + "," + group + ")");
		}
		player.sendMessage(ChatColor.RED + KarmicShare.TAG
			+ "Could not retrieve item in pool!");
		e.printStackTrace();
		return false;
	    }
	}
	if (RootConfig.getBoolean(ConfigNode.DEBUG_ITEM)) {
	    plugin.getLogger()
		    .info("Has item " + item.toString() + " : " + has);
	}
	return has;
    }

    public static int takeItem(Player player, ItemStack item, String group) {
	// Check if they have "take" permission
	if (!PermissionHandler.has(player, PermissionNode.TAKE)) {
	    if (RootConfig.getBoolean(ConfigNode.DEBUG_ITEM)) {
		plugin.getLogger().info(
			player.getName() + " lacks take permission");
	    }
	    player.sendMessage(ChatColor.RED + KarmicShare.TAG
		    + " Lack permission: KarmicShare.take");
	    return -1;
	}
	final int groupId = Karma.getGroupId(group);
	if (groupId == -1) {
	    if (RootConfig.getBoolean(ConfigNode.DEBUG_ITEM)) {
		plugin.getLogger().warning("Unknown group: " + group);
	    }
	    player.sendMessage(ChatColor.RED + KarmicShare.TAG
		    + " Unknown group '" + ChatColor.GOLD + group
		    + ChatColor.RED + "'");
	    return -1;
	}
	int karma = 0;
	if (!RootConfig.getBoolean(ConfigNode.KARMA_DISABLED)) {
	    if (!PermissionHandler.has(player, PermissionNode.IGNORE_KARMA)) {
		if (!(RootConfig.getBoolean(ConfigNode.KARMA_IGNORE_SELF_GROUP) && group
			.equalsIgnoreCase("self_" + player.getName()))) {
		    // Check karma before anything
		    karma = RootConfig.getInt(ConfigNode.KARMA_PLAYER_DEFAULT);
		    try {
			karma = Karma.getPlayerKarma(player.getName());
			if (karma <= RootConfig
				.getInt(ConfigNode.KARMA_LOWER_LIMIT)) {
			    // They are at the limit, or somehow past that for
			    // whatever reason
			    if (RootConfig.getBoolean(ConfigNode.DEBUG_ITEM)) {
				plugin.getLogger().info(
					"Karma at limit: " + player.getName());
			    }
			    player.sendMessage(ChatColor.RED + KarmicShare.TAG
				    + "Your karma is at the limit!");
			    return -1;
			}
		    } catch (SQLException e1) {
			if (RootConfig.getBoolean(ConfigNode.DEBUG_ITEM)) {
			    plugin.getLogger().warning(
				    "Could not retrieve karma: "
					    + player.getName());
			}
			player.sendMessage(ChatColor.RED + KarmicShare.TAG
				+ " Could not retrieve player karma");
			e1.printStackTrace();
			return -1;
		    }
		}
	    }
	}
	int amount = item.getAmount();
	String query = "";
	boolean has = hasItem(player, item, group);
	Item temp = new Item(item);
	try {
	    if (!has) {
		if (RootConfig.getBoolean(ConfigNode.DEBUG_ITEM)) {
		    plugin.getLogger().info("Item not available");
		}
		player.sendMessage(ChatColor.RED + KarmicShare.TAG
			+ " Item is no longer available.");
		return -1;
	    }
	    boolean hasKarma = false;
	    if (!RootConfig.getBoolean(ConfigNode.KARMA_DISABLED)
		    && !PermissionHandler.has(player,
			    PermissionNode.IGNORE_KARMA)) {
		if (!(RootConfig.getBoolean(ConfigNode.KARMA_IGNORE_SELF_GROUP) && group
			.equalsIgnoreCase("self_" + player.getName()))) {
		    // Check karma again, before giving item, to
		    // adjust amount
		    // based on karma and karma multipliers
		    int karmaAdj = 0;
		    boolean staticKarma = false;
		    if (RootConfig.getBoolean(ConfigNode.KARMA_STATIC)) {
			staticKarma = true;
		    } else {
			// Using per-item karma
			Item[] karmaList = KarmaConfig.VALUES.keySet().toArray(
				new Item[0]);
			// Check if requested item is in the
			// karma list
			for (Item k : karmaList) {
			    if (k.areSame(temp)) {
				// Item karma needs to be
				// adjusted
				hasKarma = true;
			    }
			}
			if (hasKarma) {
			    try {
				karmaAdj = karma
					+ (KarmaConfig.VALUES.get(temp).getTakeValue() * amount * -1);
				if (karmaAdj < RootConfig
					.getInt(ConfigNode.KARMA_LOWER_LIMIT)) {
				    // They went beyond the
				    // lower limit
				    // adjust amount given based
				    // on karma now
				    int tempKarma = Math.abs(karmaAdj)
					    - Math.abs(RootConfig
						    .getInt(ConfigNode.KARMA_LOWER_LIMIT));
				    int div = tempKarma
					    / KarmaConfig.VALUES.get(temp).getTakeValue();
				    int rem = tempKarma
					    % KarmaConfig.VALUES.get(temp).getTakeValue();
				    if (rem != 0) {
					div++;
				    }
				    amount -= div;
				    if (amount <= 0) {
					// Cannot give any items
					// as they'd go beyond
					// karma limit
					if (RootConfig
						.getBoolean(ConfigNode.DEBUG_ITEM)) {
					    plugin.getLogger().info(
						    "Not enough karma to take item: "
							    + player.getName());
					}
					player.sendMessage(ChatColor.RED
						+ KarmicShare.TAG
						+ " Not enough karma to take item");
					return -1;
				    } else {
					player.sendMessage(ChatColor.YELLOW
						+ KarmicShare.TAG
						+ " Near/Hit karma limit!");
				    }
				}
			    } catch (NullPointerException n) {
				// Found item, but there is no
				// config for specific data
				// value
				// thus adjust using regular
				// means
				staticKarma = true;
				// Reset so later we use default
				// karma change
				hasKarma = false;
			    }
			} else {
			    staticKarma = true;
			}
		    }
		    if (staticKarma) {
			// Item does not have a multiplier,
			// so use default
			karmaAdj = karma
				+ (RootConfig
					.getInt(ConfigNode.KARMA_CHANGE_TAKE)
					* amount * -1);
			if (karmaAdj < RootConfig
				.getInt(ConfigNode.KARMA_LOWER_LIMIT)) {
			    // They went beyond the lower
			    // limit
			    // adjust amount given based on
			    // karma now
			    int tempKarma = Math.abs(karmaAdj)
				    - Math.abs(RootConfig
					    .getInt(ConfigNode.KARMA_LOWER_LIMIT));
			    int div = tempKarma
				    / RootConfig
					    .getInt(ConfigNode.KARMA_CHANGE_TAKE);
			    int rem = tempKarma
				    % RootConfig
					    .getInt(ConfigNode.KARMA_CHANGE_TAKE);
			    if (rem != 0) {
				div++;
			    }
			    amount -= div;
			    amount = amount
				    / RootConfig
					    .getInt(ConfigNode.KARMA_CHANGE_TAKE);
			    if (amount <= 0) {
				// Cannot give any items as
				// they'd go beyond
				// karma limit
				if (RootConfig
					.getBoolean(ConfigNode.DEBUG_ITEM)) {
				    plugin.getLogger().info(
					    "Not enough karma to take item: "
						    + player.getName());
				}
				player.sendMessage(ChatColor.RED
					+ KarmicShare.TAG
					+ " Not enough karma to take item");
				return -1;
			    } else {
				player.sendMessage(ChatColor.YELLOW
					+ KarmicShare.TAG
					+ " Near/Hit karma limit!");
			    }
			}
		    }
		}
	    }
	    if (temp.isTool()) {
		// Handle tools
		try {
		    String toolQuery = "";
		    if (!item.getEnchantments().isEmpty()) {
			// Enchanted
			final String enchantString = parseItemEnchantments(item);
			toolQuery = "SELECT * FROM " + Table.ITEMS.getName()
				+ " WHERE itemid='" + item.getTypeId()
				+ "' AND enchantments='" + enchantString
				+ "' AND groups='" + groupId + "';";
			Query toolRS = plugin.getDatabaseHandler().select(
				toolQuery);
			if (RootConfig.getBoolean(ConfigNode.DEBUG_ITEM)) {
			    plugin.getLogger().info("Encahnted tool taken");
			}
			if (toolRS.getResult().next()) {
			    if ((toolRS.getResult().getInt("amount") - amount) <= 0) {
				// DROP
				toolQuery = "DELETE FROM "
					+ Table.ITEMS.getName() + " WHERE id='"
					+ toolRS.getResult().getInt("id")
					+ "';";
				if (RootConfig
					.getBoolean(ConfigNode.DEBUG_ITEM)) {
				    plugin.getLogger().info(
					    "Encahnted tool drop");
				}
			    } else {
				// UPDATE
				toolQuery = "UPDATE "
					+ Table.ITEMS.getName()
					+ " SET amount='"
					+ (toolRS.getResult().getInt("amount") - amount)
					+ "' WHERE id='"
					+ toolRS.getResult().getInt("id")
					+ "';";
				if (RootConfig
					.getBoolean(ConfigNode.DEBUG_ITEM)) {
				    plugin.getLogger().info(
					    "Encahnted tool update");
				}
			    }
			}
			toolRS.closeQuery();
		    } else {
			if (RootConfig.getBoolean(ConfigNode.DEBUG_ITEM)) {
			    plugin.getLogger().info("Non-enchanted tool");
			}
			// Non-enchanted tool
			toolQuery = "SELECT * FROM " + Table.ITEMS.getName()
				+ " WHERE itemid='" + item.getTypeId()
				+ "' AND groups='" + groupId + "';";
			Query toolRS = plugin.getDatabaseHandler().select(
				toolQuery);
			if (toolRS.getResult().next()) {
			    if ((toolRS.getResult().getInt("amount") - amount) <= 0) {
				// DROP
				toolQuery = "DELETE FROM "
					+ Table.ITEMS.getName()
					+ " WHERE itemid='" + item.getTypeId()
					+ "' AND amount='" + amount
					+ "' AND groups='" + groupId + "';";
			    } else {
				// UPDATE
				toolQuery = "UPDATE "
					+ Table.ITEMS.getName()
					+ " SET amount='"
					+ (toolRS.getResult().getInt("amount") - amount)
					+ "' WHERE itemid='" + item.getTypeId()
					+ "' AND groups='" + groupId + "';";
			    }
			}
			toolRS.closeQuery();
		    }
		    if (!toolQuery.contains("SELECT")) {
			plugin.getDatabaseHandler().standardQuery(toolQuery);
		    }
		} catch (SQLException e) {
		    if (RootConfig.getBoolean(ConfigNode.DEBUG_ITEM)) {
			plugin.getLogger().warning(
				"SQLException for takeItem(" + player.getName()
					+ "," + item.toString() + "," + group
					+ ")");
		    }
		    player.sendMessage(ChatColor.RED + KarmicShare.TAG
			    + "Could not retrieve item in pool!");
		    e.printStackTrace();
		    return -1;
		}
	    } else if (temp.isPotion()) {
		if (RootConfig.getBoolean(ConfigNode.DEBUG_ITEM)) {
		    plugin.getLogger().info("Take potion");
		}
		query = "SELECT * FROM " + Table.ITEMS.getName()
			+ " WHERE itemid='" + item.getTypeId()
			+ "' AND durability='" + item.getDurability()
			+ "' AND groups='" + groupId + "';";
		Query rs = plugin.getDatabaseHandler().select(query);
		try {
		    if (rs.getResult().next()) {
			if ((rs.getResult().getInt("amount") - amount) <= 0) {
			    // Drop record as there are none left
			    query = "DELETE FROM " + Table.ITEMS.getName()
				    + " WHERE itemid='" + item.getTypeId()
				    + "' AND durability='"
				    + item.getDurability() + "' AND groups='"
				    + groupId + "';";
			} else {
			    query = "UPDATE "
				    + Table.ITEMS.getName()
				    + " SET amount='"
				    + (rs.getResult().getInt("amount") - amount)
				    + "' WHERE itemid='" + item.getTypeId()
				    + "' AND durability='"
				    + item.getDurability() + "' AND groups='"
				    + groupId + "';";
			}
		    }
		    rs.closeQuery();
		    plugin.getDatabaseHandler().standardQuery(query);
		} catch (SQLException e) {
		    if (RootConfig.getBoolean(ConfigNode.DEBUG_ITEM)) {
			plugin.getLogger().warning(
				"SQLException for takeItem(" + player.getName()
					+ "," + item.toString() + "," + group
					+ ")");
		    }
		    player.sendMessage(ChatColor.RED + KarmicShare.TAG
			    + "Could not retrieve item in pool!");
		    e.printStackTrace();
		    return -1;
		}
	    } else {
		if (RootConfig.getBoolean(ConfigNode.DEBUG_ITEM)) {
		    plugin.getLogger().info("normal item");
		}
		query = "SELECT * FROM " + Table.ITEMS.getName()
			+ " WHERE itemid='" + item.getTypeId() + "' AND data='"
			+ item.getData().getData() + "' AND groups='" + groupId
			+ "';";
		Query rs = plugin.getDatabaseHandler().select(query);
		try {
		    if (rs.getResult().next()) {
			if ((rs.getResult().getInt("amount") - amount) <= 0) {
			    // Drop record as there are none left
			    query = "DELETE FROM " + Table.ITEMS.getName()
				    + " WHERE itemid='" + item.getTypeId()
				    + "' AND data='" + item.getData().getData()
				    + "' AND groups='" + groupId + "';";
			} else {
			    query = "UPDATE "
				    + Table.ITEMS.getName()
				    + " SET amount='"
				    + (rs.getResult().getInt("amount") - amount)
				    + "' WHERE itemid='" + item.getTypeId()
				    + "' AND data='" + item.getData().getData()
				    + "' AND groups='" + groupId + "';";
			}
		    }
		    rs.closeQuery();
		    plugin.getDatabaseHandler().standardQuery(query);
		} catch (SQLException e) {
		    if (RootConfig.getBoolean(ConfigNode.DEBUG_ITEM)) {
			plugin.getLogger().warning(
				"SQLException for takeItem(" + player.getName()
					+ "," + item.toString() + "," + group
					+ ")");
		    }
		    player.sendMessage(ChatColor.RED + KarmicShare.TAG
			    + "Could not retrieve item in pool!");
		    e.printStackTrace();
		    return -1;
		}
	    }
	    // Smoke effect
	    smokePlayer(player);
	    // Update karma
	    if (!RootConfig.getBoolean(ConfigNode.KARMA_DISABLED)) {
		if (!PermissionHandler.has(player, PermissionNode.IGNORE_KARMA)) {
		    if (!(RootConfig
			    .getBoolean(ConfigNode.KARMA_IGNORE_SELF_GROUP) && group
			    .equalsIgnoreCase("self_" + player.getName()))) {
			if (hasKarma) {
			    Karma.updatePlayerKarma(player.getName(), amount
				    * KarmaConfig.VALUES.get(temp).getTakeValue() * -1);
			} else {
			    Karma.updatePlayerKarma(
				    player.getName(),
				    amount
					    * RootConfig
						    .getInt(ConfigNode.KARMA_CHANGE_TAKE)
					    * -1);
			}
		    }
		}
	    }
	} catch (SQLException e) {
	    if (RootConfig.getBoolean(ConfigNode.DEBUG_ITEM)) {
		plugin.getLogger().warning(
			"SQLException for takeItem(" + player.getName() + ","
				+ item.toString() + "," + group + ")");
	    }
	    player.sendMessage(ChatColor.RED + KarmicShare.TAG
		    + "Could not retrieve item in pool!");
	    e.printStackTrace();
	    return -1;
	}
	return amount;
    }

    public static boolean giveItem(Player player, ItemStack item, String group) {
	if (!PermissionHandler.has(player, PermissionNode.GIVE)) {
	    if (RootConfig.getBoolean(ConfigNode.DEBUG_ITEM)) {
		plugin.getLogger().info(
			player.getName() + " lacks give permission");
	    }
	    player.sendMessage(ChatColor.RED + KarmicShare.TAG
		    + " Lack permission: KarmicShare.give");
	    return false;
	}
	int groupId = Karma.getGroupId(group);
	if (groupId == -1) {
	    if (RootConfig.getBoolean(ConfigNode.DEBUG_ITEM)) {
		plugin.getLogger().warning("Unknown group " + group);
	    }
	    player.sendMessage(ChatColor.RED + KarmicShare.TAG
		    + " Unknown group '" + ChatColor.GOLD + group
		    + ChatColor.RED + "'");
	    return false;
	}
	final Item i = new Item(item);
	// Check if its a tool
	String query = "";
	if (i.isTool()) {
	    // Check if enchanted
	    Map<Enchantment, Integer> enchantments = item.getEnchantments();
	    if (!enchantments.isEmpty()) {
		if (RootConfig.getBoolean(ConfigNode.DEBUG_ITEM)) {
		    plugin.getLogger().info("enchanted tool");
		}
		// Tool has enchantments
		final String enchantString = parseItemEnchantments(item);
		// Add new instance of item to database
		query = "INSERT INTO "
			+ Table.ITEMS.getName()
			+ " (itemid,amount,data,durability,enchantments,groups) VALUES ('"
			+ item.getTypeId() + "','" + item.getAmount() + "','"
			+ item.getData().getData() + "','"
			+ item.getDurability() + "','" + enchantString + "','"
			+ groupId + "');";
		plugin.getDatabaseHandler().standardQuery(query);
	    } else {
		if (RootConfig.getBoolean(ConfigNode.DEBUG_ITEM)) {
		    plugin.getLogger().info("non-enchanted tool");
		}
		// Normal tool
		// Create SQL query to see if item is already in
		// database
		query = "SELECT * FROM " + Table.ITEMS.getName()
			+ " WHERE itemid='" + item.getTypeId()
			+ "' AND durability='" + item.getDurability()
			+ "' AND groups='" + groupId
			+ "' AND enchantments IS NULL;";
		Query rs = plugin.getDatabaseHandler().select(query);

		// Send Item to database
		try {
		    if (rs.getResult().next()) {
			do {
			    // For tools, look up for similar
			    // durability. Add amount that way
			    // if it exists
			    int total = item.getAmount()
				    + rs.getResult().getInt("amount");
			    query = "UPDATE " + Table.ITEMS.getName()
				    + " SET amount='" + total
				    + "' WHERE itemid='" + item.getTypeId()
				    + "' AND groups='" + groupId + "';";
			} while (rs.getResult().next());
		    } else {
			// Item not in database, therefore add it
			query = "INSERT INTO "
				+ Table.ITEMS.getName()
				+ " (itemid,amount,data,durability,groups) VALUES ('"
				+ item.getTypeId() + "','" + item.getAmount()
				+ "','" + item.getData().getData() + "','"
				+ item.getDurability() + "','" + groupId
				+ "');";
		    }
		    rs.closeQuery();
		    plugin.getDatabaseHandler().standardQuery(query);
		} catch (SQLException e) {
		    if (RootConfig.getBoolean(ConfigNode.DEBUG_ITEM)) {
			plugin.getLogger().warning(
				"SQLException for giveItem(" + player.getName()
					+ "," + item.toString() + "," + group
					+ ")");
		    }
		    player.sendMessage(ChatColor.RED + KarmicShare.TAG
			    + "Could not query item pool!");
		    e.printStackTrace();
		    return false;
		}
	    }
	} else if (i.isPotion()) {
	    if (RootConfig.getBoolean(ConfigNode.DEBUG_ITEM)) {
		plugin.getLogger().info("Potion item");
	    }
	    // Handle potion case
	    // Potion item
	    // Create SQL query to see if item is already in
	    // database
	    query = "SELECT * FROM " + Table.ITEMS.getName()
		    + " WHERE itemid='" + item.getTypeId()
		    + "' AND durability='" + item.getDurability()
		    + "' AND groups='" + groupId + "';";
	    Query rs = plugin.getDatabaseHandler().select(query);

	    // Send Item to database
	    try {
		if (rs.getResult().next()) {
		    // For potions, look up for similar
		    // durability. Add amount that way
		    // if it exists
		    int total = item.getAmount()
			    + rs.getResult().getInt("amount");
		    query = "UPDATE " + Table.ITEMS.getName() + " SET amount='"
			    + total + "' WHERE itemid='" + item.getTypeId()
			    + "' AND durability='" + item.getDurability()
			    + "' AND groups='" + groupId + "';";
		} else {
		    // Item not in database, therefore add it
		    query = "INSERT INTO "
			    + Table.ITEMS.getName()
			    + " (itemid,amount,data,durability,groups) VALUES ('"
			    + item.getTypeId() + "','" + item.getAmount()
			    + "','0','" + item.getDurability() + "','"
			    + groupId + "');";
		}
		rs.closeQuery();
		plugin.getDatabaseHandler().standardQuery(query);
	    } catch (SQLException e) {
		if (RootConfig.getBoolean(ConfigNode.DEBUG_ITEM)) {
		    plugin.getLogger()
			    .warning(
				    "SQLException for giveItem("
					    + player.getName() + ","
					    + item.toString() + "," + group
					    + ")");
		}
		player.sendMessage(ChatColor.RED + KarmicShare.TAG
			+ "Could not query item pool!");
		e.printStackTrace();
		return false;
	    }
	} else {
	    if (RootConfig.getBoolean(ConfigNode.DEBUG_ITEM)) {
		plugin.getLogger().info("Normal item");
	    }
	    // Normal item
	    // Create SQL query to see if item is already in
	    // database
	    query = "SELECT * FROM " + Table.ITEMS.getName()
		    + " WHERE itemid='" + item.getTypeId() + "' AND data='"
		    + item.getData().getData() + "' AND groups='" + groupId
		    + "';";
	    Query rs = plugin.getDatabaseHandler().select(query);

	    // Send Item to database
	    try {
		if (rs.getResult().next()) {
		    do {
			// For tools, look up for similar
			// durability. Add amount that way
			// if it exists
			int total = item.getAmount()
				+ rs.getResult().getInt("amount");
			query = "UPDATE " + Table.ITEMS.getName()
				+ " SET amount='" + total + "' WHERE itemid='"
				+ item.getTypeId() + "' AND data='"
				+ item.getData().getData() + "' AND groups='"
				+ groupId + "';";
		    } while (rs.getResult().next());
		} else {
		    // Item not in database, therefore add it
		    query = "INSERT INTO "
			    + Table.ITEMS.getName()
			    + " (itemid,amount,data,durability,groups) VALUES ('"
			    + item.getTypeId() + "','" + item.getAmount()
			    + "','" + item.getData().getData() + "','"
			    + item.getDurability() + "','" + groupId + "');";
		}
		rs.closeQuery();
		plugin.getDatabaseHandler().standardQuery(query);
	    } catch (SQLException e) {
		if (RootConfig.getBoolean(ConfigNode.DEBUG_ITEM)) {
		    plugin.getLogger()
			    .warning(
				    "SQLException for giveItem("
					    + player.getName() + ","
					    + item.toString() + "," + group
					    + ")");
		}
		player.sendMessage(ChatColor.RED + KarmicShare.TAG
			+ "Could not query item pool!");
		e.printStackTrace();
		return false;
	    }
	}
	try {
	    // Update karma
	    if (!RootConfig.getBoolean(ConfigNode.KARMA_DISABLED)) {
		if (!PermissionHandler.has(player, PermissionNode.IGNORE_KARMA)) {
		    if (!(RootConfig
			    .getBoolean(ConfigNode.KARMA_IGNORE_SELF_GROUP) && group
			    .equalsIgnoreCase("self_" + player.getName()))) {
			if (RootConfig.getBoolean(ConfigNode.KARMA_STATIC)) {
			    Karma.updatePlayerKarma(
				    player.getName(),
				    item.getAmount()
					    * RootConfig
						    .getInt(ConfigNode.KARMA_CHANGE_GIVE));
			} else {
			    // Check if given item has a multiplier
			    Item[] karmaList = KarmaConfig.VALUES.keySet()
				    .toArray(new Item[0]);
			    boolean hasKarma = false;
			    for (Item k : karmaList) {
				if (k.areSame(i)) {
				    // Item karma needs to be adjusted
				    hasKarma = true;
				}
			    }
			    if (hasKarma) {
				try {
				    Karma.updatePlayerKarma(
					    player.getName(),
					    item.getAmount()
						    * KarmaConfig.VALUES.get(i).getGiveValue());
				} catch (NullPointerException n) {
				    // Found item, but there is no
				    // config for specific data value
				    // thus adjust using regular means
				    Karma.updatePlayerKarma(
					    player.getName(),
					    item.getAmount()
						    * RootConfig
							    .getInt(ConfigNode.KARMA_CHANGE_GIVE));
				}
			    } else {
				Karma.updatePlayerKarma(
					player.getName(),
					item.getAmount()
						* RootConfig
							.getInt(ConfigNode.KARMA_CHANGE_GIVE));
			    }
			}
		    }
		}
	    }
	} catch (SQLException e) {
	    if (RootConfig.getBoolean(ConfigNode.DEBUG_ITEM)) {
		plugin.getLogger().warning(
			"SQLException for giveItem(" + player.getName() + ","
				+ item.toString() + "," + group + ")");
	    }
	    player.sendMessage(ChatColor.RED + KarmicShare.TAG
		    + "Could not adjust karma!");
	    e.printStackTrace();
	    return false;
	}
	// Smoke effect
	smokePlayer(player);
	return true;
    }

    private static String parseItemEnchantments(ItemStack item) {
	final StringBuilder sb = new StringBuilder();
	final Map<ComparableEnchantment, Integer> map = new HashMap<ComparableEnchantment, Integer>();
	for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments()
		.entrySet()) {
	    map.put(new ComparableEnchantment(entry.getKey()), entry.getValue());
	}
	TreeSet<ComparableEnchantment> keys = new TreeSet<ComparableEnchantment>(
		map.keySet());
	for (ComparableEnchantment key : keys) {
	    sb.append(key.getId()
		    + "v"
		    + item.getEnchantments().get(key.getEnchantment())
			    .intValue() + "i");
	}
	// Remove trailing comma
	sb.deleteCharAt(sb.length() - 1);
	return sb.toString();
    }

    /**
     * Provides a smoke effect for the player.
     * 
     * http://forums.bukkit.org/threads/smoke-effect-yes-i-know-others-have-
     * asked.29492/
     * 
     * @param Player
     *            that should get the effect
     * @author Adamki11s
     */
    private static void smokePlayer(Player player) {
	// Check if enabled in config
	if (!RootConfig.getBoolean(ConfigNode.EFFECTS)) {
	    return;
	}
	// Effect
	final Location loc = player.getLocation();
	final World w = loc.getWorld();
	for (double x = (loc.getX() - 3); x <= (loc.getX() + 3); x++) {
	    for (double y = (loc.getY() - 3); y <= (loc.getY() + 3); y++) {
		for (double z = (loc.getZ() - 3); z <= (loc.getZ() + 3); z++) {
		    w.playEffect(new Location(w, x, y, z), Effect.SMOKE, 1);
		}
	    }
	}
    }
}
