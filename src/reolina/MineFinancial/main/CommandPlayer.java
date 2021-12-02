package reolina.MineFinancial.main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.jline.internal.Log;
import org.bukkit.entity.Player;
import reolina.MineFinancial.AControl.APlayer;

public class CommandPlayer implements CommandExecutor {

    private MineFinancial plugin;
    public CommandPlayer(MineFinancial plugin)
    {
        this.plugin = plugin;
    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    //label - command name
    //String args - least;
    {
        ChatColor[] chc = {ChatColor.WHITE, ChatColor.DARK_GREEN, ChatColor.LIGHT_PURPLE, ChatColor.RED};
        String[] finr = {"простой человек","торговец","модератор","администратор"};
        String senderName = sender.getName();
        boolean isPlayer = sender instanceof Player;
        APlayer apl = APlayer.list.get(senderName);
            switch (label) {
                case "игрок":
                    if (!isPlayer) {
                        sender.sendMessage(ChatColor.RED + "Эта команда только для "+ChatColor.AQUA+"игроков");
                        return false;
                    }
                case "player":
                    if (!isPlayer) {
                        sender.sendMessage(ChatColor.RED + "This command is only for "+ChatColor.AQUA+"players");
                        return false;
                    }
                    switch (args[0]) {
                        case "баланс":
                            sender.sendMessage(ChatColor.GREEN + "У вас на балансе " + ChatColor.AQUA + apl.GetBalanceString());
                            return true;
                        case "финроль":
                            sender.sendMessage(ChatColor.GREEN + "Вы " + chc[apl.frole.ordinal()] + finr[apl.frole.ordinal()]);
                            return true;
                        case "balance":
                            sender.sendMessage(ChatColor.GREEN + "You have " + ChatColor.AQUA + apl.GetBalanceString());
                            return true;
                        case "finrole":
                            sender.sendMessage(ChatColor.GREEN + "You are " + chc[apl.frole.ordinal()] + apl.frole);
                            return true;
                        default:
                            sender.sendMessage(ChatColor.RED + "Unknown command: " + args[0]);
                            return false;
                    }
            }
        return false;
    }
}
