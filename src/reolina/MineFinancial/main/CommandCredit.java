package reolina.MineFinancial.main;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import reolina.MineFinancial.AControl.*;
import reolina.MineFinancial.definition.CRole;

import java.math.BigDecimal;
import java.text.ParseException;

public class CommandCredit  implements CommandExecutor {
    private MineFinancial plugin;
    public CommandCredit(MineFinancial plugin)
    {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (args.length == 0)
            return false;

        if (args[0].equalsIgnoreCase("list")){
            ACredit.GetAllAvailableCredits((Player)sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("given")) { //выдаёт все предоставленные игроком (кланом) кредиты
            ACredit.GetBorrowedCreditsGivenBy(APlayer.list.get(sender.getName()), (Player) sender);
            ACredit.GetAvailableCreditsGivenBy(APlayer.list.get(sender.getName()), (Player) sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("by")) { //выписывает кредиты определённого игрока (свободные)
            sender.sendMessage(ChatColor.RED+"Not implemented yet");
            return true;
        }

        if (args[0].equalsIgnoreCase("taken")){ //выдаёт все взятые игроком кредиты
            ACredit.GetCreditsTakenBy(APlayer.list.get(sender.getName()));
            return true;
        }

        if (args[0].equalsIgnoreCase("create")){
            IBalance temp;
            int index = 1;
            if (args[1].equalsIgnoreCase("asclan")){
                if (AClan.clans.get(APlayer.list.get(sender.getName()).getMemberOfClan()).membersRole.get(sender.getName()) != CRole.leader){
                    sender.sendMessage(ChatColor.RED+"У вас недостаточно прав для этого");
                    return true;
                }
                temp = AClan.clans.get(APlayer.list.get(sender.getName()).getMemberOfClan());
                index = 3;
            } else {
                temp = APlayer.list.get(sender.getName());
            }
            BigDecimal amount = new BigDecimal(args[index++]);
            BigDecimal rate; int days;
            try { rate = new BigDecimal(args[index++]); } catch (Exception e) { new ACredit(temp, amount, new BigDecimal("1.0"), -1); return true;}
            try { days = Integer.parseInt(args[index]); } catch (Exception e) { new ACredit(temp, amount, rate, -1); return true; }
            new ACredit(temp, amount, rate, days);
            return true;
        }

        if (args[0].equalsIgnoreCase("take")){
            BigDecimal amount;
            try { amount = new BigDecimal(args[2]);}catch (Exception ex) {
                ACredit.getAvailableInstanceByID(Integer.parseInt(args[1])).TakeCredit(APlayer.list.get(sender.getName()));
                return true;
            }
            ACredit.getAvailableInstanceByID(Integer.parseInt(args[1])).TakeCredit(APlayer.list.get(sender.getName()), amount);
            return true;
        }

        if (args[0].equalsIgnoreCase("pay")){
            try {
                ACredit.getTakenInstanceByID(Integer.parseInt(args[1])).Pay(new BigDecimal(args[2]));
            } catch (IndexOutOfBoundsException ex){
                sender.sendMessage(ChatColor.RED+"Не хватает аргументов");
                return false;
            } catch (NumberFormatException ex){
                sender.sendMessage(ChatColor.RED+"Ошибка распознавания цифр");
                return false;
            }
            return true;
        }
        return false;
    }
}
