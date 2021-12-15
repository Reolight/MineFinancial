package reolina.MineFinancial.AControl;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import reolina.MineFinancial.QueryMasterConstructor.*;
import reolina.MineFinancial.definition.AProduct;

import java.nio.Buffer;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public class AMarket {
    static AMarket instance;
    static Inventory market;
    static ArrayList<AProduct> SellList = new ArrayList<>();
    private int id = 0;
    static private final String table = "market";
    static private final String[] columnNames = {"id", "seller", "material", "amount", "meta", "price", "customer"};
    static Logger log = Logger.getLogger("Minecraft");
    static public HashMap<Player, AMSeller> amSellers = new HashMap<>();
    static HashMap<Player, String> filterMap= new HashMap<>();

    static public AMarket getInstance(){
        if (instance == null)
            instance = new AMarket();
        return instance;
    }

    private AMarket(){
        market = Bukkit.createInventory(null, 54, "Рынок");
        try{
            ResultSet rs = QueryMaster.Select(table, null, null);
            while (rs.next()){
                String seller = rs.getString(columnNames[1]);
                AProduct apr = new AProduct(new ItemStack(Material.valueOf(rs.getString(columnNames[2])), rs.getInt(columnNames[3])),
                        seller, rs.getString(columnNames[4]), rs.getBigDecimal(columnNames[5]));
                log.info(apr.getStringSeller()+" product ("+apr.getProductItem().getItemMeta().getDisplayName()+") loaded");
                if (rs.getString(columnNames[6]) == null)
                    SellList.add(apr);
            }
        }catch (Exception ex){
            log.info("err: "+ex+"\n"+ex.getStackTrace());
            QueryMaster.Create(new Table(table, new Field[]{new Field(SQLtype.INTEGER, columnNames[0], true, true, true),
                                                            new Field(SQLtype.TEXT, columnNames[1], true),
                                                            new Field(SQLtype.TEXT, columnNames[2], true),
                                                            new Field(SQLtype.INT, columnNames[3], true),
                                                            new Field(SQLtype.TEXT, columnNames[4], true),
                                                            new Field(SQLtype.REAL, columnNames[5], true),
                                                            new Field(SQLtype.TEXT, columnNames[6])}));
        }
    }

    public boolean addProduct(AProduct newProduct){
        SellList.add(newProduct);
        QueryMaster.Insert(table, new rec[] //добавляем товар в БД
                {new rec(columnNames[1], newProduct.getStringSeller(),true),
                new rec(columnNames[2], newProduct.getItemMaterial().toString(), true),
                new rec(columnNames[3], ""+newProduct.getAmount()),
                new rec(columnNames[4], newProduct.getSerializedOrigMeta(), true),
                new rec(columnNames[5], newProduct.getPrice().toString())}, null);
        //стоит ли обновить ассортимент у игроков, у которых уже открыт рынок?
        return true;
    }

        public void Show(Player p){
            if (!SellList.isEmpty()) {
                AProduct[] apr;
                market.clear();
                if (filterMap.containsKey(p)) {
                    apr = SellList.stream().filter(aProduct -> aProduct.getName().contains(filterMap.get(p)))
                            .toArray(AProduct[]::new);
                    int i = 0;
                    for (AProduct a : apr){
                        market.setItem(i++, a.getProductItem());
                    }
                }else {
                    apr = SellList.toArray(AProduct[]::new);
                    int i = 0;
                    for (AProduct a : apr){
                        market.setItem(i++, a.getProductItem());
                    }
                }
            }
            if (SellList.isEmpty())
                market.clear();
            p.openInventory(market);
        }

        public void Show(Player p, String filter){
            if (!SellList.isEmpty()) {
                filterMap.put(p, filter);
            }
            Show(p);
        }

        public boolean MarketInventoryManager(InventoryClickEvent event) {
            if (event.getSlotType() == InventoryType.SlotType.CONTAINER && event.getSlot() < 54)
                event.setCancelled(true);
            if (event.isLeftClick() && event.getSlot() < 54 && event.getSlot() >= 0 &&  //0..53. нижняя граница стоит, потому что событие каким-то образом выдало -999
                    event.getInventory().getItem(event.getSlot()) != null &&
                    event.getSlotType() == InventoryType.SlotType.CONTAINER) {
                AProduct bought = SellList.stream()
                        .filter(aProduct -> aProduct.getID() == AProduct.IDFromItemStack(event.getInventory().getItem(event.getSlot())))
                        .findFirst().get();
                if (bought == null) {
                    log.warning("MarketInventoryManager: bought item is null");
                    return false;
                } else {
                    IBalance customer = APlayer.list.get(event.getWhoClicked().getName());
                    ATransaction NaTr = new ATransaction(customer, bought.seller, bought.getPrice());
                    if (NaTr.isFlyed) {
                        APlayer aPlayer = APlayer.list.get(customer.getName());
                        aPlayer.catchObject(bought.getUnProductItem());
                        QueryMaster.Update(table, new rec[] {new rec(columnNames[6], event.getWhoClicked().getName(), true)},
                                new rec(columnNames[0], ""+bought.getID()));
                        SellList.remove(bought);
                        Show((Player) event.getWhoClicked());
                        return true;
                    }
                }
                return false;
            }
            return false;
        }
        public void closing(Player player){
            if (filterMap.containsKey(player))
                filterMap.remove(player);
        }
}