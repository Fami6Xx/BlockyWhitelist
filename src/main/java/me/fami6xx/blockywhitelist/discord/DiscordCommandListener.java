package me.fami6xx.blockywhitelist.discord;

import me.fami6xx.blockywhitelist.BlockyWhitelist;
import me.fami6xx.blockywhitelist.JSONStore;
import me.fami6xx.blockywhitelist.utils.FamiUtils;
import me.fami6xx.blockywhitelist.utils.languages.Lang;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DiscordCommandListener extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
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

            toAdd.forEach(role -> event.getGuild().addRoleToMember(toWhitelistUser, role).queue());

            HashMap<String, String> replace = new HashMap<>();
            replace.put("{player}", toWhitelistUser.getAsMention());
            event.reply(FamiUtils.replaceAndFormat(Lang.successPlayerWhitelisted, replace))
                    .queue();
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
        }
    }
}
