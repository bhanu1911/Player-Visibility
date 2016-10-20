package nl.tomudding.plugins.visibility.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import nl.tomudding.plugins.visibility.Visibility;
import nl.tomudding.plugins.visibility.listeners.PlayerListener;
import nl.tomudding.plugins.visibility.managers.ChatManager;
import nl.tomudding.plugins.visibility.managers.PlayerManager;

public class Commands implements CommandExecutor {

	private Visibility plugin;
	
	public Commands(Visibility plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("visibility")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (args.length == 0) {
					ChatManager.getInstance().sendCommandMessage(player, ChatColor.BLUE + "============ " + ChatColor.GRAY + "Visibility Help" + ChatColor.BLUE + " ============");
					ChatManager.getInstance().sendCommandMessage(player, ChatColor.BLUE + "/"+cmd.getName().toLowerCase().toString()+"" + ChatColor.GRAY + " - This help menu");
					ChatManager.getInstance().sendCommandMessage(player, ChatColor.BLUE + "/"+cmd.getName().toLowerCase().toString()+" info" + ChatColor.GRAY + " - Information about the plugin");
					return true;
				} else if (args.length == 1) {
					if (args[0].equalsIgnoreCase("info")) {
						ChatManager.getInstance().sendCommandMessage(player, ChatColor.BLUE + "============ " + ChatColor.GRAY + "Visibility Information" + ChatColor.BLUE + " ============");
						ChatManager.getInstance().sendCommandMessage(player, ChatColor.GRAY + "Plugin by - " + ChatColor.BLUE + "tomudding");
						ChatManager.getInstance().sendCommandMessage(player, ChatColor.GRAY + "Plugin version - " + ChatColor.BLUE + plugin.getDescription().getVersion());
						return true;
					}
				}
			} else {
				if (args.length == 0) {
					ChatManager.getInstance().log(ChatColor.BLUE + "============ " + ChatColor.GRAY + "Visibility Help" + ChatColor.BLUE + " ============");
					ChatManager.getInstance().log(ChatColor.BLUE + "/"+cmd.getName().toLowerCase().toString()+"" + ChatColor.GRAY + " - This help menu");
					ChatManager.getInstance().log(ChatColor.BLUE + "/"+cmd.getName().toLowerCase().toString()+" info" + ChatColor.GRAY + " - Information about the plugin");
					return true;
				} else if (args.length == 1) {
					if (args[0].equalsIgnoreCase("info")) {
						ChatManager.getInstance().log(ChatColor.BLUE + "============ " + ChatColor.GRAY + "Player Visibility Information" + ChatColor.BLUE + " ============");
						ChatManager.getInstance().log(ChatColor.GRAY + "Plugin by - " + ChatColor.BLUE + "tomudding");
						ChatManager.getInstance().log(ChatColor.GRAY + "Plugin version - " + ChatColor.BLUE + plugin.getDescription().getVersion());
						return true;
					}
				}
			}
		} else if (cmd.getName().equalsIgnoreCase("show")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				
				if (player.hasPermission("visibility.show")) {
					if (Visibility.enabledWorlds.contains(player.getLocation().getWorld().getName().toString())) {
						if (PlayerManager.getInstance().getToggleState(player.getUniqueId()) == false) {
							if (!Visibility.inCooldown.contains(player.getUniqueId())) {
								for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
									player.showPlayer(onlinePlayers);
								}
								
								player.getInventory().setItem(Visibility.itemSlot, PlayerListener.createItemStack(true));
								PlayerManager.getInstance().setToggle(player.getUniqueId(), true);
								
								if (!player.hasPermission("visibility.cooldown")) {
									Visibility.inCooldown.add(player.getUniqueId());
									
				  		  			BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
				  		  			scheduler.scheduleSyncDelayedTask(this.plugin, new Runnable() {
										@Override
										public void run() {
											Visibility.inCooldown.remove(player.getUniqueId());
										}
									}, Visibility.timeCooldown * 20);
								}
		
								ChatManager.getInstance().sendMessage(player, Visibility.messageToggleOn);
								return true;
							} else {
								ChatManager.getInstance().sendMessage(player, Visibility.messageCooldown);
								return true;
							}
						} else {
							ChatManager.getInstance().sendMessage(player, Visibility.messageAlreadyOn);
							return true;
						}
					} else {
						ChatManager.getInstance().sendMessage(player, Visibility.messageWorld);
						return true;
					}
				} else {
					ChatManager.getInstance().sendMessage(player, Visibility.messagePermission);
					return true;
				}
			} else {
				ChatManager.getInstance().log("&cThis command is player-only.");
				return true;
			}
		} else if (cmd.getName().equalsIgnoreCase("hide")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				
				if (player.hasPermission("visibility.hide")) {
					if (Visibility.enabledWorlds.contains(player.getLocation().getWorld().getName().toString())) {
						if (PlayerManager.getInstance().getToggleState(player.getUniqueId()) == true) {
							if (!Visibility.inCooldown.contains(player.getUniqueId())) {
								for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
									if (!onlinePlayers.hasPermission("visibility.ignore")) {
										player.hidePlayer(onlinePlayers);
									}
								}
								
								player.getInventory().setItem(Visibility.itemSlot, PlayerListener.createItemStack(false));
								PlayerManager.getInstance().setToggle(player.getUniqueId(), false);
								
								if (!player.hasPermission("visibility.cooldown")) {
									Visibility.inCooldown.add(player.getUniqueId());
									
				  		  			BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
				  		  			scheduler.scheduleSyncDelayedTask(this.plugin, new Runnable() {
										@Override
										public void run() {
											Visibility.inCooldown.remove(player.getUniqueId());
										}
									}, Visibility.timeCooldown * 20);
								}
		
								ChatManager.getInstance().sendMessage(player, Visibility.messageToggleOff);
								return true;
							} else {
								ChatManager.getInstance().sendMessage(player, Visibility.messageCooldown);
								return true;
							}
						} else {
							ChatManager.getInstance().sendMessage(player, Visibility.messageAlreadyOff);
							return true;
						}
					} else {
						ChatManager.getInstance().sendMessage(player, Visibility.messageWorld);
						return true;
					}
				} else {
					ChatManager.getInstance().sendMessage(player, Visibility.messagePermission);
					return true;
				}
			} else {
				ChatManager.getInstance().log("&cThis command is player-only.");
				return true;
			}
		}
		return false;
	}	
}