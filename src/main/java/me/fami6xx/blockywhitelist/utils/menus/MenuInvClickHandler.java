package me.fami6xx.blockywhitelist.utils.menus;

import me.fami6xx.blockywhitelist.BlockyWhitelist;
import me.fami6xx.blockywhitelist.utils.menus.types.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;

public class MenuInvClickHandler implements Listener {
    @EventHandler
    public void onMenuClick(InventoryClickEvent e){
        InventoryHolder holder = e.getInventory().getHolder();
        if (holder instanceof Menu) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null) {
                return;
            }

            if(e.getClickedInventory() == null || e.getView().getTopInventory() != e.getClickedInventory()){
                return;
            }

            Menu menu = (Menu) holder;
            menu.handleMenu(e);
        }
    }

    @EventHandler
    public void onMenuInteract(InventoryInteractEvent e){
        InventoryHolder holder = e.getInventory().getHolder();
        if (holder instanceof Menu) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onMenuClose(InventoryCloseEvent e){
        InventoryHolder holder = e.getInventory().getHolder();
        if (holder instanceof Menu) {
            BlockyWhitelist.getInstance().getMenuManager().getPlayerMenu((Player) e.getPlayer()).setCurrentMenu(null);
        }
    }

    @EventHandler
    public void onMenuOpen(InventoryOpenEvent e) {
        InventoryHolder holder = e.getInventory().getHolder();
        if (holder instanceof Menu) {
            BlockyWhitelist.getInstance().getMenuManager().getPlayerMenu((Player) e.getPlayer()).setCurrentMenu((Menu) holder);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncChat(AsyncPlayerChatEvent e) {
        PlayerMenu playerMenu = BlockyWhitelist.getInstance().getMenuManager().getPlayerMenu(e.getPlayer());
        if (playerMenu.getPendingAction() != null) {
            e.setCancelled(true);
            String input = e.getMessage();
            new BukkitRunnable() {
                @Override
                public void run() {
                    playerMenu.getPendingAction().accept(input);
                    playerMenu.clearPendingAction();
                }
            }.runTaskLater(BlockyWhitelist.getInstance(), 1L);
        }
    }
}
