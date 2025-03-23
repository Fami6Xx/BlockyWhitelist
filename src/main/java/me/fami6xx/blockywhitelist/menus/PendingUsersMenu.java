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

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PendingUsersMenu extends EasyPaginatedMenu {
    private final UUID[] pendingPlayers;
    private final NamespacedKey key;
    private final Set<Map.Entry<String, UUID>> pendingPlayersEntrySet;

    public PendingUsersMenu(PlayerMenu menu) {
        super(menu);

        BlockyWhitelist plugin = BlockyWhitelist.getInstance();
        pendingPlayers = plugin.getJsonStore().pendingPlayers.values().toArray(new UUID[0]);
        pendingPlayersEntrySet = plugin.getJsonStore().pendingPlayers.entrySet();
        key = new NamespacedKey(plugin, "pending");
    }

    @Override
    public ItemStack getItemFromIndex(int index) {
        UUID uuid = pendingPlayers[index];
        String linkKey;

        linkKey = pendingPlayersEntrySet.stream().filter(entry -> entry.getValue().toString().equals(uuid.toString())).findFirst().get().getKey();

        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);

        ItemStack item = FamiUtils.makeSkullItem(p, "&b" + p.getName(), "&7Link key: &c" + linkKey, "&7Click to remove");
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, linkKey);
        item.setItemMeta(meta);

        return item;
    }

    @Override
    public int getCollectionSize() {
        return pendingPlayers.length;
    }

    @Override
    public void handlePaginatedMenu(InventoryClickEvent e) {
        if (e.getCurrentItem() == null) return;

        if (e.getCurrentItem().getItemMeta() == null) return;

        if (e.getCurrentItem().getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            String linkKey = e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
            BlockyWhitelist.getInstance().getJsonStore().pendingPlayers.remove(linkKey);
            BlockyWhitelist.getInstance().getJsonStore().save();
            open();
        }
    }

    @Override
    public void addAdditionalItems() {

    }

    @Override
    public String getMenuName() {
        return FamiUtils.formatWithPrefix("&cPending Users");
    }
}
