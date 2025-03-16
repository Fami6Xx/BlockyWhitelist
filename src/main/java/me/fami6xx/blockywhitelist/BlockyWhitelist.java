package me.fami6xx.blockywhitelist;

import me.fami6xx.blockywhitelist.discord.DiscordCommandListener;
import me.fami6xx.rpuniverse.core.misc.utils.FamiUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.*;

public final class BlockyWhitelist extends JavaPlugin implements Listener {
    private JSONStore jsonStore;
    private Permission perms;
    private JDA jda;
    private Guild guild;
    private static final SecureRandom random = new SecureRandom();

    @Override
    public void onEnable() {
        if (!setupPermissions()) {
            getLogger().severe("Failed to hook Vault, disabling plugin");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        File file = new File(getDataFolder(), "data.json");

        jsonStore = JSONStore.load(file);

        if (jsonStore == null) {
            getLogger().severe("Failed to load data.json");
            getLogger().severe("Please delete BlockyWhitelist folder and restart the server");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        boolean firstSetup = jsonStore.botToken.isEmpty() || jsonStore.guildId.isEmpty();
        if (firstSetup) {
            getLogger().info("First setup detected, please configure the plugin in-game");
            getLogger().info("For setup, type /blockywhitelist");
        } else {
            getLogger().info("Bot token and guild ID loaded successfully");
            List<String> errors = loadJDA();
            if (!errors.isEmpty()) {
                getLogger().severe("Failed load JDA:");
                errors.forEach(getLogger()::severe);
                return;
            }
        }

        getServer().getPluginManager().registerEvents(this, this);

        this.getCommand("blockywhitelist").setExecutor(new BlockyWhitelistCommand());
    }

    @Override
    public void onDisable() {
        try {
            jsonStore.save();
            if (jda != null) {
                getLogger().info("Shutting down Discord bot");
                jda.shutdown();
                // Allow at most 10 seconds for remaining requests to finish
                if (!jda.awaitShutdown(Duration.ofSeconds(10))) {
                    jda.shutdownNow(); // Cancel all remaining requests
                    jda.awaitShutdown(); // Wait until shutdown is complete (indefinitely)
                }
            }
        } catch (Exception e) {
            getLogger().severe("Failed to shutdown");
            getLogger().severe(e.getMessage());
        }
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (jsonStore.botToken.isEmpty() || jsonStore.guildId.isEmpty() || jda == null || guild == null || jsonStore.addedRoles.isEmpty() || jsonStore.allowedRoles.isEmpty()) {
            return;
        }

        if (jsonStore.linkedPlayers.containsKey(event.getUniqueId())) {
            return;
        }

        if (!jsonStore.pendingPlayers.containsKey(event.getUniqueId()) || jsonStore.pendingPlayers.containsKey(event.getUniqueId())) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, Component.text(getKickMessageNotLinked(event.getUniqueId())));
        }
    }

    /**
     * Trys to load JDA and connect to the discord server.
     *
     * @return List of errors that occurred during the connection.
     */
    public List<String> loadJDA() {
        List<String> errors = new ArrayList<>();
        try {
            jda = JDABuilder.createDefault(jsonStore.botToken)
                    .build()
                    .awaitReady();

            jda.setAutoReconnect(true);
            jda.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.watching("BlockyRP"), false);

            guild = jda.getGuildById(jsonStore.guildId);
            if (guild == null) {
                getLogger().severe("Failed to find guild with id " + jsonStore.guildId);
                throw new IllegalStateException("Failed to connect to Discord server");
            }

            guild.upsertCommand("link", "Linkne váš Discord účet s MC účtem.")
                    .addOption(OptionType.STRING, "code", "Váš kód, zobrazen při napojení na MC server.", true)
                    .queue();

            guild.upsertCommand("whitelist", "Whitelistne hráče.")
                    .addOption(OptionType.USER, "user", "Hráč, kterého chcete whitelistnout.", true)
                    .queue();

            guild.upsertCommand("failed", "Hráči se nepodařil pokus o whitelist.")
                    .addOption(OptionType.INTEGER, "attempt", "Kolikátý pokus o whitelist to je.", true)
                    .addOption(OptionType.USER, "user", "Hráč, kterého se pokus týká.", true)
                    .queue();

            jda.addEventListener(new DiscordCommandListener());
        } catch (InvalidTokenException | IllegalArgumentException | IllegalStateException | InterruptedException e) {
            if (e instanceof InvalidTokenException) {
                errors.add("Invalid bot token");
            } else if (e instanceof IllegalArgumentException) {
                errors.add("Bot token is empty");
            } else if (e instanceof IllegalStateException) {
                errors.add("Something went wrong: " + e.getMessage());
            } else {
                errors.add("Failed initializing JDA: " + e.getMessage());
            }
        }
        return errors;
    }

    private String getKickMessageNotWhitelisted() {
        return FamiUtils.format("&b&lBlockyWhitelist\n\n&r&cMusis projit whitelistem.\n\n&r&7Prosim pripoj se na nas discord a udelej si WhiteList.");
    }

    private String getKickMessageNotSetup() {
        return FamiUtils.format("&b&lBlockyWhitelist\n\n&r&cServer neni nastaven.\n\n&r&7Prosim kontaktuj admina.");
    }

    private String getKickMessageNotLinked(UUID uuid) {
        try {
            if (jsonStore.pendingPlayers.containsKey(uuid)) {
                String code;
                try {
                    code = jsonStore.pendingPlayers.get(uuid);
                } catch (Exception e) {
                    getLogger().severe("Failed to get code for player " + uuid);
                    return FamiUtils.format("&b&lBlockyWhitelist\n\n&r&cMusis propojit ucet.\n\n&r&7Nepodarilo se nam ziskat tvuj kod, zkus to znovu.");
                }
                return FamiUtils.format("&b&lBlockyWhitelist\n\n&r&cMusis propojit ucet.\n\n&r&7Tvuj kod: &a" + code + "\n&r&7Prosim napis &a/link " + code + " &7do discordu.");
            }
            String code = generateRandomString(6);
            int attempts = 0;
            while (jsonStore.pendingPlayers.containsValue(code)) {
                if (attempts >= 10) {
                    getLogger().severe("Failed to generate a unique code");
                    return FamiUtils.format("&b&lBlockyWhitelist\n\n&r&cMusis propojit ucet.\n\n&r&7Prosim kontaktuj admina.");
                }
                code = generateRandomString(6);
                attempts++;
            }
            String finalCode = code;
            new BukkitRunnable() {
                @Override
                public void run() {
                    jsonStore.pendingPlayers.put(uuid, finalCode);
                    jsonStore.save();
                }
            }.runTask(this);
            return FamiUtils.format("&b&lBlockyWhitelist\n\n&r&cMusis propojit ucet.\n\n&r&7Tvuj kod: &a" + code + "\n&r&7Prosim napis &a/link " + code + " &7do discordu.");
        } catch (Exception e) {
            getLogger().severe("Failed to get code for player " + uuid);
            return FamiUtils.format("&b&lBlockyWhitelist\n\n&r&cMusis propojit ucet.\n\n&r&7Nepodarilo se nam ziskat tvuj kod, zkus to znovu.");
        }
    }

    /**
     * Generates a random string of the given length.
     * @param length Length of the string.
     * @return Random string.
     */
    public static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public JSONStore getJsonStore() {
        return jsonStore;
    }

    public JDA getJDA() {
        return jda;
    }

    public Guild getGuild() {
        return guild;
    }

    public Permission getPerms() {
        return perms;
    }

    /**
     * Setup Vault permissions by obtaining the Permission provider.
     */
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp != null) {
            perms = rsp.getProvider();
        }
        return perms != null;
    }

    /**
     * Gets the Instance of the BlockyWhitelist plugin.
     * @return Instance of the BlockyWhitelist plugin.
     */
    public static BlockyWhitelist getInstance() {
        return getPlugin(BlockyWhitelist.class);
    }
}
