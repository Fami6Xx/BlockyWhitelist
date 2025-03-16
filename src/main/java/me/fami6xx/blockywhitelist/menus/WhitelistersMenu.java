package me.fami6xx.blockywhitelist.menus;

import me.fami6xx.blockywhitelist.BlockyWhitelist;
import me.fami6xx.rpuniverse.core.menuapi.types.EasyPaginatedMenu;
import me.fami6xx.rpuniverse.core.menuapi.utils.MenuTag;
import me.fami6xx.rpuniverse.core.menuapi.utils.PlayerMenu;
import me.fami6xx.rpuniverse.core.misc.utils.FamiUtils;
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

public class WhitelistersMenu extends EasyPaginatedMenu {
    private final BlockyWhitelist blockyWhitelist;
    private final Guild guild;

    public WhitelistersMenu(PlayerMenu menu) {
        super(menu);
        blockyWhitelist = BlockyWhitelist.getInstance();
        guild = blockyWhitelist.getGuild();
    }

    @Override
    public ItemStack getItemFromIndex(int index) {
        Role role = guild.getRoleById(blockyWhitelist.getJsonStore().allowedRoles.get(index));
        if (role == null) {
            ItemStack barrier = FamiUtils.makeItem(Material.BARRIER, "&c&lERROR! &7Role not found!", "&7Role ID: &c" + blockyWhitelist.getJsonStore().allowedRoles.get(index) ,"&7Click to remove");
            ItemMeta meta = barrier.getItemMeta();
            if (meta == null) return null;
            NamespacedKey key = new NamespacedKey(blockyWhitelist, "whitelist");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, blockyWhitelist.getJsonStore().allowedRoles.get(index));
            barrier.setItemMeta(meta);
            return barrier;
        }
        ItemStack item = FamiUtils.makeItem(Material.PAPER, FamiUtils.format("&b" + role.getName()), "&7Click to remove");
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        NamespacedKey key = new NamespacedKey(blockyWhitelist, "whitelist");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, role.getId());
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public int getCollectionSize() {
        return blockyWhitelist.getJsonStore().allowedRoles.size();
    }

    @Override
    public void handlePaginatedMenu(InventoryClickEvent e) {
        if (e.getCurrentItem() == null) return;

        if (e.getCurrentItem().getType() == Material.EMERALD_BLOCK) {
            new ChooseRoleMenu(playerMenu) {
                @Override
                public void handleRoleSelection(Role role) {
                    blockyWhitelist.getJsonStore().allowedRoles.add(role.getId());
                    blockyWhitelist.getJsonStore().save();
                    WhitelistersMenu.this.open();
                }

                @Override
                public boolean isAlreadySelected(Role role) {
                    return blockyWhitelist.getJsonStore().allowedRoles.contains(role.getId());
                }
            }.open();
            return;
        }

        ItemMeta meta = e.getCurrentItem().getItemMeta();
        if (meta == null) return;
        NamespacedKey key = new NamespacedKey(blockyWhitelist, "whitelist");
        String roleId = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (roleId == null) return;
        blockyWhitelist.getJsonStore().allowedRoles.remove(roleId);
        blockyWhitelist.getJsonStore().save();
        open();
    }

    @Override
    public void addAdditionalItems() {
        inventory.setItem(45, FamiUtils.makeItem(Material.EMERALD_BLOCK, "&aAdd Role", "&7Click to add a new role"));
    }

    @Override
    public String getMenuName() {
        return FamiUtils.format("&b&lBW &cWhitelisters");
    }

    @Override
    public List<MenuTag> getMenuTags() {
        return new ArrayList<>();
    }
}
