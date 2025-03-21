package me.fami6xx.blockywhitelist.utils.menus.types;

import me.fami6xx.blockywhitelist.utils.FamiUtils;
import me.fami6xx.blockywhitelist.utils.languages.Lang;
import me.fami6xx.blockywhitelist.utils.menus.PlayerMenu;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public abstract class PaginatedMenu extends Menu {
    protected int page = 0;
    protected int maxItemsPerPage = 28;
    protected int index = 0;

    public final static ItemStack BORDER_GLASS = makeColoredGlass(DyeColor.GRAY);

    public PaginatedMenu(PlayerMenu menu){
        super(menu);
    }

    /**
     * Adds the menu border to the inventory.
     */
    public void addMenuBorder(){
        inventory.setItem(48, FamiUtils.makeItem(Material.STONE_BUTTON, FamiUtils.format(Lang.previousPageItemDisplayName), FamiUtils.format(Lang.previousPageItemLore)));

        inventory.setItem(49, FamiUtils.makeItem(Material.BARRIER, FamiUtils.format(Lang.closeItemDisplayName), FamiUtils.format(Lang.closeItemLore)));

        inventory.setItem(50, FamiUtils.makeItem(Material.STONE_BUTTON, FamiUtils.format(Lang.nextPageItemDisplayName), FamiUtils.format(Lang.nextPageItemLore)));

        for (int i = 0; i < 10; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, BORDER_GLASS);
            }
        }

        // 17, 18, 26, 27, 35, 36

        inventory.setItem(17, BORDER_GLASS);
        inventory.setItem(18, BORDER_GLASS);
        inventory.setItem(26, BORDER_GLASS);
        inventory.setItem(27, BORDER_GLASS);
        inventory.setItem(35, BORDER_GLASS);
        inventory.setItem(36, BORDER_GLASS);

        for (int i = 44; i < 54; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, BORDER_GLASS);
            }
        }
    }

    /**
     * Returns the maximum number of items per page.
     *
     * @return the maximum number of items per page.
     */
    public int getMaxItemsPerPage() {
        return maxItemsPerPage;
    }

    /**
     * Returns the number of slots in the inventory.
     *
     * @return the number of slots in the inventory.
     */
    @Override
    public int getSlots(){
        return 54;
    }
}