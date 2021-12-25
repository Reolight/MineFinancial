package reolina.MineFinancial.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import reolina.MineFinancial.AControl.AMarket.AMSeller;
import reolina.MineFinancial.AControl.AMarket.AMarket;
import reolina.MineFinancial.main.MineFinancial;

public class CommandMarket implements CommandExecutor {
    MineFinancial plugin;
    public CommandMarket(MineFinancial plugin) { this.plugin = plugin; }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (args.length == 0) {
            AMarket.getInstance().Show((Player) sender);
            return true;
        }
        if (args[0].equalsIgnoreCase("sell")){
            AMarket.amSellers.put((Player) sender, new AMSeller((Player) sender));
            return true;
        }
        return true;
    }
}
