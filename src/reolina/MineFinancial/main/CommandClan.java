package reolina.MineFinancial.main;

import org.bukkit.Bukkit;
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
        if (args[0] == null) return false;
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
                case "удалить":
                case "remove":
                    if (acl == null) {sender.sendMessage(ChatColor.RED+"Вы не состоите в клане"); return true;}
                    int remR = AClan.RemoveClan(sender.getName(), acl);
                    if (remR == 401){
                        sender.sendMessage("У вас недостаточно прав для этого");
                        return true;
                    }
                    return true;
                case "лидер":
                case "leader":
                    if (acl == null) {sender.sendMessage(ChatColor.RED+"Вы не состоите в клане. Уточните клан для этой команды"); return true;}
                    sender.sendMessage(ChatColor.YELLOW+"Лидер клана "+ChatColor.LIGHT_PURPLE+acl.Name+" — "+ChatColor.BLUE+acl.clanLeader);
                    return true;
                case "участников":
                case "members":
                    if (acl != null) {
                        sender.sendMessage(ChatColor.YELLOW+"Участники клана "+ChatColor.LIGHT_PURPLE+acl.Name+
                                ": "+acl.GetMembersList());
                    } else {sender.sendMessage(ChatColor.RED+"Вы не состоите в клане"); }
                    return true;
            }
        }

        if (args.length >= 2){
            switch (args[0]) {
                case "присоединиться":
                case "join":
                    if (acl != null) {
                        sender.sendMessage(ChatColor.RED + "Вы состоите в клане " + ChatColor.LIGHT_PURPLE + acl.Name +
                                ChatColor.RED + ". Для присоединения к новому клану покиньте текущий.");
                        return true;
                    }
                    //Здесь должен создаться запрос, но пока принимаем автоматом
                    AClan applying = AClan.clans.get(args[1]);
                    if (applying != null) {
                        applying.AddMember(sender.getName());
                        sender.sendMessage(ChatColor.GREEN + "Вы присоединились к клану " + ChatColor.LIGHT_PURPLE + applying.Name);
                        applying.SendClanMessageExept(ChatColor.YELLOW+"Игрок " + ChatColor.AQUA + sender.getName() +
                                        ChatColor.YELLOW+" присоединился к клану", new String[]{sender.getName()});
                    }
                    return true;
                case "создать":
                case "create":
                    int resc = AClan.CreateClan(args[1], sender.getName());
                    if (resc == 0)
                        sender.sendMessage(ChatColor.YELLOW + "Клан " + ChatColor.LIGHT_PURPLE + args[1] + ChatColor.YELLOW + " создан!");
                    if (resc == 102)
                        sender.sendMessage(ChatColor.RED + "Клан " + ChatColor.LIGHT_PURPLE + args[1] + ChatColor.RED + " уже существует");
                    if (resc == 403) sender.sendMessage(ChatColor.RED + "Вы уже состоите в другом клане ("
                            + ChatColor.LIGHT_PURPLE + AClan.SearchPlayer(sender.getName()) + ChatColor.RED + ")");
                    return true;
                case "переименовать":
                case "rename":
                    if (acl == null || cr.ordinal() < 2) {
                        sender.sendMessage(ChatColor.RED + "Для выполнения этой команды ва должны быть лидером клана");
                        return true;
                    }
                    if (acl.Name == args[1]){ sender.sendMessage(ChatColor.GRAY+"Для переименования клана введите новое название");
                        return true;}
                    String oldname = acl.Name;
                    int renRes = AClan.RenameClan(acl.Name, args[1]);
                    if (renRes == 0) acl.SendClanMessage(ChatColor.YELLOW+"клан "+ChatColor.DARK_PURPLE+oldname+
                            ChatColor.YELLOW+" переименован в "+ChatColor.LIGHT_PURPLE+acl.Name);
                    if (renRes == 102) {sender.sendMessage(ChatColor.RED+"Имя "+ChatColor.DARK_PURPLE+args[1]+" уже занято"); return true;}
                    return true;
                case "лидер":
                case "leader":
                    AClan strClan = AClan.clans.get(args[1]);
                    if (strClan == null) {sender.sendMessage(ChatColor.RED+"Такого клана не существует"); return true;}
                    sender.sendMessage(ChatColor.YELLOW+"Лидер клана "+ChatColor.LIGHT_PURPLE+strClan.Name+" — "+ChatColor.BLUE+strClan.clanLeader);
                    return true;

                case "исключить":
                case "expel":
                    if (acl != null){
                        if (cr == CRole.leader){
                            int rempl = acl.RemovePlayer(args[1]);
                            if (rempl == 0) {
                                acl.SendClanMessage(ChatColor.YELLOW+"Игрок "+ ChatColor.AQUA+args[1]+
                                        ChatColor.YELLOW+" исключён из клана "+ChatColor.LIGHT_PURPLE+acl.Name);
                                Bukkit.getPlayer(args[1]).sendMessage(ChatColor.RED+"Вы были исключены из клана "+ChatColor.LIGHT_PURPLE+acl.Name);
                                return true;
                            }
                            if (rempl == 401) {
                                sender.sendMessage(ChatColor.RED+"Нельзя исключить лидера клана");
                                return true;
                            }
                            if (rempl == 402) {
                                sender.sendMessage(ChatColor.RED+"Такого игрока в клане нет");
                                return true;
                            }
                        } else {sender.sendMessage(ChatColor.RED+"Исключать игроков может только лидер"); return true;}
                    } else {sender.sendMessage(ChatColor.RED + "Вы не состоите в клане"); return true;}

                case "m":
                case "с": {
                        if (acl == null) {sender.sendMessage(ChatColor.RED + "Вы не состоите в клане"); return true;}
                        StringBuilder s = new StringBuilder("["+ChatColor.LIGHT_PURPLE+acl.Name+ChatColor.WHITE+">" +
                                ChatColor.AQUA+sender.getName()+ChatColor.WHITE+"]:"+ChatColor.ITALIC);
                        for (int i = 1; i < args.length; i++){
                            s.append(" "+args[i]);
                        }
                        acl.SendClanMessage(s.toString());
                        return true;
                    }
            }
        }
        return false;
    }
}