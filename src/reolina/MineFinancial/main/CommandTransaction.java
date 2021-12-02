package reolina.MineFinancial.main;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandTransaction implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        sender.sendMessage(ChatColor.ITALIC+""+ChatColor.RED+"Not implemented yet");
        return true;
    }
}
