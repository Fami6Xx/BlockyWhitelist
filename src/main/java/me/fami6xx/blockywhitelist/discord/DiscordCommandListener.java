package me.fami6xx.blockywhitelist.discord;

import me.fami6xx.blockywhitelist.BlockyWhitelist;
import me.fami6xx.blockywhitelist.JSONStore;
import me.fami6xx.blockywhitelist.utils.FamiUtils;
import me.fami6xx.blockywhitelist.utils.languages.Lang;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DiscordCommandListener extends ListenerAdapter {
    private final Map<String, Long> lastCommandUsage = new HashMap<>();
    private static final long COMMAND_COOLDOWN_MS = 1500;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String userId = event.getUser().getId();
        long currentTime = System.currentTimeMillis();
        if (lastCommandUsage.containsKey(userId)) {
            long lastUsage = lastCommandUsage.get(userId);
            if (currentTime - lastUsage < COMMAND_COOLDOWN_MS) {
                event.reply("Please wait before using commands again.")
                        .setEphemeral(true)
                        .queue();
                return;
            }
        }
        lastCommandUsage.put(userId, currentTime);

        String command = event.getName();

        if ("whitelist".equalsIgnoreCase(command)) {
            if (event.getMember() == null) {
                event.reply(Lang.errorNotDiscordMember)
                        .setEphemeral(true)
                        .queue();
                return;
            }

            if (event.getGuild() == null) {
                event.reply(Lang.errorNotGuild)
                        .setEphemeral(true)
                        .queue();
                return;
            }

            BlockyWhitelist plugin = BlockyWhitelist.getInstance();
            JSONStore jsonStore = plugin.getJsonStore();

            if (!event.getGuild().getId().equals(jsonStore.guildId)) {
                event.reply(Lang.errorNotGuild)
                        .setEphemeral(true)
                        .queue();
                return;
            }

            boolean hasPermission = event.getMember().getRoles().stream()
                    .anyMatch(role -> jsonStore.allowedRoles.contains(role.getId()));

            if (!hasPermission) {
                event.reply(Lang.errorNoPermission)
                        .setEphemeral(true)
                        .queue();
                return;
            }

            if (event.getOption("user") == null) {
                event.reply(Lang.errorNoUserProvided)
                        .setEphemeral(true)
                        .queue();
                return;
            }

            User toWhitelistUser = event.getOption("user").getAsUser();

            List<Role> toAdd = event.getGuild().getRoles().stream()
                    .filter(role -> jsonStore.addedRoles.contains(role.getId()))
                    .toList();

            if (toAdd.isEmpty()) {
                event.reply(Lang.errorNoRoleSet)
                        .setEphemeral(true)
                        .queue();
                return;
            }

            List<Member> usersWithRoles = event.getGuild().getMembersWithRoles(toAdd);
            if (usersWithRoles.stream().anyMatch(member -> member.getId().equals(toWhitelistUser.getId()))) {
                HashMap<String, String> replace = new HashMap<>();
                replace.put("{player}", toWhitelistUser.getAsMention());
                event.reply(FamiUtils.replaceAndFormat(Lang.errorPlayerAlreadyWhitelisted, replace))
                        .setEphemeral(true)
                        .queue();
                return;
            }

            HashMap<String, String> replace = new HashMap<>();
            replace.put("{user}", toWhitelistUser.getAsMention());
            event.reply(FamiUtils.replaceAndFormat(Lang.areYouSureToWhitelist, replace))
                    .addActionRow(
                            Button.success("confirm-bwl-" + toWhitelistUser.getId(), Lang.areYouSureConfirmButton),
                            Button.danger("cancel-bwl", Lang.areYouSureCancelButton)
                    )
                    .setEphemeral(true)
                    .queue();

            plugin.getLogger().info(
                    String.format("User %s (%s) executed %s command for target %s",
                            event.getUser().getName(),
                            event.getUser().getId(),
                            command,
                            toWhitelistUser.getName())
            );
        }

        if ("link".equalsIgnoreCase(command)) {
            if (event.getMember() == null) {
                event.reply(Lang.errorNotDiscordMember)
                        .setEphemeral(true)
                        .queue();
                return;
            }

            if (event.getGuild() == null) {
                event.reply(Lang.errorNotGuild)
                        .setEphemeral(true)
                        .queue();
                return;
            }

            BlockyWhitelist plugin = BlockyWhitelist.getInstance();
            JSONStore jsonStore = plugin.getJsonStore();

            if (!event.getGuild().getId().equals(jsonStore.guildId)) {
                event.reply(Lang.errorNotGuild)
                        .setEphemeral(true)
                        .queue();
                return;
            }

            if (event.getOption("code") == null) {
                event.reply(Lang.errorNoCodeProvided)
                        .setEphemeral(true)
                        .queue();
                return;
            }

            if (jsonStore.linkedPlayers.containsValue(event.getMember().getId())) {
                event.reply(Lang.errorAccountAlreadyLinked)
                        .setEphemeral(true)
                        .queue();
                return;
            }

            String code = event.getOption("code").getAsString();
            // Validate the code format
            if (!code.matches("^[A-Za-z0-9]{6}$")) {
                event.reply("Invalid code format.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            if (!jsonStore.pendingPlayers.containsKey(code)) {
                event.reply(Lang.errorInvalidCode)
                        .setEphemeral(true)
                        .queue();
                return;
            }

            UUID toLinkUser = jsonStore.pendingPlayers.get(code);

            jsonStore.pendingPlayers.remove(code);
            jsonStore.linkedPlayers.put(toLinkUser, event.getMember().getId());
            jsonStore.save();

            event.reply(Lang.successAccountLinked)
                    .setEphemeral(true)
                    .queue();
        }

        if ("failed".equalsIgnoreCase(command)) {
            if (event.getMember() == null) {
                event.reply(Lang.errorNotDiscordMember)
                        .setEphemeral(true)
                        .queue();
                return;
            }

            if (event.getGuild() == null) {
                event.reply(Lang.errorNotGuild)
                        .setEphemeral(true)
                        .queue();
                return;
            }

            BlockyWhitelist plugin = BlockyWhitelist.getInstance();
            JSONStore jsonStore = plugin.getJsonStore();

            if (!event.getGuild().getId().equals(jsonStore.guildId)) {
                event.reply(Lang.errorNotGuild)
                        .setEphemeral(true)
                        .queue();
                return;
            }

            boolean hasPermission = event.getMember().getRoles().stream()
                    .anyMatch(role -> jsonStore.allowedRoles.contains(role.getId()));

            if (!hasPermission) {
                event.reply(Lang.errorNoPermission)
                        .setEphemeral(true)
                        .queue();
                return;
            }

            if (event.getOption("attempt") == null) {
                event.reply(Lang.errorNoAttemptProvided)
                        .setEphemeral(true)
                        .queue();
                return;
            }

            if (event.getOption("user") == null) {
                event.reply(Lang.errorNoUserProvided)
                        .setEphemeral(true)
                        .queue();
                return;
            }

            int attempt = event.getOption("attempt").getAsInt();
            User toFailUser = event.getOption("user").getAsUser();

            if (attempt < 1 || attempt > 3) {
                event.reply(Lang.errorInvalidAttempt)
                        .setEphemeral(true)
                        .queue();
                return;
            }

            String roleId = switch (attempt) {
                case 1 -> jsonStore.failedRoleIdOne;
                case 2 -> jsonStore.failedRoleIdTwo;
                case 3 -> jsonStore.failedRoleIdThree;
                default -> null;
            };

            if (roleId == null || roleId.isEmpty()) {
                event.reply(Lang.errorNoRoleForAttempt)
                        .setEphemeral(true)
                        .queue();
                return;
            }

            Role role = event.getGuild().getRoleById(roleId);
            if (role == null) {
                event.reply(Lang.errorRoleNotFound)
                        .setEphemeral(true)
                        .queue();
                return;
            }

            if (event.getGuild().getMembersWithRoles(role).stream().anyMatch(member -> member.getId().equals(toFailUser.getId()))) {
                HashMap<String, String> replace = new HashMap<>();
                replace.put("{player}", toFailUser.getAsMention());
                event.reply(FamiUtils.replaceAndFormat(Lang.errorPlayerAlreadyHasRole, replace))
                        .setEphemeral(true)
                        .queue();
                return;
            }

            event.getGuild().addRoleToMember(toFailUser, role).queue();
            HashMap<String, String> replace = new HashMap<>();
            replace.put("{player}", toFailUser.getAsMention());
            replace.put("{attempt}", String.valueOf(attempt));
            event.reply(FamiUtils.replaceAndFormat(Lang.successRoleGivenForAttempt, replace))
                    .queue();

            plugin.getLogger().info(
                    String.format("User %s (%s) executed %s command for target %s",
                            event.getUser().getName(),
                            event.getUser().getId(),
                            command,
                            toFailUser.getName())
            );
        }

        if ("username".equalsIgnoreCase(command)) {
            if (event.getMember() == null) {
                event.reply(Lang.errorNotDiscordMember)
                        .setEphemeral(true)
                        .queue();
                return;
            }
            if (event.getGuild() == null) {
                event.reply(Lang.errorNotGuild)
                        .setEphemeral(true)
                        .queue();
                return;
            }

            BlockyWhitelist plugin = BlockyWhitelist.getInstance();
            JSONStore jsonStore = plugin.getJsonStore();

            boolean hasPermission = event.getMember().getRoles().stream()
                    .anyMatch(role -> jsonStore.allowedRoles.contains(role.getId()));

            if (!hasPermission) {
                event.reply(Lang.errorNoPermission)
                        .setEphemeral(true)
                        .queue();
                return;
            }

            if (!event.getGuild().getId().equals(jsonStore.guildId)) {
                event.reply(Lang.errorNotGuild)
                        .setEphemeral(true)
                        .queue();
                return;
            }

            if (event.getOption("user") == null) {
                event.reply(Lang.errorNoUserProvided)
                        .setEphemeral(true)
                        .queue();
                return;
            }

            // Find the Minecraft UUID linked to the Discord member.
            String discordId = event.getOption("user").getAsUser().getId();
            UUID minecraftUUID = null;

            for (Map.Entry<UUID, String> entry : jsonStore.linkedPlayers.entrySet()) {
                if (entry.getValue().equals(discordId)) {
                    minecraftUUID = entry.getKey();
                    break;
                }
            }

            if (minecraftUUID == null) {
                event.reply(Lang.errorNoAccountLinked)
                        .setEphemeral(true)
                        .queue();
                return;
            }

            // Get the Minecraft username using Bukkit's API.
            OfflinePlayer player = Bukkit.getOfflinePlayer(minecraftUUID);
            String username = player.getName();
            if (username == null) {
                username = Lang.unknown;
            }

            HashMap<String, String> replace = new HashMap<>();
            replace.put("{player}", event.getMember().getAsMention());
            replace.put("{username}", username);
            event.reply(FamiUtils.replaceAndFormat(Lang.successUsernameRetrieved, replace))
                    .setEphemeral(true)
                    .queue();

            plugin.getLogger().info(
                    String.format("User %s (%s) executed %s command for target %s",
                            event.getUser().getName(),
                            event.getUser().getId(),
                            command,
                            username)
            );
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().contentEquals("confirm-bwl-")) {
            String userId = event.getComponentId().substring(11);
            BlockyWhitelist plugin = BlockyWhitelist.getInstance();
            JSONStore jsonStore = plugin.getJsonStore();

            User toWhitelistUser = event.getJDA().getUserById(userId);
            if (toWhitelistUser == null) {
                event.reply("User not found.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            List<Role> toAdd = event.getGuild().getRoles().stream()
                    .filter(role -> jsonStore.addedRoles.contains(role.getId()))
                    .toList();

            if (toAdd.isEmpty()) {
                event.reply(Lang.errorNoRoleSet)
                        .setEphemeral(true)
                        .queue();
                return;
            }

            List<Member> usersWithRoles = event.getGuild().getMembersWithRoles(toAdd);
            if (usersWithRoles.stream().anyMatch(member -> member.getId().equals(toWhitelistUser.getId()))) {
                HashMap<String, String> replace = new HashMap<>();
                replace.put("{player}", toWhitelistUser.getAsMention());
                event.reply(FamiUtils.replaceAndFormat(Lang.errorPlayerAlreadyWhitelisted, replace))
                        .setEphemeral(true)
                        .queue();
                return;
            }

            for (Role role : toAdd) {
                event.getGuild().addRoleToMember(toWhitelistUser, role).queue();
            }

            HashMap<String, String> replace = new HashMap<>();
            replace.put("{player}", toWhitelistUser.getAsMention());
            event.reply(FamiUtils.replaceAndFormat(Lang.successPlayerWhitelisted, replace))
                    .setEphemeral(true)
                    .queue();

            plugin.getLogger().info(
                    String.format("User %s (%s) confirmed whitelist for target %s",
                            event.getUser().getName(),
                            event.getUser().getId(),
                            toWhitelistUser.getName())
            );
        }

        if (event.getComponentId().contentEquals("cancel-bwl")) {
            event.reply(Lang.cancelledWhitelisting)
                    .setEphemeral(true)
                    .queue();
        }
    }
}
