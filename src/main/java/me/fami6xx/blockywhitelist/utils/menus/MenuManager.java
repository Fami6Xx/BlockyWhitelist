package me.fami6xx.blockywhitelist.utils.menus;

import me.fami6xx.blockywhitelist.BlockyWhitelist;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashMap;

/**
 * Manages player menus and handles menu-related events.
 */
public class MenuManager {
    MenuInvClickHandler clickHandler;

    private static final HashMap<Player, PlayerMenu> playerMenuMap = new HashMap<>();

    /**
     * Enables the menu manager by registering the click handler.
     *
     * @return true if the operation was successful.
     */
    public boolean enable() {
        this.clickHandler = new MenuInvClickHandler();
        BlockyWhitelist.getInstance().getServer().getPluginManager().registerEvents(this.clickHandler, BlockyWhitelist.getInstance());
        return true;
    }

    /**
     * Disables the menu manager by unregistering the click handler.
     *
     * @return true if the operation was successful.
     */
    public boolean disable() {
        InventoryClickEvent.getHandlerList().unregister(this.clickHandler);
        return true;
    }

    /**
     * Retrieves the PlayerMenu for a given player, creating one if it doesn't exist.
     *
     * @param player the player whose menu is to be retrieved.
     * @return the PlayerMenu associated with the player.
     */
    public PlayerMenu getPlayerMenu(Player player){
        PlayerMenu playerMenu;
        if (!(playerMenuMap.containsKey(player))) {
            playerMenu = new PlayerMenu(player);
            playerMenuMap.put(player, playerMenu);

            return playerMenu;
        } else {
            return playerMenuMap.get(player);
        }
    }
}

