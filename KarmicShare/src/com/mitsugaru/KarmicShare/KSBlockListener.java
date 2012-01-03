package com.mitsugaru.KarmicShare;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

import com.splatbang.betterchest.BetterChest;

public class KSBlockListener extends BlockListener {
	private KarmicShare plugin;

	public KSBlockListener(KarmicShare karmicShare) {
		plugin = karmicShare;
	}

	@Override
	public void onSignChange(final SignChangeEvent event)
	{
		if(!event.isCancelled())
		{
			boolean has = false;
			for(String line : event.getLines())
			{
				if(ChatColor.stripColor(line).equalsIgnoreCase("[KarmicShare]"))
				{
					has = true;
				}
			}
			if(has)
			{
				//Thanks to Wolvereness for the following code
				if(event.getBlock().getRelative(BlockFace.DOWN).getType() == Material.CHEST)
				{
					//Reformat sign
					event.setLine(0,  "");
					event.setLine(1, ChatColor.AQUA + "[KarmicShare]");
					event.setLine(2, "Page:");
					event.setLine(3, "1");
				}
				else
				{
					//Reformat sign
					event.setLine(0,  "");
					event.setLine(1, ChatColor.DARK_RED + "[KarmicShare]");
					event.setLine(2, "Page:");
					event.setLine(3, "1");
					event.getPlayer().sendMessage(ChatColor.YELLOW + KarmicShare.prefix + " No chest found!");
				}
			}
		}
	}

	@Override
	public void onBlockPlace(final BlockPlaceEvent event)
	{
		final Material material = event.getBlock().getType();
		if(material.equals(Material.CHEST))
		{
			final Block block = event.getBlock();
			final BetterChest chest = new BetterChest((Chest) block.getState());
			if(block.getRelative(BlockFace.UP).getType() == Material.WALL_SIGN)
			{
				Sign sign = (Sign) block.getRelative(BlockFace.UP).getState();
				for(String s : sign.getLines())
				{
					if(ChatColor.stripColor(s).equalsIgnoreCase("[KarmicShare]"))
					{
						//Reformat sign
						sign.setLine(0,  "");
						sign.setLine(1, ChatColor.AQUA + "[KarmicShare]");
						sign.setLine(2, "Page:");
						sign.setLine(3, "1");
						sign.update();
					}
				}
			}
			else if(chest.isDoubleChest())
			{
				if(chest.attachedBlock().getRelative(BlockFace.UP).getType().equals(Material.WALL_SIGN))
				{
					final Sign sign = (Sign) chest.attachedBlock().getRelative(BlockFace.UP).getState();
					if(ChatColor.stripColor(sign.getLine(1)).equalsIgnoreCase("[KarmicShare]"))
					{
						//Reformat sign
						sign.setLine(0,  "");
						sign.setLine(1, ChatColor.AQUA + "[KarmicShare]");
						sign.setLine(2, "Page:");
						sign.setLine(3, "1");
						sign.update();
					}
				}
			}
		}

	}

	@Override
	public void onBlockBreak(final BlockBreakEvent event)
	{
		if(!event.isCancelled())
		{
			final Material material = event.getBlock().getType();
			if(material.equals(Material.CHEST))
			{
				final Block block = event.getBlock();
				final BetterChest chest = new BetterChest((Chest) block.getState());
				if(block.getRelative(BlockFace.UP).getType().equals(Material.WALL_SIGN))
				{
					Sign sign = (Sign) block.getRelative(BlockFace.UP).getState();
					if(ChatColor.stripColor(sign.getLine(1)).equalsIgnoreCase("[KarmicShare]"))
					{
						//Empty chest as it has spawned items
						chest.getInventory().clear();
						chest.update();
						//Update sign
						sign.setLine(1, ChatColor.DARK_RED + "[KarmicShare]");
						sign.update();
					}
				}
				else if(chest.isDoubleChest())
				{
					if(chest.attachedBlock().getRelative(BlockFace.UP).getType().equals(Material.WALL_SIGN))
					{
						final Sign sign = (Sign) chest.attachedBlock().getRelative(BlockFace.UP).getState();
						if(ChatColor.stripColor(sign.getLine(1)).equalsIgnoreCase("[KarmicShare]"))
						{
							//Empty chest as it has spawned items
							chest.getInventory().clear();
							chest.update();
						}
					}
				}
			}
			else if(material.equals(Material.WALL_SIGN))
			{
				final Sign sign = (Sign) event.getBlock().getState();
				if(event.getBlock().getRelative(BlockFace.DOWN).getType() == Material.CHEST)
				{
					BetterChest chest = new BetterChest((Chest) sign.getBlock().getRelative(BlockFace.DOWN).getState());
					chest.getInventory().clear();
					chest.update();
				}
			}
		}
	}

	/**
	 * Thanks to Ribesg for the following method
	 */
	@Override
	public void onBlockPhysics(BlockPhysicsEvent event)
	{
		Material material = event.getBlock().getType();
		if(material.equals(Material.SIGN_POST) || material.equals(Material.WALL_SIGN))
		{
			Sign sign = (Sign) event.getBlock().getState();
			if(ChatColor.stripColor(sign.getLine(1)).equalsIgnoreCase("[KarmicShare]"))
			{
				event.setCancelled(true);
			}
		}
	}
}
