package reolina.MineFinancial.main;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import reolina.MineFinancial.AControl.AClan;
import reolina.MineFinancial.AControl.APlayer;
import reolina.MineFinancial.AControl.AReminder;

import java.util.logging.Logger;

public class PlayerLogEvents implements Listener {
    Logger log = Logger.getLogger("Minecraft");
    private MineFinancial plugin;
    public PlayerLogEvents(MineFinancial plugin)
    {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

    }

    @EventHandler public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        event.setJoinMessage("");
        if (APlayer.list.containsKey(player.getName())) {
            player.sendMessage(ChatColor.GREEN+"Здравствуй, "+ChatColor.AQUA+player.getName()+"!");
            if (APlayer.list.get(player.getName()).getMemberOfClan() != null && !APlayer.list.get(player.getName()).getMemberOfClan().equalsIgnoreCase(""))
                AClan.clans.get(APlayer.list.get(player.getName()).getMemberOfClan()).SendClanNotificationExcept(
                        ChatColor.DARK_AQUA+""+ChatColor.ITALIC+player.getName()+ChatColor.GRAY+" зашёл на сервер",
                        new String[]{player.getName()});
            AReminder.RemindCount(player.getName());
        } else {
            APlayer.AddPlayer(player.getName());
            player.sendMessage(ChatColor.GREEN+"Добро пожаловать, "+ChatColor.AQUA+player.getName()+ChatColor.GREEN+"!");
        }
    }
    @EventHandler public void onPlayerQuit(PlayerQuitEvent event){
        event.setQuitMessage("");
        Player player = event.getPlayer();
        if (APlayer.list.get(player.getName()).getMemberOfClan() != null && !APlayer.list.get(player.getName()).getMemberOfClan().equalsIgnoreCase(""))
            AClan.clans.get(APlayer.list.get(player.getName()).getMemberOfClan()).SendClanNotificationExcept(
                    ChatColor.DARK_AQUA+""+ChatColor.ITALIC+player.getName()+ChatColor.GRAY+" вышел с сервера",
                    new String[]{player.getName()});
        AReminder.SaveRemindersForPlayer(event.getPlayer().getName());
    }
}