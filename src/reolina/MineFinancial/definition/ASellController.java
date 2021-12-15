package reolina.MineFinancial.definition;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import reolina.MineFinancial.AControl.ABank;
import reolina.MineFinancial.AControl.AClan;
import reolina.MineFinancial.AControl.APlayer;
import reolina.MineFinancial.AControl.IBalance;

import java.math.BigDecimal;
import java.util.ArrayList;

public class ASellController {
    public IBalance seller;
    ItemStack itemStack;
    ItemMeta origMeta;
    BigDecimal price;
    String customer;
    int virtualAmount = 1;

    public String getStringSeller(){
        switch (seller.getOwnerType()){
            case player :
                return "@"+seller.getName();
            case clan:
                return "#"+seller.getName();
            case bank:
                return "$";
        }
        return null;
    }

    public ASellController(ItemStack item, String seller, BigDecimal cost){
        itemStack = item;
        origMeta = itemStack.getItemMeta();
        if (seller.startsWith("$"))
            this.seller = ABank.getInstance();
        else
            this.seller = seller.startsWith("@") ? APlayer.list.get(seller.substring(1)) : AClan.clans.get(seller.substring(1));
        price = cost;
        displayMeta();
    }

    public void changeSeller(String seller){
        if (seller.startsWith("$"))
            this.seller = ABank.getInstance();
        else
            this.seller = seller.startsWith("@") ? APlayer.list.get(seller.substring(1)) : AClan.clans.get(seller.substring(1));
    }
    private void displayMeta() {
        ItemMeta temp = origMeta.clone();
        ArrayList<String> lore = new ArrayList<>();
        if (origMeta.getLore() != null) {
            lore.addAll(origMeta.getLore());
            lore.add("");
        }
        String sllr = seller.getOwnerType() == Type.bank ? ChatColor.GOLD+"Банк" :
                (seller.getOwnerType() == Type.clan ? "клан "+ChatColor.LIGHT_PURPLE+ seller.getName() :
                        "игрок "+ChatColor.AQUA+seller.getName());
        lore.add(ChatColor.ITALIC+""+ChatColor.WHITE+"Продавец: "+sllr);
        lore.add(ChatColor.ITALIC+""+ChatColor.WHITE+"Цена: "+ChatColor.GOLD+" "+price);
        lore.add(ChatColor.ITALIC+""+ChatColor.GRAY +"За единицу: "+price.divide(new BigDecimal(virtualAmount)));
        temp.setLore(lore);
        itemStack.setItemMeta(temp);
    }

    public boolean changePrice(BigDecimal delta) {
        if (price.add(delta).compareTo(BigDecimal.ZERO) > 0) {
            price = price.add(delta);
            displayMeta();
            return true;
        } else return false;
    }

    public boolean changeAmount(int amount){
        if (itemStack.getMaxStackSize() >= amount){
            itemStack.setAmount(amount);
            displayMeta();
            return true;
        } else return false;
    }

    public ItemStack getItem() {
        displayMeta();
        return itemStack;
    }
    public void setVirtualAmount(int virtualAmount) {
        this.virtualAmount = virtualAmount;
        displayMeta();
    }

    public BigDecimal getPrice() { return price; }
}
