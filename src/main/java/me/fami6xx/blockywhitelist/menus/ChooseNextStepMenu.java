package me.fami6xx.blockywhitelist.menus;

import me.fami6xx.blockywhitelist.BlockyWhitelist;
import me.fami6xx.rpuniverse.core.menuapi.types.Menu;
import me.fami6xx.rpuniverse.core.menuapi.utils.MenuTag;
import me.fami6xx.rpuniverse.core.menuapi.utils.PlayerMenu;
import me.fami6xx.rpuniverse.core.misc.utils.FamiUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public class ChooseNextStepMenu extends Menu {
    private final BlockyWhitelist blockyWhitelist = BlockyWhitelist.getInstance();
    public ChooseNextStepMenu(PlayerMenu menu) {
        super(menu);
    }

    @Override
    public String getMenuName() {
        return FamiUtils.format("&b&lBW &c&lChoose");
    }

    @Override
    public int getSlots() {
        return 18;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        switch (e.getCurrentItem().getType()) {
            case OAK_SIGN:
                if (playerMenu.getPendingAction() != null) {
                    player.sendMessage(FamiUtils.format("&c&lERROR! &7You have already something to write!"));
                    player.closeInventory();
                    return;
                }
                player.sendMessage(FamiUtils.format("&b&lBW &7Please write in chat the bot token."));
                player.closeInventory();
                playerMenu.setPendingAction(s -> {
                    blockyWhitelist.getJsonStore().botToken = s;
                    blockyWhitelist.getJsonStore().save();
                    player.sendMessage(FamiUtils.format("&b&lBW &7Bot token set to &a" + s));
                    if (!blockyWhitelist.getJsonStore().guildId.isEmpty()) {
                        List<String> errors = blockyWhitelist.loadJDA();
                        if (!errors.isEmpty()) {
                            player.sendMessage(FamiUtils.format("&b&lBW &cErrors while loading bot:"));
                            for (String error : errors) {
                                player.sendMessage(FamiUtils.format("&c- " + error));
                            }
                            return;
                        } else {
                            player.sendMessage(FamiUtils.format("&b&lBW &aBot loaded successfully!"));
                            blockyWhitelist.getLogger().info("Bot loaded successfully!");
                        }
                    }
                    open();
                });
                break;
            case COMPASS:
                if (playerMenu.getPendingAction() != null) {
                    player.sendMessage(FamiUtils.format("&c&lERROR! &7You have already something to write!"));
                    player.closeInventory();
                    return;
                }
                player.sendMessage(FamiUtils.format("&b&lBW &7Please write in chat the guild id."));
                player.closeInventory();
                playerMenu.setPendingAction(s -> {
                    blockyWhitelist.getJsonStore().guildId = s;
                    blockyWhitelist.getJsonStore().save();
                    player.sendMessage(FamiUtils.format("&b&lBW &7Guild id set to &a" + s));
                    if (!blockyWhitelist.getJsonStore().botToken.isEmpty()) {
                        List<String> errors = blockyWhitelist.loadJDA();
                        if (!errors.isEmpty()) {
                            player.sendMessage(FamiUtils.format("&b&lBW &cErrors while loading bot:"));
                            for (String error : errors) {
                                player.sendMessage(FamiUtils.format("&c- " + error));
                            }
                            return;
                        } else {
                            player.sendMessage(FamiUtils.format("&b&lBW &aBot loaded successfully!"));
                            blockyWhitelist.getLogger().info("Bot loaded successfully!");
                        }
                    }
                    open();
                });
                break;
            case PAPER:
                if (blockyWhitelist.getJDA() == null) {
                    player.sendMessage(FamiUtils.format("&b&lBW &cPlease set the bot token and guild id first."));
                    player.closeInventory();
                    return;
                }
                
                // Open the WhitelistersMenu
                WhitelistersMenu whitelistersMenu = new WhitelistersMenu(playerMenu);
                whitelistersMenu.open();
                break;
            case EMERALD:
                if (blockyWhitelist.getJDA() == null) {
                    player.sendMessage(FamiUtils.format("&b&lBW &cPlease set the bot token and guild id first."));
                    player.closeInventory();
                    return;
                }
                
                // Open the WhitelistRolesMenu
                WhitelistRolesMenu whitelistRolesMenu = new WhitelistRolesMenu(playerMenu);
                whitelistRolesMenu.open();
                break;
            case REDSTONE:
                if (blockyWhitelist.getJDA() == null) {
                    player.sendMessage(FamiUtils.format("&b&lBW &cPlease set the bot token and guild id first."));
                    player.closeInventory();
                    return;
                }

                // Open the FailedAttemptsMenu
                FailedAttemptsMenu failedAttemptsMenu = new FailedAttemptsMenu(playerMenu);
                failedAttemptsMenu.open();
                break;
        }
    }

    @Override
    public void setMenuItems() {
        if (blockyWhitelist.getJsonStore().botToken.isEmpty()) {
            inventory.setItem(1, FamiUtils.makeItem(Material.OAK_SIGN, "&c&lBot Token", "&7Click to set the bot token."));
        } else {
            inventory.setItem(1, FamiUtils.makeItem(Material.OAK_SIGN, "&a&lBot Token", "&7Bot token is set.", "&7Click to change."));
        }
        if (blockyWhitelist.getJsonStore().guildId.isEmpty()) {
            inventory.setItem(3, FamiUtils.makeItem(Material.COMPASS, "&c&lGuild Id", "&7Click to set the guild id."));
        } else {
            inventory.setItem(3, FamiUtils.makeItem(Material.COMPASS, "&a&lGuild Id", "&7Guild id is set.", "&7Click to change."));
        }
        inventory.setItem(5, FamiUtils.makeItem(Material.PAPER, "&a&lWhitelist Adders", "&7Click to manage which roles can add whitelist"));
        inventory.setItem(7, FamiUtils.makeItem(Material.EMERALD, "&a&lWhitelist roles", "&7Click to manage which roles are whitelisted"));
        inventory.setItem(12, FamiUtils.makeItem(Material.REDSTONE, "&a&lFailed Attempts", "&7Click to see failed attempts roles"));
        setFillerGlass();
    }

    @Override
    public List<MenuTag> getMenuTags() {
        return new ArrayList<>();
    }
}
