package reolina.MineFinancial.AControl.AMarket;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import reolina.MineFinancial.AControl.ABank;
import reolina.MineFinancial.AControl.AClan;
import reolina.MineFinancial.AControl.APlayer;
import reolina.MineFinancial.AControl.IBalance;
import reolina.MineFinancial.definition.Type;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.logging.Logger;

public class  AProduct {
    static Logger log = Logger.getLogger("Minecraft");
    static int lastID = 0;
    int id;
    public IBalance seller;
    ItemStack itemStack;
    ItemMeta origMeta;
    BigDecimal price;
    String customer;

    public int getID() {return id;}
    static public int IDFromItemStack(ItemStack item){
        ArrayList<String> arr = new ArrayList<>(item.getItemMeta().getLore());
        if (arr != null){
            for (String s : arr){
                if (s.contains("Продавец")) {
                    return Integer.parseInt(s.substring(s.indexOf('(')+1,s.indexOf(')')));
                }
            }
        }
        return -1;
    }
    public Material getItemMaterial(){
        return itemStack.getData().getItemType();
    }
    public int getAmount(){ return itemStack.getAmount();}
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
    private AProduct(ItemStack item, String seller){
        log.info("Loading ItemStack in store: seller "+seller+" ("+seller.substring(1)+")");
        itemStack = item;
        origMeta = itemStack.getItemMeta();
        if (seller.startsWith("$"))
            this.seller = ABank.getInstance();
        else
            this.seller = seller.startsWith("@") ? APlayer.list.get(seller.substring(1)) : AClan.clans.get(seller.substring(1));
        id = lastID++;
    }

    public AProduct(ItemStack item, String seller, BigDecimal cost){
        this(item, seller);
        price = cost;
        displayMeta();
    }

    public AProduct(ItemStack item, String seller, String encodedString, BigDecimal cost){
        this(item, seller);
        try {
            price = cost;
            ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(encodedString));
            BukkitObjectInputStream bois = new BukkitObjectInputStream(bais);
            origMeta = (ItemMeta) bois.readObject();
            itemStack.setItemMeta(origMeta);
            if (price == null)
                this.price = new BigDecimal(0);
            else
                price = cost;
            displayMeta();
        } catch (Exception ex) {
            log.warning("ERR: "+ex+"\n"+ex.getStackTrace());
        }
    }

    public void changeSeller(String seller){
        if (seller.startsWith("$"))
            this.seller = ABank.getInstance();
        else
            this.seller = seller.startsWith("@") ? APlayer.list.get(seller.substring(1)) : AClan.clans.get(seller.substring(1));
    }
    public String getName() { return itemStack.toString(); }
    public String getLocalName() { return itemStack.getItemMeta().getLocalizedName();}
    public BigDecimal getPrice() { return price; }
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
        lore.add(ChatColor.ITALIC+""+ChatColor.GRAY+"("+id+")"+ChatColor.WHITE+" Продавец: "+sllr);
        lore.add(ChatColor.ITALIC+""+ChatColor.WHITE+"Цена: "+ChatColor.GOLD+" "+price);
        lore.add(ChatColor.ITALIC+""+ChatColor.GRAY+"За единицу: "+price.divide(new BigDecimal(itemStack.getAmount())));
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

    public String getSerializedOrigMeta() {
        try {
            ByteArrayOutputStream BAOS = new ByteArrayOutputStream();
            BukkitObjectOutputStream BOOS = new BukkitObjectOutputStream(BAOS);
            BOOS.writeObject(origMeta);
            String s64 = Base64.getEncoder().encodeToString(BAOS.toByteArray());
            return s64;
        } catch (IOException ex) {
            log.warning(ex+"\n"+ex.getStackTrace());
            return null;
        }
    }

    public boolean changeAmount(int amount){
        if (itemStack.getMaxStackSize() >= amount){
            itemStack.setAmount(amount);
            displayMeta();
            return true;
        } else return false;
    }

    public ItemStack getProductItem() {
        displayMeta();
        return itemStack;
    }
    public ItemStack getUnProductItem(){
        itemStack.setItemMeta(origMeta);
        return itemStack;
    }
        //проверки на равенство
    public boolean equals(AProduct aProduct){
        return (itemStack.toString().equals(aProduct.itemStack.toString()));
    }
    public boolean equals(ItemStack itemStack){
        return itemStack.toString().equals(itemStack.toString());
    }
}
