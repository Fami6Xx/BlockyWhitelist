package me.fami6xx.blockywhitelist;

import me.fami6xx.rpuniverse.core.misc.utils.FamiUtils;
import net.essentialsx.api.v2.services.discord.DiscordService;
import net.essentialsx.api.v2.services.discord.InteractionException;
import net.essentialsx.api.v2.services.discord.InteractionMember;
import net.essentialsx.api.v2.services.discordlink.DiscordLinkService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class BlockyWhitelist extends JavaPlugin implements Listener {
    private DiscordService discordService;
    private DiscordLinkService discordLinkService;
    private JSONStore jsonStore;

    @Override
    public void onEnable() {
        discordService = Bukkit.getServicesManager().load(DiscordService.class);
        discordLinkService = Bukkit.getServicesManager().load(DiscordLinkService.class);
        try {
            discordService.getInteractionController().registerCommand(new WhitelistSlashCommand());
        } catch (InteractionException e) {
            throw new RuntimeException(e);
        }

        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        File file = new File(getDataFolder(), "data.json");

        jsonStore = JSONStore.load(file);

        getServer().getPluginManager().registerEvents(this, this);

        this.getCommand("blockywhitelist").setExecutor(new BlockyWhitelistCommand());
    }

    @Override
    public void onDisable() {
        jsonStore.save();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final String discordId = getDiscordId(player);
        if (discordId == null) {
            kickPlayer(player);
            return;
        }

        InteractionMember member = discordService.getMemberById(discordId).join();
        if (member == null) {
            kickPlayer(player);
            return;
        }

        boolean whitelisted = false;
        for (String role : jsonStore.addedRoles) {
            if (member.hasRole(role)) {
                whitelisted = true;
                break;
            }
        }

        if (!whitelisted) {
            kickPlayer(player);
        }
    }

    private void kickPlayer(Player player) {
        player.kickPlayer(FamiUtils.format("&b&lBlockyWhitelist\n\n&r&cMusis projit whitelistem.\n\n&r&7Prosim pripoj se na nas discord a udelej si WhiteList."));
    }

    public JSONStore getJsonStore() {
        return jsonStore;
    }

    public static BlockyWhitelist getInstance() {
        return getPlugin(BlockyWhitelist.class);
    }

    public static DiscordService getDiscordService() {
        return getInstance().discordService;
    }

    public static DiscordLinkService getDiscordLinkService() {
        return getInstance().discordLinkService;
    }

    public static String getDiscordTag(final Player player) {
        final String discordId = getDiscordLinkService().getDiscordId(player.getUniqueId());
        if (discordId == null) {
            return null;
        }

        final InteractionMember member = getDiscordService().getMemberById(discordId).join();
        return member == null ? null : member.getTag();
    }

    public static String getDiscordId(final Player player) {
        final String discordId = getDiscordLinkService().getDiscordId(player.getUniqueId());
        if (discordId == null) {
            return null;
        }

        final InteractionMember member = getDiscordService().getMemberById(discordId).join();
        return member == null ? null : member.getId();
    }
}
