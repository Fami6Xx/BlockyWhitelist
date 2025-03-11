package me.fami6xx.blockywhitelist;

import net.essentialsx.api.v2.services.discord.DiscordService;
import net.essentialsx.api.v2.services.discord.InteractionException;
import net.essentialsx.api.v2.services.discord.InteractionMember;
import net.essentialsx.api.v2.services.discordlink.DiscordLinkService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class BlockyWhitelist extends JavaPlugin {
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

        this.getCommand("blockywhitelist").setExecutor(new BlockyWhitelistCommand());
    }

    @Override
    public void onDisable() {
        jsonStore.save();
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
}
