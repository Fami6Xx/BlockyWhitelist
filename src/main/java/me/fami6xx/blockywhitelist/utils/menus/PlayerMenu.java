package me.fami6xx.blockywhitelist.utils.menus;

import me.fami6xx.blockywhitelist.utils.menus.types.Menu;
import org.bukkit.entity.Player;
import org.bukkit.util.Consumer;

public class PlayerMenu {
    private final Player player;
    private Menu currentMenu;
    private Consumer<String> pendingAction;

    public PlayerMenu(Player p) {
        this.player = p;
    }

    public Player getPlayer() {
        return player;
    }

    public void setCurrentMenu(Menu menu) {
        this.currentMenu = menu;
    }

    public Menu getCurrentMenu() {
        return currentMenu;
    }


    public void setPendingAction(Consumer<String> action) {
        this.pendingAction = action;
    }

    public Consumer<String> getPendingAction() {
        return pendingAction;
    }

    public void clearPendingAction() {
        this.pendingAction = null;
    }
}

