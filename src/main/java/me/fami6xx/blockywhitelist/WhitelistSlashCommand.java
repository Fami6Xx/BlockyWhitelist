package me.fami6xx.blockywhitelist;

import net.essentialsx.api.v2.services.discord.*;

import java.util.ArrayList;
import java.util.List;

public class WhitelistSlashCommand implements InteractionCommand {
    @Override
    public boolean isDisabled() {
        return false;
    }

    @Override
    public boolean isEphemeral() {
        return false;
    }

    @Override
    public String getName() {
        return "whitelist";
    }

    @Override
    public String getDescription() {
        return "Přidá whitelist roli hráči";
    }

    @Override
    public List<InteractionCommandArgument> getArguments() {
        return List.of(
                new InteractionCommandArgument(
                        "user",
                        "Člen, kterému chcete přidat whitelist roli",
                        InteractionCommandArgumentType.USER,
                        true)
        );
    }

    @Override
    public void onCommand(InteractionEvent event) {
        DiscordService service = BlockyWhitelist.getDiscordService();

        boolean containsRole = false;
        for (String roleId : BlockyWhitelist.getInstance().getJsonStore().allowedRoles) {
            InteractionRole role = service.getRole(roleId);
            if(role == null) {
                event.reply("Nastala chyba při hledání whitelist role. Prosím kontaktujte administrátora.");
                return;
            }

            if (event.getMember().hasRole(role)) {
                containsRole = true;
            }
        }
        if (!containsRole) {
            event.reply("Nemáte oprávnění použít tento příkaz.");
            return;
        }

        InteractionMember member = event.getUserArgument("user");
        if (member == null) {
            event.reply("Nastala chyba při hledání člena. Zkontrolujte zda jste správně označili člena.");
            return;
        }
        List<InteractionRole> roles = new ArrayList<>();
        for (String roleId : BlockyWhitelist.getInstance().getJsonStore().addedRoles) {
            InteractionRole role = service.getRole(roleId);
            if(role != null) {
                roles.add(role);
            }
        }
        service.modifyMemberRoles(member, roles, null);
        event.reply("Whitelist role byla úspěšně přidána!");

    }
}
