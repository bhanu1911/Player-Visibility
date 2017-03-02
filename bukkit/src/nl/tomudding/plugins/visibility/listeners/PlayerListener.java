package nl.tomudding.plugins.visibility.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
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
	
	public PlayerListener(Visibility plugin) {}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		
		if (Visibility.enabledWorlds.contains(player.getLocation().getWorld().getName().toString())) {
			if (PlayerManager.getInstance().checkIfExists(uuid) == false) {
				ChatManager.getInstance().log("Player " + uuid + " is not in data.yml, injecting...");
				
				PlayerManager.getInstance().setToggle(uuid, true);
				for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
					if (Visibility.enabledWorlds.contains(onlinePlayers.getLocation().getWorld().getName().toString())) player.showPlayer(onlinePlayers);
				}

				Visibility.setItemInHand(player, Visibility.createItemStack(true));
				if (Visibility.enableJoinMessage) ChatManager.getInstance().sendMessage(player, Visibility.messageToggleOn);
			} else if (PlayerManager.getInstance().checkIfExists(uuid) == true) {
				ChatManager.getInstance().log("Player " + uuid + " is in data.yml");
				
				if (PlayerManager.getInstance().getToggleState(uuid) == true) {
	  		  		for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
	  		  			if (Visibility.enabledWorlds.contains(onlinePlayers.getLocation().getWorld().getName().toString())) player.showPlayer(onlinePlayers);
	  		  		}
	  		  		
					Visibility.setItemInHand(player, Visibility.createItemStack(true));
					if (Visibility.enableJoinMessage) ChatManager.getInstance().sendMessage(player,  Visibility.messageToggleOn);
				} else if (PlayerManager.getInstance().getToggleState(uuid) == false) {
	  		  		for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
		  		  		if (Visibility.enabledWorlds.contains(onlinePlayers.getLocation().getWorld().getName().toString())) {
	  		  				if (!onlinePlayers.hasPermission("visibility.ignore")) {
	  		  					player.hidePlayer(onlinePlayers);
	  		  				}
		  		  		}
	  		  		}
	  		  		
	  		  		Visibility.setItemInHand(player, Visibility.createItemStack(false));
					if (Visibility.enableJoinMessage) ChatManager.getInstance().sendMessage(player, Visibility.messageToggleOff);
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
		if ((event.getHand() != null) && (event.getHand().equals(EquipmentSlot.HAND))) {
			if (Visibility.getItemInHand(player).isSimilar(Visibility.createItemStack(true))) {
				if (event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) return;
				if (!Visibility.enabledWorlds.contains(player.getLocation().getWorld().getName().toString())) { if (Visibility.enableWorldToggleMessage) { ChatManager.getInstance().sendMessage(player, Visibility.messageWorld); } return; }
				if (!player.hasPermission("visibility.hide")) { ChatManager.getInstance().sendMessage(player, Visibility.messagePermission); return; }
				if (PlayerManager.getInstance().getToggleState(player.getUniqueId()) == false) { ChatManager.getInstance().sendMessage(player, Visibility.messageAlreadyOff); return; }
				event.setCancelled(true);
				if (Visibility.inCooldown.containsKey(player.getUniqueId())) {
					long timeLeft = Visibility.inCooldown.get(player.getUniqueId()).longValue() / 1000L + Visibility.toggleCooldown - (System.currentTimeMillis() / 1000L);
					if (timeLeft > 0L) {
						ChatManager.getInstance().sendMessage(player, Visibility.messageCooldown.replace("%time%", Long.toString(timeLeft)));
						return;
					} else {
						Visibility.removeCooldown(player, false);
					}
				}
				
				if (!player.hasPermission("visibility.bypass.cooldown")) { Visibility.setCooldown(player, false); }
				Visibility.setItemInHand(player, Visibility.createItemStack(false));
				
				for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
					if (!onlinePlayers.hasPermission("visibility.ignore")) {
						player.hidePlayer(onlinePlayers);
					}
				}
				
				PlayerManager.getInstance().setToggle(player.getUniqueId(), false);
				ChatManager.getInstance().sendMessage(player, Visibility.messageToggleOff);
			} else if (Visibility.getItemInHand(player).isSimilar(Visibility.createItemStack(false))) {
				if (event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) return;
				if (!Visibility.enabledWorlds.contains(player.getLocation().getWorld().getName().toString())) { if (Visibility.enableWorldToggleMessage) { ChatManager.getInstance().sendMessage(player, Visibility.messageWorld); } return; }
				if (!player.hasPermission("visibility.show")) { ChatManager.getInstance().sendMessage(player, Visibility.messagePermission); return; }
				event.setCancelled(true);
				if (Visibility.inCooldown.containsKey(player.getUniqueId())) {
					long timeLeft = Visibility.inCooldown.get(player.getUniqueId()).longValue() / 1000L + Visibility.toggleCooldown - (System.currentTimeMillis() / 1000L);
					if (timeLeft > 0L) {
						ChatManager.getInstance().sendMessage(player, Visibility.messageCooldown.replace("%time%", Long.toString(timeLeft)));
						return;
					} else {
						Visibility.removeCooldown(player, true);
					}
				}
				
				if (!player.hasPermission("visibility.bypass.cooldown")) { Visibility.setCooldown(player, true); }
				Visibility.setItemInHand(player, Visibility.createItemStack(true));
				
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
				Visibility.setItemInHand(player, Visibility.createItemStack(true));
			} else if (PlayerManager.getInstance().getToggleState(player.getUniqueId()) == false) {
				Visibility.setItemInHand(player, Visibility.createItemStack(false));
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
				if (Visibility.enableWorldSwitchMessage) ChatManager.getInstance().sendMessage(player, Visibility.messageToggleOn);
			} else if (PlayerManager.getInstance().getToggleState(player.getUniqueId()) == false) {
				if (Visibility.enableWorldSwitchMessage) ChatManager.getInstance().sendMessage(player, Visibility.messageToggleOff);
			}
		} else {
			for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
				player.showPlayer(onlinePlayers);
				onlinePlayers.showPlayer(player); // Not sure about this one
			}
			
			if (player.getInventory().contains(Visibility.createItemStack(true))) {
				player.getInventory().remove(Visibility.createItemStack(true));
			} else if (player.getInventory().contains(Visibility.createItemStack(false))) {
				player.getInventory().remove(Visibility.createItemStack(false));
			}
		}
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		if (Visibility.enabledWorlds.contains(event.getRespawnLocation().getWorld().getName().toString())) {
			Visibility.setItemInHand(player, Visibility.createItemStack(PlayerManager.getInstance().getToggleState(player.getUniqueId())));
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
			if (event.getOffHandItem().isSimilar(Visibility.createItemStack(true)) || event.getOffHandItem().isSimilar(Visibility.createItemStack(false)) ||
				event.getMainHandItem().isSimilar(Visibility.createItemStack(true)) || event.getMainHandItem().isSimilar(Visibility.createItemStack(false))) {
				event.setCancelled(true);
				if (Visibility.enableItemSwitchMessage) {
					ChatManager.getInstance().sendMessage(player, Visibility.messageSwitch);
				}
			}
		}
	}
	
	@EventHandler
	public void onTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		if (Visibility.enabledWorlds.contains(player.getLocation().getWorld().getName().toString())) {
			if (Visibility.getItemInHand(player).isSimilar(Visibility.createItemStack(true)) || Visibility.getItemInHand(player).isSimilar(Visibility.createItemStack(false))) {
				if (event.getCause().equals(TeleportCause.ENDER_PEARL) || event.getCause().equals(TeleportCause.CHORUS_FRUIT)) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		if (Visibility.enabledWorlds.contains(player.getLocation().getWorld().getName().toString())) {
			if (event.getItemDrop().getItemStack().isSimilar(Visibility.createItemStack(true)) || event.getItemDrop().getItemStack().isSimilar(Visibility.createItemStack(false))) {
				event.setCancelled(true);
				if (Visibility.enableItemSwitchMessage) {
					ChatManager.getInstance().sendMessage(player, Visibility.messageSwitch);
				}
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onClickInventory(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		if (Visibility.enabledWorlds.contains(player.getLocation().getWorld().getName().toString())) {
			if ((event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) && (event.getCurrentItem().isSimilar(Visibility.createItemStack(true)) || event.getCurrentItem().isSimilar(Visibility.createItemStack(false)))) {
				event.setCancelled(true);
				event.setResult(Result.DENY);
				if (Visibility.enableItemSwitchMessage) {
					ChatManager.getInstance().sendMessage(player, Visibility.messageSwitch);
				}
			} else if (event.getHotbarButton() != -1) {
				if ((event.getCurrentItem() != null) && (player.getInventory().getItem(event.getHotbarButton()) != null)) {
					if (player.getInventory().getItem(event.getHotbarButton()).isSimilar(Visibility.createItemStack(true)) || player.getInventory().getItem(event.getHotbarButton()).isSimilar(Visibility.createItemStack(false))) {
						event.setCancelled(true);
						event.setResult(Result.DENY);
						if (Visibility.enableItemSwitchMessage) {
							ChatManager.getInstance().sendMessage(player, Visibility.messageSwitch);
						}
					}
				}
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onCreateInventory(InventoryCreativeEvent event) {
		Player player = (Player) event.getWhoClicked();
		if (Visibility.enabledWorlds.contains(player.getLocation().getWorld().getName().toString())) {
			if ((event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) && (event.getCurrentItem().isSimilar(Visibility.createItemStack(true)) || event.getCurrentItem().isSimilar(Visibility.createItemStack(false)))) {
				event.setCancelled(true);
				event.setResult(Result.DENY);
				if (Visibility.enableItemSwitchMessage) {
					ChatManager.getInstance().sendMessage(player, Visibility.messageSwitch);
				}
			} else if ((event.getCursor() != null && event.getCursor().getType() != Material.AIR) && (event.getCursor().isSimilar(Visibility.createItemStack(true)) || event.getCursor().isSimilar(Visibility.createItemStack(false)))) {
				event.setCancelled(true);
				event.setResult(Result.DENY);
				if (Visibility.enableItemSwitchMessage) {
					ChatManager.getInstance().sendMessage(player, Visibility.messageSwitch);
				}
			}
		}
	}
}
