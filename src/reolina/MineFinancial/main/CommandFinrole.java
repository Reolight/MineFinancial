package reolina.MineFinancial.main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.jline.internal.Log;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import reolina.MineFinancial.AControl.APlayer;

public class CommandFinrole implements CommandExecutor {

    private MineFinancial plugin;
    public CommandFinrole(MineFinancial plugin)
    {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        ChatColor[] chc = {ChatColor.WHITE, ChatColor.DARK_GREEN, ChatColor.LIGHT_PURPLE, ChatColor.RED};
        String[] finr = {"простой человек","торговец","модератор","администратор"};
        String senderName = sender.getName();
        //boolean isPlayer = sender instanceof CraftPlayer;
        APlayer apl = APlayer.list.get(args[1]);
        APlayer sen = APlayer.list.get(senderName);
        //if (isPlayer)
        //    sender.sendMessage(ChatColor.RED + "Эта команда только для " + ChatColor.DARK_PURPLE + "Сервера ("+sender+")");
        if (sen.frole.ordinal() < 2){
            sender.sendMessage(ChatColor.RED + "Только " + chc[2] + finr[2] + ChatColor.RED + " и выше может выполнить эту команду");
            return true;
        }

        switch (label) {
            case "финроль":
                switch (args[0]) {
                    case "повысить":
                        if (APlayer.PromoteFRole(args[1])) {
                            sender.sendMessage(ChatColor.AQUA + senderName + ChatColor.YELLOW + " был повышен до " +
                                    chc[apl.frole.ordinal()] + apl.frole.toString());
                            Bukkit.getPlayer(args[1]).sendMessage(ChatColor.DARK_BLUE + "Вы были повышены до " +
                                    chc[apl.frole.ordinal()] + apl.frole.toString());
                        } else
                            sender.sendMessage(ChatColor.AQUA + args[1] + ChatColor.RED + " не был повышен");
                        return true;
                    case "понизить":
                        if (APlayer.DemoteFRole(args[1])) {
                            sender.sendMessage(ChatColor.AQUA + senderName + ChatColor.RED + " был понижен до " +
                                    chc[apl.frole.ordinal()] + apl.frole.toString());
                            Bukkit.getPlayer(args[1]).sendMessage(ChatColor.DARK_RED + "Вы были понижены до " +
                                    chc[apl.frole.ordinal()] + apl.frole.toString());
                        } else
                            sender.sendMessage(ChatColor.AQUA + args[1] + ChatColor.RED + " не был понижен");
                        return true;
                }
                break;
            case "finrole":
                //if (!isPlayer) {
                //    sender.sendMessage(ChatColor.RED + "This command is only for " + ChatColor.DARK_PURPLE + "Server");
                //    return false;
                //}
                switch (args[0]) {
                    case "promote":
                        if (APlayer.PromoteFRole(args[1])) {
                            sender.sendMessage(ChatColor.AQUA + args[1] + ChatColor.YELLOW + " have been prompted to " +
                                    chc[apl.frole.ordinal()] + apl.frole.toString());
                            Bukkit.getPlayer(args[1]).sendMessage(ChatColor.DARK_BLUE + "You have been prompted to " +
                                    chc[apl.frole.ordinal()] + apl.frole.toString());
                        } else
                            sender.sendMessage(ChatColor.AQUA + args[1] + ChatColor.RED + " was not promoted");
                        return true;
                    case "demote":
                        if (APlayer.DemoteFRole(args[1])) {
                            sender.sendMessage(ChatColor.AQUA + args[1] + ChatColor.RED + " have been demoted to " +
                                    chc[apl.frole.ordinal()] + apl.frole.toString());
                            Bukkit.getPlayer(args[1]).sendMessage(ChatColor.RED + "You have been demoted to " +
                                    chc[apl.frole.ordinal()] + apl.frole.toString());
                        } else
                            sender.sendMessage(ChatColor.AQUA + senderName + ChatColor.RED + " was not demoted");
                        return true;
                }
        }
        return false;
    }

}
