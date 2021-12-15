package reolina.MineFinancial.main;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import reolina.MineFinancial.AControl.*;

import java.math.BigDecimal;
import java.util.logging.Logger;

public class InventoryEvents implements Listener {
    MineFinancial plugin;
    Logger log = Logger.getLogger("Minecraft");
    public InventoryEvents(MineFinancial plugin)
    {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

    }

    @EventHandler public boolean onInventoryClickEvent (InventoryClickEvent event){
        //if (event.getView().getTitle().contains("Продажа")) {
        //}
        if (event.getView().getTitle().contains("Продать"))
            AMarket.amSellers.get((Player) event.getWhoClicked()).SellerInterfaceEventManager(event);
        if (event.getView().getTitle().contains("Рынок")) {
            AMarket.getInstance().MarketInventoryManager(event);
        }
        return true;
    }

    @EventHandler public boolean onInventoryClose(InventoryCloseEvent event){
        if (event.getView().getTitle().contains("Продажа")) {
            BigDecimal price = new BigDecimal(0);
            for (ItemStack itma : event.getInventory().getContents()){
                if (itma == null) continue;
                price = price.add(new BigDecimal(itma.getAmount()));
            }
            if (price.compareTo(BigDecimal.ZERO) == 0){
                return true;} //прерываем код, чтобы игроку не перечислялось НИСКОЛЬКО и в банк не передавалось НИЧЕГО
            new ATransaction((IBalance) ABank.getInstance(), (IBalance) APlayer.list.get(event.getPlayer().getName()), price);
            ABank.getInstance().getItems(event.getInventory().getContents());
        }
        if (event.getView().getTitle().contains("Продать")) {
            AMarket.amSellers.get((Player) event.getPlayer()).Remove(event); //это только для удаления из массива экранов продаж
        }
        if (event.getView().getTitle().contains("Почта")) {
            APlayer.list.get(event.getPlayer()).onInventoryClose(event);
        }
        return false;
    }
}
