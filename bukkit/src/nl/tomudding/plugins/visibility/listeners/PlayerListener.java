package nl.tomudding.plugins.visibility.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EquipmentSlot;

import nl.tomudding.plugins.visibility.Visibility;
import nl.tomudding.plugins.visibility.managers.ChatManager;
import nl.tomudding.plugins.visibility.managers.PlayerManager;

public class PlayerListener implements Listener {
	private Visibility plugin;
	
	public PlayerListener(Visibility plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		
		if (Visibility.enabledWorlds.contains(player.getLocation().getWorld().getName().toString())) {
			if (PlayerManager.getInstance().checkIfExists(uuid) == false) {
				ChatManager.getInstance().log("Player " + uuid + " is not in data.yml, injecting...");
				
				PlayerManager.getInstance().setToggle(uuid, true);
				for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
					player.showPlayer(onlinePlayers);
				}

				player.getInventory().setItem(Visibility.itemSlot, Visibility.createItemStack(true));
				ChatManager.getInstance().sendMessage(player, Visibility.messageToggleOn);
			} else if (PlayerManager.getInstance().checkIfExists(uuid) == true) {
				ChatManager.getInstance().log("Player " + uuid + " is in data.yml");
				
				if (PlayerManager.getInstance().getToggleState(uuid) == true) {
	  		  		for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
	  		  			if (Visibility.enabledWorlds.contains(onlinePlayers.getWorld().getName().toString())) {
	  		  				player.showPlayer(onlinePlayers);
	  		  			}
	  		  		}
	  		  		
					player.getInventory().setItem(Visibility.itemSlot, Visibility.createItemStack(true));
					ChatManager.getInstance().sendMessage(player,  Visibility.messageToggleOn);
				} else if (PlayerManager.getInstance().getToggleState(uuid) == false) {
	  		  		for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
	  		  			if (Visibility.enabledWorlds.contains(onlinePlayers.getWorld().getName().toString())) {
	  		  				if (!onlinePlayers.hasPermission("visibility.ignore")) {
	  		  					player.hidePlayer(onlinePlayers);
	  		  				}
	  		  			}
	  		  		}
	  		  		
					player.getInventory().setItem(Visibility.itemSlot, Visibility.createItemStack(false));
					ChatManager.getInstance().sendMessage(player, Visibility.messageToggleOff);
				}
			}
			
			for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
				if (Visibility.enabledWorlds.contains(onlinePlayers.getLocation().getWorld().getName().toString())) {
					if (PlayerManager.getInstance().getToggleState(onlinePlayers.getUniqueId()) == true) {
						onlinePlayers.showPlayer(player);
					} else if (PlayerManager.getInstance().getToggleState(onlinePlayers.getUniqueId()) == false) {
						if (!player.hasPermission("visibility.ignore")) {
							onlinePlayers.hidePlayer(player);
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (event.getHand().equals(EquipmentSlot.HAND)) {
			if (player.getInventory().getItemInMainHand().equals(null) || player.getInventory().getItemInMainHand().equals(Material.AIR)) return;
			if (player.getInventory().getItemInMainHand().equals(Visibility.createItemStack(true))) {
				if (event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) return;
				if (!player.hasPermission("visibility.hide")) { ChatManager.getInstance().sendMessage(player, Visibility.messagePermission); return; }
				if (!Visibility.enabledWorlds.contains(player.getLocation().getWorld().getName().toString())) { ChatManager.getInstance().sendMessage(player, Visibility.messageWorld); return; }
				event.setCancelled(true);
				if (Visibility.inCooldown.containsKey(player.getUniqueId())) {
					long timeLeft = Visibility.inCooldown.get(player.getUniqueId()).longValue() / 1000L + Visibility.timeCooldown - (System.currentTimeMillis() / 1000L);
					if (timeLeft > 0L) {
						ChatManager.getInstance().sendMessage(player, Visibility.messageCooldown.replace("%time%", Long.toString(timeLeft)));
					} else {
						Visibility.removeCooldown(player, false);
					}
					return;
				}
				
				if (!player.hasPermission("visibility.bypass.cooldown")) { Visibility.setCooldown(player, false); }
				player.getInventory().setItem(Visibility.itemSlot, Visibility.createItemStack(false));
				
				for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
					if (!onlinePlayers.hasPermission("visibility.ignore")) {
						player.hidePlayer(onlinePlayers);
					}
				}
				
				PlayerManager.getInstance().setToggle(player.getUniqueId(), false);
				ChatManager.getInstance().sendMessage(player, Visibility.messageToggleOff);
			} else if (player.getInventory().getItemInMainHand().equals(Visibility.createItemStack(false))) {
				if (event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) return;
				if (!player.hasPermission("visibility.show")) { ChatManager.getInstance().sendMessage(player, Visibility.messagePermission); return; }
				if (!Visibility.enabledWorlds.contains(player.getLocation().getWorld().getName().toString())) { ChatManager.getInstance().sendMessage(player, Visibility.messageWorld); return; }
				event.setCancelled(true);
				if (Visibility.inCooldown.containsKey(player.getUniqueId())) {
					long timeLeft = Visibility.inCooldown.get(player.getUniqueId()).longValue() / 1000L + Visibility.timeCooldown - (System.currentTimeMillis() / 1000L);
					if (timeLeft > 0L) {
						ChatManager.getInstance().sendMessage(player, Visibility.messageCooldown.replace("%time%", Long.toString(timeLeft)));
					} else {
						Visibility.removeCooldown(player, true);
					}
					return;
				}
				
				if (!player.hasPermission("visibility.bypass.cooldown")) { Visibility.setCooldown(player, true); }
				player.getInventory().setItem(Visibility.itemSlot, Visibility.createItemStack(true));
				
				for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
					player.showPlayer(onlinePlayers);
				}
					
				PlayerManager.getInstance().setToggle(player.getUniqueId(), true);
				ChatManager.getInstance().sendMessage(player, Visibility.messageToggleOn);
			}
		}
	}

	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();
		
		if (Visibility.enabledWorlds.contains(player.getLocation().getWorld().getName().toString())) {
			if (PlayerManager.getInstance().getToggleState(player.getUniqueId()) == true) {
				player.getPlayer().getInventory().setItem(Visibility.itemSlot, Visibility.createItemStack(true));
			} else if (PlayerManager.getInstance().getToggleState(player.getUniqueId()) == false) {
				player.getPlayer().getInventory().setItem(Visibility.itemSlot, Visibility.createItemStack(false));
			}
			
			for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
				if (onlinePlayers.getWorld().equals(player.getLocation().getWorld())) {
					if (PlayerManager.getInstance().getToggleState(player.getUniqueId()) == true) {
	  		  			player.showPlayer(onlinePlayers);
					} else if (PlayerManager.getInstance().getToggleState(player.getUniqueId()) == false) {
	  		  			if (!onlinePlayers.hasPermission("visibility.ignore")) {
	  		  				player.hidePlayer(onlinePlayers);
	  		  			}
					}
					
  		  			if (PlayerManager.getInstance().getToggleState(onlinePlayers.getUniqueId()) == true) {
  		  				onlinePlayers.showPlayer(player);
  		  			} else if (PlayerManager.getInstance().getToggleState(onlinePlayers.getUniqueId()) == false) {
  		  				if (!player.hasPermission("visibility.ignore")) {
  		  					onlinePlayers.hidePlayer(player);
  		  				}
  		  			}
				}
			}
			
			if (PlayerManager.getInstance().getToggleState(player.getUniqueId()) == true) {
				ChatManager.getInstance().sendMessage(player, Visibility.messageToggleOn);
			} else if (PlayerManager.getInstance().getToggleState(player.getUniqueId()) == false) {
				ChatManager.getInstance().sendMessage(player, Visibility.messageToggleOff);
			}
		} else {
			for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
				player.showPlayer(onlinePlayers);
				onlinePlayers.showPlayer(player); // Not sure about this one
			}
			
			if (player.getInventory().contains(Visibility.createItemStack(true))) {
				player.getInventory().remove(Visibility.createItemStack(false));
			} else if (player.getInventory().contains(Visibility.createItemStack(false))) {
				player.getInventory().remove(Visibility.createItemStack(false));
			}
		}
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		if (Visibility.enabledWorlds.contains(event.getRespawnLocation().getWorld().getName().toString())) {
			player.getInventory().setItem(Visibility.itemSlot, Visibility.createItemStack(PlayerManager.getInstance().getToggleState(player.getUniqueId())));
		} else {
			for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
				player.showPlayer(onlinePlayers);
				onlinePlayers.showPlayer(player); // Not sure about this one
			}
		}
	}
	
	@EventHandler
	public void onHandItemSwap(PlayerSwapHandItemsEvent event) {
		Player player = event.getPlayer();
		if (Visibility.enabledWorlds.contains(player.getLocation().getWorld().getName().toString())) {
			if (event.getOffHandItem().equals(Visibility.createItemStack(true)) || event.getOffHandItem().equals(Visibility.createItemStack(false))) {
				event.setCancelled(true);
				ChatManager.getInstance().sendMessage(player, Visibility.messageNoSwitch);
			}
		}
	}
	
	@EventHandler
	public void onTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		if (Visibility.enabledWorlds.contains(player.getLocation().getWorld().getName().toString())) {
			if (player.getInventory().getItemInMainHand().equals(Visibility.createItemStack(true)) || player.getInventory().getItemInMainHand().equals(Visibility.createItemStack(false))) {
				if (event.getCause().equals(TeleportCause.ENDER_PEARL) || event.getCause().equals(TeleportCause.CHORUS_FRUIT)) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler(priority=EventPriority.NORMAL)
	public void onDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		if (Visibility.enabledWorlds.contains(player.getLocation().getWorld().getName().toString()) && player.getInventory().getHeldItemSlot() == Visibility.itemSlot) {
			event.setCancelled(true);
			ChatManager.getInstance().sendMessage(player, Visibility.messageNoSwitch);
		}
	}
	
	@EventHandler
	public void onClickInventory(InventoryClickEvent event) {
		if (Visibility.enabledWorlds.contains(event.getWhoClicked().getLocation().getWorld().getName().toString()) && 
			event.getSlot() == Visibility.itemSlot) {
				event.setCancelled(true);
				ChatManager.getInstance().sendMessage(plugin.getServer().getPlayer(event.getWhoClicked().getUniqueId()), Visibility.messageNoSwitch);
		}
	}
}
