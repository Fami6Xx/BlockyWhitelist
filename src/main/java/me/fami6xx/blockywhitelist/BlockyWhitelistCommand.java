package me.fami6xx.blockywhitelist;

import me.fami6xx.blockywhitelist.menus.ChooseNextStepMenu;
import me.fami6xx.blockywhitelist.utils.FamiUtils;
import me.fami6xx.blockywhitelist.utils.languages.Lang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BlockyWhitelistCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)){
            sender.sendMessage(FamiUtils.format("&c&lERROR! &7" + Lang.errorOnlyPlayersCanUseThisCommandMessage));
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("blockywhitelist.use")){
            player.sendMessage(FamiUtils.format("&c&lERROR! &7" + Lang.errorYouDontHavePermissionToUseThisCommandMessage));
            return true;
        }

        new ChooseNextStepMenu(BlockyWhitelist.getInstance().getMenuManager().getPlayerMenu(player)).open();
        return true;
    }
}
