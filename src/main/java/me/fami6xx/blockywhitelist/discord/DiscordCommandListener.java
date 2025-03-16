package me.fami6xx.blockywhitelist.discord;

import me.fami6xx.blockywhitelist.BlockyWhitelist;
import me.fami6xx.blockywhitelist.JSONStore;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;
import java.util.UUID;

public class DiscordCommandListener extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String command = event.getName();

        if ("whitelist".equalsIgnoreCase(command)) {
            if (event.getMember() == null) {
                event.reply("Tento příkaz může použít jen člen Discord serveru.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            if (event.getGuild() == null) {
                event.reply("Tento příkaz může být použit jen na Discord serveru.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            BlockyWhitelist plugin = BlockyWhitelist.getInstance();
            JSONStore jsonStore = plugin.getJsonStore();

            if (!event.getGuild().getId().equals(jsonStore.guildId)) {
                event.reply("Tento příkaz může být použit jen na Discord serveru.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            boolean hasPermission = event.getMember().getRoles().stream()
                    .anyMatch(role -> jsonStore.allowedRoles.contains(role.getId()));

            if (!hasPermission) {
                event.reply("Nemáš oprávnění k použití tohoto příkazu!")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            if (event.getOption("user") == null) {
                event.reply("Nebyl poskytnut žádný uživatel.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            User toWhitelistUser = event.getOption("user").getAsUser();

            List<Role> toAdd = event.getGuild().getRoles().stream()
                    .filter(role -> jsonStore.addedRoles.contains(role.getId()))
                    .toList();

            if (toAdd.isEmpty()) {
                event.reply("Není nastavena žádná role pro whitelistnutí.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            List<Member> usersWithRoles = event.getGuild().getMembersWithRoles(toAdd);
            if (usersWithRoles.stream().anyMatch(member -> member.getId().equals(toWhitelistUser.getId()))) {
                event.reply("Hráč " + toWhitelistUser.getAsMention() + " již je whitelistnut!")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            toAdd.forEach(role -> {
                event.getGuild().addRoleToMember(toWhitelistUser, role).queue();
            });

            event.reply("Hráč " + toWhitelistUser.getAsMention() + " byl úspěšně whitelistnut!")
                    .queue();
        }

        if ("link".equalsIgnoreCase(command)) {
            if (event.getMember() == null) {
                event.reply("Tento příkaz může použít jen člen Discord serveru.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            if (event.getGuild() == null) {
                event.reply("Tento příkaz může být použit jen na Discord serveru.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            BlockyWhitelist plugin = BlockyWhitelist.getInstance();
            JSONStore jsonStore = plugin.getJsonStore();

            if (!event.getGuild().getId().equals(jsonStore.guildId)) {
                event.reply("Tento příkaz může být použit jen na Discord serveru.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            if (event.getOption("code") == null) {
                event.reply("Nebyl poskytnut žádný kód.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            if (jsonStore.linkedPlayers.containsValue(event.getMember().getId())) {
                event.reply("Již máš propojený účet!")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            String code = event.getOption("code").getAsString();

            if (!jsonStore.pendingPlayers.containsKey(code)) {
                event.reply("Neplatný kód.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            UUID toLinkUser = jsonStore.pendingPlayers.get(code);

            jsonStore.pendingPlayers.remove(code);
            jsonStore.linkedPlayers.put(toLinkUser, event.getMember().getId());
            jsonStore.save();

            event.reply("Tvůj účet byl úspěšně propojen!")
                    .setEphemeral(true)
                    .queue();
        }

        if ("failed".equalsIgnoreCase(command)) {
            if (event.getMember() == null) {
                event.reply("Tento příkaz může použít jen člen Discord serveru.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            if (event.getGuild() == null) {
                event.reply("Tento příkaz může být použit jen na Discord serveru.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            BlockyWhitelist plugin = BlockyWhitelist.getInstance();
            JSONStore jsonStore = plugin.getJsonStore();

            if (!event.getGuild().getId().equals(jsonStore.guildId)) {
                event.reply("Tento příkaz může být použit jen na Discord serveru.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            if (event.getOption("attempt") == null) {
                event.reply("Nebylo poskytnuto číslo pokusu.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            if (event.getOption("user") == null) {
                event.reply("Nebyl poskytnut žádný uživatel.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            int attempt = event.getOption("attempt").getAsInt();
            User toFailUser = event.getOption("user").getAsUser();

            if (attempt < 1 || attempt > 3) {
                event.reply("Neplatné číslo pokusu.")
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
                event.reply("Nebyla nastavena role pro tento pokus.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            Role role = event.getGuild().getRoleById(roleId);
            if (role == null) {
                event.reply("Role pro tento pokus nebyla nalezena.")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            if (event.getGuild().getMembersWithRoles(role).stream().anyMatch(member -> member.getId().equals(toFailUser.getId()))) {
                event.reply("Hráč " + toFailUser.getAsMention() + " již má tuto roli!")
                        .setEphemeral(true)
                        .queue();
                return;
            }

            event.getGuild().addRoleToMember(toFailUser, role).queue();
            event.reply("Hráči " + toFailUser.getAsMention() + " byla přidána role za pokus " + attempt + "!")
                    .queue();
        }
    }
}
