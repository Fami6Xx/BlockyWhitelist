package me.fami6xx.blockywhitelist;

import net.essentialsx.api.v2.services.discord.InteractionCommand;
import net.essentialsx.api.v2.services.discord.InteractionCommandArgument;
import net.essentialsx.api.v2.services.discord.InteractionCommandArgumentType;
import net.essentialsx.api.v2.services.discord.InteractionEvent;

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

    }
}
