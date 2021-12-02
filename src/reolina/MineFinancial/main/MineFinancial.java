package reolina.MineFinancial.main;

import org.bukkit.plugin.java.JavaPlugin;
import reolina.MineFinancial.AControl.AControl;
import reolina.MineFinancial.QueryMasterConstructor.QueryMaster;

import java.util.logging.Logger;

public class MineFinancial extends JavaPlugin {
    Logger log = Logger.getLogger("Minecraft");

    public void onEnable(){
        getLogger().info("enabled");
        QueryMaster.QuieryMasterInit();
        AControl controller = AControl.getInstance();

        new PlayerLogEvents(this);
        getCommand("bank").setExecutor(new CommandBank(this));
        getCommand("player").setExecutor(new CommandPlayer(this));
        getCommand("finrole").setExecutor(new CommandFinrole(this));
        getCommand("clan").setExecutor(new CommandClan(this));
    }

    public void onDisable(){
        getLogger().info("disabled");
    }
}
