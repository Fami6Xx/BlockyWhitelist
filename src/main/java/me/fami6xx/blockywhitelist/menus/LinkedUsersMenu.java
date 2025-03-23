package me.fami6xx.blockywhitelist.menus;

import me.fami6xx.blockywhitelist.BlockyWhitelist;
import me.fami6xx.blockywhitelist.utils.FamiUtils;
import me.fami6xx.blockywhitelist.utils.menus.PlayerMenu;
import me.fami6xx.blockywhitelist.utils.menus.types.EasyPaginatedMenu;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.UUID;

public class LinkedUsersMenu extends EasyPaginatedMenu {
    private final HashMap<UUID, String> linkedPlayers;
    private final UUID[] linkedPlayersKeys;
    private final NamespacedKey key;

    public LinkedUsersMenu(PlayerMenu menu) {
        super(menu);
        BlockyWhitelist plugin = BlockyWhitelist.getInstance();
        linkedPlayers = (HashMap<UUID, String>) plugin.getJsonStore().linkedPlayers.clone();
        linkedPlayersKeys = linkedPlayers.keySet().toArray(new UUID[0]);
        key = new NamespacedKey(plugin, "linked");
    }

    @Override
    public ItemStack getItemFromIndex(int index) {
        UUID uuid = linkedPlayersKeys[index];
        String discordId = linkedPlayers.get(uuid);

        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);

        ItemStack item = FamiUtils.makeSkullItem(p, "&b" + p.getName(), "&7Discord ID: &c" + discordId, "&7Click to unlink");
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, uuid.toString());
        item.setItemMeta(meta);

        return item;
    }

    @Override
    public int getCollectionSize() {
        return linkedPlayersKeys.length;
    }

    @Override
    public void handlePaginatedMenu(InventoryClickEvent e) {
        if (e.getCurrentItem() == null) return;

        if (e.getCurrentItem().getItemMeta() == null) return;

        if (e.getCurrentItem().getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            UUID uuid = UUID.fromString(e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING));
            BlockyWhitelist.getInstance().getJsonStore().linkedPlayers.remove(uuid);
            BlockyWhitelist.getInstance().getJsonStore().save();
            open();
        }
    }

    @Override
    public void addAdditionalItems() {

    }

    @Override
    public String getMenuName() {
        return FamiUtils.formatWithPrefix("&cLinked Users");
    }
}
