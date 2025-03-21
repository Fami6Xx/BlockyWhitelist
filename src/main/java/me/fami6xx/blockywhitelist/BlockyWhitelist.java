package me.fami6xx.blockywhitelist;

import me.fami6xx.blockywhitelist.discord.DiscordCommandListener;
import me.fami6xx.blockywhitelist.utils.FamiUtils;
import me.fami6xx.blockywhitelist.utils.languages.Lang;
import me.fami6xx.blockywhitelist.utils.languages.LocalizationUtil;
import me.fami6xx.blockywhitelist.utils.menus.MenuManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.*;

public final class BlockyWhitelist extends JavaPlugin implements Listener {
    private JSONStore jsonStore;
    private JDA jda;
    private Guild guild;
    private static final SecureRandom random = new SecureRandom();
    private boolean loadedMembers = false;
    private MenuManager menuManager;

    private File langFile;

    @Override
    public void onEnable() {
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

        langFile = new File(getDataFolder(), "languages.json");
        if (!langFile.exists()) {
            getLogger().info("Creating languages.json");
            try {
                LocalizationUtil.saveStaticTranslationsToFile(Lang.class, langFile);
                getLogger().info("Translations saved");
            } catch (IOException e) {
                getLogger().severe("Failed to save translations to " + langFile.getPath());
                getLogger().severe(e.getMessage());
            }
        } else {
            getLogger().info("Loading translations");
            try {
                LocalizationUtil.loadStaticTranslationsFromFile(Lang.class, langFile);
                getLogger().info("Translations loaded successfully");
            } catch (IOException e) {
                getLogger().severe("Failed to load translations from " + langFile.getPath());
                getLogger().severe(e.getMessage());
            }
        }

        menuManager = new MenuManager();

        if (!menuManager.enable()) {
            getLogger().severe("Failed to enable MenuManager! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
        }else{
            getLogger().info("MenuManager enabled!");
        }

        boolean firstSetup = jsonStore.botToken.isEmpty() || jsonStore.guildId.isEmpty();
        if (firstSetup) {
            getLogger().info("First setup detected, please configure the plugin in-game");
            getLogger().info("For setup, type /blockywhitelist");
            loadedMembers = true;
        } else {
            getLogger().info("Bot token and guild ID loaded successfully");
            List<String> errors = loadJDA();
            if (!errors.isEmpty()) {
                getLogger().severe("Errors encountered while loading JDA:");
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
            try {
                LocalizationUtil.saveStaticTranslationsToFile(Lang.class, langFile);
                getLogger().info("Translations saved");
            } catch (IOException e) {
                getLogger().severe("Failed to save translations to " + langFile.getPath());
                getLogger().severe(e.getMessage());
            }
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

        if (!loadedMembers) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text(getKickMessageLoadingMembers()));
            return;
        }

        if (jsonStore.linkedPlayers.containsKey(event.getUniqueId())) {
            String discordId = jsonStore.linkedPlayers.get(event.getUniqueId());
            if (discordId == null) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text(getKickMessageNotLinked(event.getUniqueId())));
                return;
            }
            if (guild.getMemberById(discordId) == null) {
                getLogger().warning("Discord ID " + discordId + " not found in guild");
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text(getKickMessageNotLinked(event.getUniqueId())));
                return;
            }
            Member member = guild.getMemberById(discordId);
            boolean whitelisted = member.getRoles().stream().anyMatch(role -> jsonStore.addedRoles.contains(role.getId()));
            if (!whitelisted) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, Component.text(getKickMessageNotWhitelisted()));
                return;
            }

            getLogger().info(event.getName() + " is whitelisted for @" + member.getEffectiveName() + " (" + member.getId() + ")");
            return;
        }

        if (!jsonStore.pendingPlayers.containsValue(event.getUniqueId()) || jsonStore.pendingPlayers.containsValue(event.getUniqueId())) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, Component.text(getKickMessageNotLinked(event.getUniqueId())));
            return;
        }
    }

    /**
     * Trys to load JDA and connect to the discord server.
     *
     * @return List of errors that occurred during the connection.
     */
    public List<String> loadJDA() {
        List<String> errors = new ArrayList<>();
        loadedMembers = false;
        try {
            jda = JDABuilder.createDefault(jsonStore.botToken)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .build()
                    .awaitReady();

            jda.setAutoReconnect(true);

            guild = jda.getGuildById(jsonStore.guildId);
            if (guild == null) {
                getLogger().severe("Failed to find guild with id " + jsonStore.guildId);
                throw new IllegalStateException("Failed to connect to Discord server");
            }

            jda.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.watching(guild.getName()), false);

            // Load members to cache
            guild.loadMembers().onSuccess(result -> {
                getLogger().info("Loaded " + result.size() + " members to cache");
                loadedMembers = true;
                getLogger().info("Connection of players is now allowed");
            });

            getLogger().info("Checking roles...");
            // Role check
            for (String roleId : jsonStore.allowedRoles) {
                if (guild.getRoleById(roleId) == null) {
                    getLogger().severe("Failed to find whitelist adder role with id " + roleId);
                    errors.add("Failed to find whitelist adder role with id " + roleId);
                }
            }

            for (String roleId : jsonStore.addedRoles) {
                if (guild.getRoleById(roleId) == null) {
                    getLogger().severe("Failed to find whitelisted role with id " + roleId);
                    errors.add("Failed to find whitelisted role with id " + roleId);
                }
            }

            if (!jsonStore.failedRoleIdOne.isEmpty() && guild.getRoleById(jsonStore.failedRoleIdOne) == null) {
                getLogger().severe("Failed to find failed attempt one role with id " + jsonStore.failedRoleIdOne);
                errors.add("Failed to find failed attempt one role with id " + jsonStore.failedRoleIdOne);
            }

            if (!jsonStore.failedRoleIdTwo.isEmpty() && guild.getRoleById(jsonStore.failedRoleIdTwo) == null) {
                getLogger().severe("Failed to find failed attempt two role with id " + jsonStore.failedRoleIdTwo);
                errors.add("Failed to find failed attempt two role with id " + jsonStore.failedRoleIdTwo);
            }

            if (!jsonStore.failedRoleIdThree.isEmpty() && guild.getRoleById(jsonStore.failedRoleIdThree) == null) {
                getLogger().severe("Failed to find failed attempt three role with id " + jsonStore.failedRoleIdThree);
                errors.add("Failed to find failed attempt three role with id " + jsonStore.failedRoleIdThree);
            }

            getLogger().info("Registering commands...");

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

            getLogger().info("Finished loading JDA");
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
        return FamiUtils.format("&b&lBlockyWhitelist\n\n&r&cNemas whitelisted roli.\n\n&r&7Prosim pripoj se na nas discord a udelej si WhiteList.");
    }

    private String getKickMessageNotSetup() {
        return FamiUtils.format("&b&lBlockyWhitelist\n\n&r&cServer neni nastaven.\n\n&r&7Prosim kontaktuj admina.");
    }

    private String getKickMessageLoadingMembers() {
        return FamiUtils.format("&b&lBlockyWhitelist\n\n&r&cServer se nacita.\n\n&r&7Zkus to znovu za chvili.");
    }

    private String getKickMessageNotLinked(UUID uuid) {
        try {
            if (jsonStore.pendingPlayers.containsValue(uuid)) {
                String code;
                try {
                    Collection<String> coll = jsonStore.pendingPlayers.keySet();
                    code = coll.stream().filter(s -> jsonStore.pendingPlayers.get(s).equals(uuid)).findFirst().orElse(null).toString();
                } catch (Exception e) {
                    getLogger().severe("Failed to get code for player " + uuid);
                    return FamiUtils.format("&b&lBlockyWhitelist\n\n&r&cMusis propojit ucet.\n\n&r&7Nepodarilo se nam ziskat tvuj kod, zkus to znovu.");
                }
                return FamiUtils.format("&b&lBlockyWhitelist\n\n&r&cMusis propojit ucet.\n\n&r&7Tvuj kod: &a" + code + "\n&r&7Prosim napis &a/link " + code + " &7do discordu.");
            }
            String code = generateRandomString(6);
            int attempts = 0;
            while (jsonStore.pendingPlayers.containsKey(code)) {
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
                    jsonStore.pendingPlayers.put(finalCode, uuid);
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

    /**
     * Gets the Instance of the BlockyWhitelist plugin.
     * @return Instance of the BlockyWhitelist plugin.
     */
    public static BlockyWhitelist getInstance() {
        return getPlugin(BlockyWhitelist.class);
    }

    /**
     * Get the MenuManager
     * @return The MenuManager
     */
    public MenuManager getMenuManager() {
        return menuManager;
    }
}
