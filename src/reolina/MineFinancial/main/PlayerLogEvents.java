package reolina.MineFinancial.main;


import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import reolina.MineFinancial.AControl.APlayer;

public class PlayerLogEvents implements Listener {

    private MineFinancial plugin;
    public PlayerLogEvents(MineFinancial plugin)
    {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        if (APlayer.list.containsKey(player.getName())) {
            player.sendMessage(ChatColor.GREEN+"Hello, "+ChatColor.AQUA+player.getName()+"!");
            //reminders
        } else {
            APlayer.AddPlayer(player.getName());
            player.sendMessage(ChatColor.GREEN+"Welcome, "+ChatColor.AQUA+player.getName());
        }
    }
}