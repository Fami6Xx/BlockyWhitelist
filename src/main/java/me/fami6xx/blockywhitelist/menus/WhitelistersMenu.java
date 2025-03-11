package me.fami6xx.blockywhitelist.menus;

import me.fami6xx.blockywhitelist.BlockyWhitelist;
import me.fami6xx.rpuniverse.core.menuapi.types.EasyPaginatedMenu;
import me.fami6xx.rpuniverse.core.menuapi.utils.MenuTag;
import me.fami6xx.rpuniverse.core.menuapi.utils.PlayerMenu;
import me.fami6xx.rpuniverse.core.misc.utils.FamiUtils;
import net.essentialsx.api.v2.services.discord.DiscordService;
import net.essentialsx.api.v2.services.discord.InteractionRole;
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
    private final DiscordService discordService;

    public WhitelistersMenu(PlayerMenu menu) {
        super(menu);
        blockyWhitelist = BlockyWhitelist.getInstance();
        discordService = BlockyWhitelist.getDiscordService();
    }

    @Override
    public ItemStack getItemFromIndex(int index) {
        InteractionRole role = discordService.getRole(blockyWhitelist.getJsonStore().allowedRoles.get(index));
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
            if (playerMenu.getPendingAction() != null) {
                playerMenu.getPlayer().sendMessage(FamiUtils.format("&c&lERROR! &7You have already something to write!"));
            }
            playerMenu.getPlayer().closeInventory();
            playerMenu.getPlayer().sendMessage(FamiUtils.format("&b&lBlockyWhitelist"));
            playerMenu.getPlayer().sendMessage(FamiUtils.format("&7Please write the id of the role you want to add to the whitelist adders."));
            playerMenu.setPendingAction((s -> {
                if (discordService.getRole(s) == null) {
                    playerMenu.getPlayer().sendMessage(FamiUtils.format("&c&lERROR! &7Role not found!"));
                    return;
                }
                blockyWhitelist.getJsonStore().allowedRoles.add(s);
                blockyWhitelist.getJsonStore().save();
                open();
            }));
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
