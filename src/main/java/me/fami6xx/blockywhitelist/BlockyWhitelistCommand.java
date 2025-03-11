package me.fami6xx.blockywhitelist;

import me.fami6xx.rpuniverse.RPUniverse;
import me.fami6xx.rpuniverse.core.misc.utils.FamiUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BlockyWhitelistCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)){
            sender.sendMessage(FamiUtils.format("&c&lERROR! &7" + RPUniverse.getLanguageHandler().errorOnlyPlayersCanUseThisCommandMessage));
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("blockywhitelist.use")){
            player.sendMessage(FamiUtils.format("&c&lERROR! &7" + RPUniverse.getLanguageHandler().errorYouDontHavePermissionToUseThisCommandMessage));
            return true;
        }
        return true;
    }
}
