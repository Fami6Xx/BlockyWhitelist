package me.fami6xx.blockywhitelist.menus;

import me.fami6xx.rpuniverse.core.menuapi.types.Menu;
import me.fami6xx.rpuniverse.core.menuapi.utils.MenuTag;
import me.fami6xx.rpuniverse.core.menuapi.utils.PlayerMenu;
import me.fami6xx.rpuniverse.core.misc.utils.FamiUtils;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public class ChooseNextStepMenu extends Menu {
    public ChooseNextStepMenu(PlayerMenu menu) {
        super(menu);
    }

    @Override
    public String getMenuName() {
        return FamiUtils.format("&b&lBW &c&lChoose");
    }

    @Override
    public int getSlots() {
        return 9;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        switch (e.getCurrentItem().getType()) {
            case PAPER:
                // Open the WhitelistersMenu
                WhitelistersMenu whitelistersMenu = new WhitelistersMenu(playerMenu);
                whitelistersMenu.open();
                break;
            case EMERALD:
                // Open the WhitelistRolesMenu
                WhitelistRolesMenu whitelistRolesMenu = new WhitelistRolesMenu(playerMenu);
                whitelistRolesMenu.open();
                break;
        }
    }

    @Override
    public void setMenuItems() {
        //2,6
        inventory.setItem(2, FamiUtils.makeItem(Material.PAPER, "&a&lWhitelisters", "&7Click to manage which roles can add whitelist"));
        inventory.setItem(6, FamiUtils.makeItem(Material.EMERALD, "&a&lWhitelist roles", "&7Click to manage which roles are whitelisted"));
        setFillerGlass();
    }

    @Override
    public List<MenuTag> getMenuTags() {
        return new ArrayList<>();
    }
}
