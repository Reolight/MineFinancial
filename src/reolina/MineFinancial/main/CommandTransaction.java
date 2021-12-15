package reolina.MineFinancial.main;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import reolina.MineFinancial.AControl.*;

import java.math.BigDecimal;
import java.util.Locale;

public class CommandTransaction implements CommandExecutor {

    MineFinancial plugin;

    public CommandTransaction(MineFinancial plug){
        plugin = plug;
    }

    private IBalance getIBalanceInstance(String inst){
        IBalance iBalance;
        switch (inst.charAt(0)){
            case '#':
                return AClan.clans.get(inst.substring(1));
            case '@':
                return APlayer.list.get(inst.substring(1));
            case '$':
                return ABank.getInstance();
        }
        return null;
    }

    private boolean isSenderAcceptable(String sender, String CommExec){ //проверка на отправителя: 1. Отправитель может быть только тем, кто исп-ет команду
                                    //2. отправителем может быть только лидер клана и назначенные экономисты(?)
        if (sender.startsWith("$"))
            return false;
        if (sender.charAt(0) == '@'){
            if (CommExec.equalsIgnoreCase(sender.substring(1)))
                return true;
            else return false;
        }
        if (sender.charAt(0) == '#'){
            AClan acl = AClan.clans.get(AClan.SearchPlayer(CommExec));
            if (acl.Name.equalsIgnoreCase(sender.substring(1)) &&
                    acl.membersRole.get(CommExec).ordinal() >= 1)
                return true;
            else return false;
        }
        return false;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        IBalance inst1 = APlayer.list.get(sender.getName());
        IBalance inst2 = null;
        BigDecimal amount;
        int str = 0;

        if (args[0].startsWith("#") &&(args[1].startsWith("@") || args[1].startsWith("@") || //если оба аргумента начинаются как ссылки на игрока или клан
               args[1].startsWith("$"))){
            if (!isSenderAcceptable(args[str], sender.getName())){
                sender.sendMessage(ChatColor.RED + "Отправитель не может быть "+ (args[str].startsWith("$") ? ChatColor.GOLD+" банком" : args[str]));
                return false;
            }
            inst1 = getIBalanceInstance(args[str]);
            if (inst1 == null) {
                sender.sendMessage(ChatColor.RED+"Неизвестный отправитель: "+ChatColor.DARK_RED+args[str]);
                return true;
            }
            str++;
        }

        ///if (args.length == 2){ // на случай двух аргументов
        inst2 = getIBalanceInstance(args[str]);
        if (inst2 == null) {
            sender.sendMessage(ChatColor.RED+"Неизвестный адрес отправки: "+ChatColor.DARK_RED+args[0]);
            return true;
        }
        str++;

        if (inst1 == inst2) {  //защита от "дураков": транзакция самому себе или из своего клана в свой же имеет смысла
            sender.sendMessage(ChatColor.RED+"Невозможно провести транзакцию на подобных условиях");
            return true;
        }

        try{
            amount = new BigDecimal(args[str]);
            if (amount.compareTo(new BigDecimal(0)) < 1){
                sender.sendMessage(ChatColor.RED+"Сумма платежа не может быть отрицательной или равной нулю");
                return true;
            }
        } catch (Exception ex){
            sender.sendMessage(ChatColor.RED+"Проверьте введённую сумму");
            return true;
        }
        //}
        new ATransaction(inst1, inst2, amount);
        return true;
    }
}
