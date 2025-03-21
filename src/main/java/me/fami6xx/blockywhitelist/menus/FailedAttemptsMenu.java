package me.fami6xx.blockywhitelist.menus;

import me.fami6xx.blockywhitelist.BlockyWhitelist;
import me.fami6xx.blockywhitelist.JSONStore;
import me.fami6xx.blockywhitelist.utils.FamiUtils;
import me.fami6xx.blockywhitelist.utils.menus.PlayerMenu;
import me.fami6xx.blockywhitelist.utils.menus.types.Menu;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

public class FailedAttemptsMenu extends Menu {
    JSONStore store;
    Guild guild;

    public FailedAttemptsMenu(PlayerMenu menu) {
        super(menu);
        store = BlockyWhitelist.getInstance().getJsonStore();
        guild = BlockyWhitelist.getInstance().getGuild();
    }

    @Override
    public String getMenuName() {
        return FamiUtils.format("&b&lBW &cFailed Attempts");
    }

    @Override
    public int getSlots() {
        return 9;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        if (e.getCurrentItem() == null) return;
        switch (e.getCurrentItem().getType()) {
            case OAK_LOG:
                new ChooseRoleMenu(playerMenu) {
                    @Override
                    public void handleRoleSelection(Role role) {
                        store.failedRoleIdOne = role.getId();
                        store.save();
                        FailedAttemptsMenu.this.open();
                    }

                    @Override
                    public boolean isAlreadySelected(Role role) {
                        return role.getId().equals(store.failedRoleIdOne);
                    }
                }.open();
                break;
            case STONE:
                new ChooseRoleMenu(playerMenu) {
                    @Override
                    public void handleRoleSelection(Role role) {
                        store.failedRoleIdTwo = role.getId();
                        store.save();
                        FailedAttemptsMenu.this.open();
                    }

                    @Override
                    public boolean isAlreadySelected(Role role) {
                        return role.getId().equals(store.failedRoleIdTwo);
                    }
                }.open();
                break;
            case DIAMOND:
                new ChooseRoleMenu(playerMenu) {
                    @Override
                    public void handleRoleSelection(Role role) {
                        store.failedRoleIdThree = role.getId();
                        store.save();
                        FailedAttemptsMenu.this.open();
                    }

                    @Override
                    public boolean isAlreadySelected(Role role) {
                        return role.getId().equals(store.failedRoleIdThree);
                    }
                }.open();
                break;
        }
    }

    @Override
    public void setMenuItems() {
        // 2,4,6

        if (store.failedRoleIdOne.isEmpty())
            inventory.setItem(2, FamiUtils.makeItem(Material.OAK_LOG, "&c&lAttempt 1", "&7No role set"));
        else {
            String name = guild.getRoleById(store.failedRoleIdOne) == null ? "Role not found" : guild.getRoleById(store.failedRoleIdOne).getName();
            inventory.setItem(2, FamiUtils.makeItem(Material.OAK_LOG, "&c&lAttempt 1", "&7Role: &c" + name, "&7Click to change"));
        }

        if (store.failedRoleIdTwo.isEmpty())
            inventory.setItem(4, FamiUtils.makeItem(Material.STONE, "&c&lAttempt 2", "&7No role set"));
        else {
            String name = guild.getRoleById(store.failedRoleIdTwo) == null ? "Role not found" : guild.getRoleById(store.failedRoleIdTwo).getName();
            inventory.setItem(4, FamiUtils.makeItem(Material.STONE, "&c&lAttempt 2", "&7Role: &c" + name, "&7Click to change"));
        }

        if (store.failedRoleIdThree.isEmpty())
            inventory.setItem(6, FamiUtils.makeItem(Material.DIAMOND, "&c&lAttempt 3", "&7No role set"));
        else {
            String name = guild.getRoleById(store.failedRoleIdThree) == null ? "Role not found" : guild.getRoleById(store.failedRoleIdThree).getName();
            inventory.setItem(6, FamiUtils.makeItem(Material.DIAMOND, "&c&lAttempt 3", "&7Role: &c" + name, "&7Click to change"));
        }

        setFillerGlass();
    }
}
