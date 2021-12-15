package reolina.MineFinancial.main;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import reolina.MineFinancial.AControl.ABank;
import reolina.MineFinancial.AControl.AClan;
import reolina.MineFinancial.AControl.AControl;
import reolina.MineFinancial.AControl.AReminder;
import reolina.MineFinancial.QueryMasterConstructor.QueryMaster;

import java.util.logging.Logger;

public class MineFinancial extends JavaPlugin {
    Logger log = Logger.getLogger("Minecraft");

    public void onEnable(){
        getLogger().info("enabled");
        QueryMaster.QuieryMasterInit();
        AControl controller = AControl.getInstance();

        new PlayerLogEvents(this);
        new InventoryEvents(this);
        getCommand("bank").setExecutor(new CommandBank(this));
        getCommand("player").setExecutor(new CommandPlayer(this));
        getCommand("finrole").setExecutor(new CommandFinrole(this));
        getCommand("clan").setExecutor(new CommandClan(this));
        getCommand("rem").setExecutor(new CommandReminder(this));
        getCommand("pay").setExecutor(new CommandTransaction(this));
        getCommand("market").setExecutor(new CommandMarket(this));
        getCommand("credit").setExecutor(new CommandCredit(this));
    }

    public void onDisable(){
        for (Player p : Bukkit.getOnlinePlayers()){
            AReminder.SaveRemindersForPlayer(p.getName());
        }
        ABank.getInstance().saveBankInv();
        QueryMaster.DeactivateConnection();
        getLogger().info("disabled");
    }
}
