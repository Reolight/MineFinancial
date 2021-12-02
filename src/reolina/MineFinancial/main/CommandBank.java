package reolina.MineFinancial.main;

import net.minecraft.server.commands.CommandList;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import reolina.MineFinancial.AControl.ABank;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Locale;

public class CommandBank implements CommandExecutor {

    private MineFinancial plugin;
    public CommandBank(MineFinancial plugin)
    {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
                //label - command name
                //String args - least;
    {
        ABank Bank = ABank.getInstance();
        switch (args[0].toLowerCase(Locale.ROOT)){
            case "баланс":
                sender.sendMessage(ChatColor.YELLOW+"Текущий баланс банка: "+ChatColor.AQUA+"¥"+Bank.GetBalance());
                return true;
            case "balance":
                sender.sendMessage(ChatColor.YELLOW+"Current bank balance: "+ChatColor.AQUA+"¥"+Bank.GetBalance());
                return true;

            case "эмиссия":
            case "emission":
                try {
                    if (Bank.MoneyEmission(new BigDecimal(args[1]))) {
                        sender.sendMessage();
                    };
                }
                catch (SQLException ex) {sender.sendMessage(ChatColor.RED+ex.toString()); }
                return true;
            default:
                sender.sendMessage("Unknown command "+args[0]);
                return false;
        }
    }
}
