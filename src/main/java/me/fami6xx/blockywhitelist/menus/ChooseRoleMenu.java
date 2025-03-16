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

public abstract class ChooseRoleMenu extends EasyPaginatedMenu {
    private final Guild guild;
    private final BlockyWhitelist blockyWhitelist;

    public ChooseRoleMenu(PlayerMenu menu) {
        super(menu);
        blockyWhitelist = BlockyWhitelist.getInstance();
        guild = blockyWhitelist.getGuild();
    }

    @Override
    public String getMenuName() {
        return FamiUtils.format("&b&lBW &cChoose a role");
    }

    @Override
    public List<MenuTag> getMenuTags() {
        return new ArrayList<>();
    }

    @Override
    public ItemStack getItemFromIndex(int index) {
        Role role = guild.getRoles().get(index);
        ItemStack item = FamiUtils.makeItem(Material.PAPER, FamiUtils.format("&b" + role.getName()), "&7Click to select");
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        NamespacedKey key = new NamespacedKey(blockyWhitelist, "role");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, role.getId());
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public int getCollectionSize() {
        return guild.getRoles().size();
    }

    @Override
    public void handlePaginatedMenu(InventoryClickEvent e) {
        if (e.getCurrentItem() == null) return;
        if (e.getCurrentItem().getItemMeta() == null) return;
        if (e.getCurrentItem().getItemMeta().getPersistentDataContainer().isEmpty()) return;
        NamespacedKey key = new NamespacedKey(blockyWhitelist, "role");
        String roleId = e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (roleId == null) return;
        Role role = guild.getRoleById(roleId);
        if (role == null) return;
        handleRoleSelection(role);
    }

    @Override
    public void addAdditionalItems() {}

    public abstract void handleRoleSelection(Role role);
}
