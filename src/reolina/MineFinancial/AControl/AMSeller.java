package reolina.MineFinancial.AControl;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import reolina.MineFinancial.definition.AProduct;
import reolina.MineFinancial.definition.ASellController;

import java.math.BigDecimal;
import java.util.logging.Logger;

public class AMSeller {
    private ASellController PriceController;
    static Logger log = Logger.getLogger("Minecraft");
    static private ItemStack[] PlayerOrClan = {new ItemStack(Material.PLAYER_HEAD), new ItemStack(Material.RED_BANNER)};
    private boolean sellAsClan;
    private boolean hasClan;
    ItemStack amplifier;
    ItemStack divider;
    ItemStack adder;
    ItemStack subser;
    Inventory SellInv;

    static public void init(){
        PlayerOrClan[0].getItemMeta().setDisplayName("Игрок");
        PlayerOrClan[1].getItemMeta().setDisplayName("Клан");
    }

    public AMSeller(Player player){
        SellInv = Bukkit.createInventory(player, 9, "Продать");

        amplifier = new ItemStack(Material.YELLOW_WOOL);
        ItemMeta meta = amplifier.getItemMeta(); meta.setDisplayName("x10");
        amplifier.setItemMeta(meta);

        divider = new ItemStack(Material.YELLOW_WOOL);
        meta = divider.getItemMeta(); meta.setDisplayName("/10");
        divider.setItemMeta(meta);

        adder = new ItemStack(Material.GREEN_WOOL);
        meta = adder.getItemMeta(); meta.setDisplayName("+1");
        adder.setItemMeta(meta);

        subser = new ItemStack(Material.RED_WOOL);
        meta = subser.getItemMeta(); meta.setDisplayName("-1");
        subser.setItemMeta(meta);

        ItemStack paper = new ItemStack(Material.PAPER);
        meta = paper.getItemMeta(); meta.setDisplayName("Информация о товаре:");
        paper.setItemMeta(meta);
        PriceController = new ASellController(paper, "@"+player.getName(), new BigDecimal(0));

        ItemStack isPlayer;
        SellInv.setContents(new ItemStack[]{PlayerOrClan[0], null, divider, subser, null, adder, amplifier, null, PriceController.getItem()});

        if (APlayer.list.get(player.getName()).getMemberOfClan() != null &&
                !APlayer.list.get(player.getName()).getMemberOfClan().equals(""))
            hasClan = true;

        player.openInventory(SellInv);
    }

    public void SellerInterfaceEventManager(InventoryClickEvent event){
        Inventory inv = event.getInventory();
        if ((event.isLeftClick() || event.isRightClick()) &&
                (event.getSlot() != 4 && event.getSlot() < 9 && event.getSlotType() == InventoryType.SlotType.CONTAINER)) {
            //if (inv.getItem(4) != null){
                ItemMeta metaSubsr = inv.getItem(3).getItemMeta(); //он жалуется, что может produce NullPointerException...
                ItemMeta metaAdder = inv.getItem(5).getItemMeta(); //нет, не может, эти предметы объявлены выше, поэтому здесь их существование несомненно
                switch ((event.getSlot())){
                    //
                    case 0:
                        if (hasClan) {
                            sellAsClan = !sellAsClan;
                            PriceController.changeSeller(sellAsClan ? "#"+APlayer.list.get(event.getWhoClicked().getName()).getMemberOfClan()
                                    : "@"+event.getWhoClicked().getName());
                        }
                        break;
                    case 2:
                        metaAdder.setDisplayName(""+(Double.parseDouble(metaAdder.getDisplayName())/10));
                        adder.setItemMeta(metaAdder);
                        metaSubsr.setDisplayName(""+(Double.parseDouble(metaSubsr.getDisplayName())/10));
                        subser.setItemMeta(metaSubsr);
                        break;
                    case 6:
                        metaAdder.setDisplayName(""+(Double.parseDouble(metaAdder.getDisplayName())*10));
                        adder.setItemMeta(metaAdder);
                        metaSubsr.setDisplayName(""+(Double.parseDouble(metaSubsr.getDisplayName())*10));
                        subser.setItemMeta(metaSubsr);
                        break;
                    case 3:
                        if (inv.getItem(4) == null)
                            break;
                        PriceController.changePrice(new BigDecimal(metaSubsr.getDisplayName()));
                        break;
                    case 5:
                        if (inv.getItem(4) == null)
                            break;
                        PriceController.changePrice(new BigDecimal(metaAdder.getDisplayName()));
                        break;
                    case 4:
                        if (inv.getItem(4) != null)
                            PriceController.setVirtualAmount(inv.getItem(4).getAmount());
                        else
                            PriceController.setVirtualAmount(1);;
                        break;
                    case 8:
                        if (event.getInventory().getItem(4) != null){ //здесь мы создаём новый продукт из этой вещи
                            String seller = sellAsClan ? "#"+APlayer.list.get(event.getWhoClicked().getName()).getMemberOfClan()
                                    : "@"+event.getWhoClicked().getName();
                            AProduct aProduct = new AProduct(event.getInventory().getItem(4), seller, PriceController.getPrice());
                            event.getInventory().setItem(4, null);
                            AMarket.getInstance().addProduct(aProduct);
                            event.getWhoClicked().sendMessage(ChatColor.GREEN+"Предмет " +ChatColor.DARK_GREEN+
                                    aProduct.getName()+ChatColor.GREEN+" помещён на рынок");
                        }
                    default: break;
                }
            //}
            SellInv.setContents(new ItemStack[]{sellAsClan ? PlayerOrClan[1] : PlayerOrClan[0], null,
                    divider, subser, event.getInventory().getItem(4), adder, amplifier, null, PriceController.getItem()});
            ((Player)event.getWhoClicked()).openInventory(SellInv);
            event.setCancelled(true);
        }
    }

    public void Remove(InventoryCloseEvent event){
        AMarket.amSellers.remove(this);
    }
}
