package reolina.MineFinancial.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.jline.internal.Log;
import reolina.MineFinancial.AControl.AReminder;
import reolina.MineFinancial.main.MineFinancial;

public class CommandReminder implements CommandExecutor {

    MineFinancial plugin;
    public CommandReminder(MineFinancial plugin) {this.plugin = plugin; }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        try {String a = args[0];} catch (Exception e){
            AReminder.RemindCount(sender.getName());
            return true;
        }
        if (args.length>=1) {
            if (args[0].equalsIgnoreCase("l")) {
                AReminder.SendAll(sender.getName());
                return true;
            }
            try {
                AReminder.Reply(Integer.parseInt(args[0]), (args[1].startsWith("y") || args[1].startsWith( "у")) ? true : false);
                return  true;
            } catch (NumberFormatException ex) {
                sender.sendMessage(ChatColor.RED + "Это число: " + args[0] + "?");
            } catch (NullPointerException nup) {
                sender.sendMessage(ChatColor.RED + "Введите у, если согласны. Другой ответ будет принят за отказ");
                sender.sendMessage(nup.toString());
                Log.warn(nup+"\n"+nup.getStackTrace());
            }
        }
        return false;
    }
}
