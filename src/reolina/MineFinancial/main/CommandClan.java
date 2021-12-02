package reolina.MineFinancial.main;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import reolina.MineFinancial.AControl.AClan;
import reolina.MineFinancial.AControl.APlayer;
import reolina.MineFinancial.definition.CRole;

import java.util.ArrayList;

public class CommandClan implements CommandExecutor {
    private MineFinancial plugin;
    public CommandClan(MineFinancial plugin)
    {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (args == null) return false;
        AClan acl = AClan.clans.get(AClan.SearchPlayer(sender.getName()));
        CRole cr = acl != null ? acl.membersRole.get(sender.getName()) : null;
        if (args.length == 1){
            switch (args[0]){
                case "присоединиться":
                case "join":
                case "список":
                case "list":
                    sender.sendMessage(ChatColor.GRAY+AClan.GetClanList());
                    return true;
                case "покинуть":
                case "leave":
                    if (acl == null) {
                        sender.sendMessage(ChatColor.RED + "Вы не состоите в клане");
                        return true;
                    }
                    int rem = acl.RemovePlayer(sender.getName());
                    if (rem == 0){
                        acl.SendClanMessage(ChatColor.AQUA + sender.getName()+ChatColor.YELLOW+" покидает "+ChatColor.LIGHT_PURPLE);
                        sender.sendMessage(ChatColor.GREEN+"Вы покидаете клан "+ChatColor.LIGHT_PURPLE);
                     } else if (rem == 401) sender.sendMessage(ChatColor.RED + "Вы не можете покинуть клан "+
                            ChatColor.LIGHT_PURPLE+acl.Name+ChatColor.RED+" поскольку вы его лидер" );
                    return true;
                case "баланс":
                case "balance":
                    if (acl == null){
                        sender.sendMessage(ChatColor.RED + "Вы не состоите в клане");
                        return true;
                    }
                    if (cr.ordinal()<1)
                        sender.sendMessage("У вас недостаточно прав для этого");
                    else sender.sendMessage(ChatColor.YELLOW+"Баланс клана "+ChatColor.LIGHT_PURPLE+acl.Name+ChatColor.YELLOW+
                            " составляет "+ChatColor.AQUA+acl.GetBalanceString());
                    return true;

            }
        }
        if (args[0] == "message" || args[0] == "сообщение" || args[0] == "m" || args[0] == "с"){
            if (acl == null) {sender.sendMessage(ChatColor.RED + "Вы не состоите в клане"); return true;}
            StringBuilder s = new StringBuilder("["+ChatColor.LIGHT_PURPLE+acl.Name+ChatColor.WHITE+">" +
                    ChatColor.AQUA+sender.getName()+ChatColor.WHITE+"]:"+ChatColor.ITALIC);
            for (int i = 1; i < args.length-1; i++){
                s.append(" "+args[i]);
            }
            acl.SendClanMessage(s.toString());
        }

        if (args.length == 2){
            switch (args[0]){
                case "присоединиться":
                case "join":
                    if (acl != null) {
                        sender.sendMessage(ChatColor.RED + "Вы состоите в клане "+ChatColor.LIGHT_PURPLE+acl.Name+
                                ChatColor.RED+". Для присоединения к новому клану покиньте текущий.");
                        return true;
                    }
                    //Здесь должен создаться запрос, но пока принимаем автоматом
                    AClan applying = AClan.clans.get(args[1]);
                    if (applying != null){
                        applying.AddMember(sender.getName());
                        sender.sendMessage(ChatColor.GREEN+"Вы присоединились к клану "+ChatColor.LIGHT_PURPLE+ applying.Name);
                        applying.SendClanMessageExept("Игрок "+ChatColor.AQUA+" присоединился к клану",
                                new String[]{sender.getName()});
                    }
                case "создать":
                case "create":
                    int resc = AClan.CreateClan(args[1], sender.getName());
                    if (resc == 0) sender.sendMessage(ChatColor.YELLOW+"Клан "+ChatColor.LIGHT_PURPLE+args[1]+ChatColor.YELLOW+" создан!");
                    if (resc == 102) sender.sendMessage(ChatColor.RED+"Клан "+ChatColor.LIGHT_PURPLE+args[1]+ChatColor.RED+" уже существует");
                    if (resc == 403) sender.sendMessage(ChatColor.RED+"Вы уже состоите в другом клане ("
                            +ChatColor.LIGHT_PURPLE+ AClan.SearchPlayer(sender.getName())+ChatColor.RED+")");
                    return true;
            }
        }
        return false;
    }
}