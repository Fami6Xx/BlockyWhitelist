package me.fami6xx.blockywhitelist.menus;

import me.fami6xx.blockywhitelist.BlockyWhitelist;
import me.fami6xx.blockywhitelist.utils.FamiUtils;
import me.fami6xx.blockywhitelist.utils.menus.PlayerMenu;
import me.fami6xx.blockywhitelist.utils.menus.types.EasyPaginatedMenu;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class WhitelistRolesMenu extends EasyPaginatedMenu {
    private final BlockyWhitelist blockyWhitelist;
    private final NamespacedKey blockyWhitelistKey;
    private final Guild guild;

    public WhitelistRolesMenu(PlayerMenu menu) {
        super(menu);
        blockyWhitelist = BlockyWhitelist.getInstance();
        blockyWhitelistKey = new NamespacedKey(blockyWhitelist, "whitelist");
        guild = blockyWhitelist.getGuild();
    }

    @Override
    public ItemStack getItemFromIndex(int index) {
        Role role = guild.getRoleById(blockyWhitelist.getJsonStore().addedRoles.get(index));
        if (role == null) {
            ItemStack barrier = FamiUtils.makeItem(Material.BARRIER, "&c&lERROR! &7Role not found!", "&7Role ID: &c" + blockyWhitelist.getJsonStore().addedRoles.get(index) ,"&7Click to remove");
            ItemMeta meta = barrier.getItemMeta();
            if (meta == null) return null;
            NamespacedKey key = new NamespacedKey(blockyWhitelist, "whitelist");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, blockyWhitelist.getJsonStore().addedRoles.get(index));
            barrier.setItemMeta(meta);
            return barrier;
        }
        ItemStack item = FamiUtils.makeItem(Material.EMERALD, FamiUtils.format("&b" + role.getName()), "&7Click to remove");
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        meta.getPersistentDataContainer().set(blockyWhitelistKey, PersistentDataType.STRING, role.getId());
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public int getCollectionSize() {
        return blockyWhitelist.getJsonStore().addedRoles.size();
    }

    @Override
    public void handlePaginatedMenu(InventoryClickEvent e) {
        if (e.getCurrentItem() == null) return;

        if (e.getCurrentItem().getType() == Material.EMERALD_BLOCK) {
            new ChooseRoleMenu(playerMenu) {
                @Override
                public void handleRoleSelection(Role role) {
                    blockyWhitelist.getJsonStore().addedRoles.add(role.getId());
                    blockyWhitelist.getJsonStore().save();
                    WhitelistRolesMenu.this.open();
                }

                @Override
                public boolean isAlreadySelected(Role role) {
                    return blockyWhitelist.getJsonStore().addedRoles.contains(role.getId());
                }
            }.open();
            return;
        }

        ItemMeta meta = e.getCurrentItem().getItemMeta();
        if (meta == null) return;
        String roleId = meta.getPersistentDataContainer().get(blockyWhitelistKey, PersistentDataType.STRING);
        if (roleId == null) return;
        blockyWhitelist.getJsonStore().addedRoles.remove(roleId);
        blockyWhitelist.getJsonStore().save();
        open();
    }

    @Override
    public void addAdditionalItems() {
        inventory.setItem(45, FamiUtils.makeItem(Material.EMERALD_BLOCK, "&aAdd Role", "&7Click to add a new role"));
    }

    @Override
    public String getMenuName() {
        return FamiUtils.format("&b&lBW &cWhitelist Roles");
    }
}
