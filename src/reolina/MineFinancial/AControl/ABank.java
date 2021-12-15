package reolina.MineFinancial.AControl;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftInventory;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import reolina.MineFinancial.QueryMasterConstructor.*;
import reolina.MineFinancial.definition.Type;
import reolina.MineFinancial.main.MineFinancial;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.chrono.MinguoEra;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ABank extends account implements IBalance { //def for bank(?)
    private static ABank instance = null;
    static Logger log = Logger.getLogger("Minecraft");

    static private final String table = "bank";
    static private final String[] columnName = {"id", "emission", "balance"};

    static private final String invTable = "bank_inv";
    static private final String[] invColumnNames = {"id", "item"};

    private ArrayList<ItemStack> BankOwns = new ArrayList<>();
    private Inventory bankInventory;

    Type OwnerType = Type.bank;
    private int UpdateBalance()
    {
        try{
            QueryMaster updBalance = new QueryMaster(QueryMaster.QueryType.UPDATE, "bank");
            updBalance.AddValue("balance",balance.toString());
            updBalance.AddWhere("id = 1");
            updBalance.Execute();
            return 0;
        }catch (Exception ex){
            log.info(ex.toString());
            return 200;
        }
    }

    public boolean MoneyEmission(BigDecimal emission) throws SQLException {
        QueryMaster newEmission = new QueryMaster(QueryMaster.QueryType.INSERT, "bank");
        newEmission.AddValue("emission", emission.toString());
        newEmission.AddValue("balance", balance.add(emission).toString());
        try{
            newEmission.Execute();
            balance = balance.add(emission);
            UpdateBalance();
            return true;
        }catch (SQLException ex)
        {
            log.info(ex+"\n"+ex.getStackTrace());
            return false;
        }
    }

    @Override public Type getOwnerType() {
        return OwnerType;
    }

    @Override public String getName(){
        return "bank";
    }
    @Override public int ChangeBalance(BigDecimal delta) {
        int res = super.ChangeBalance(delta);
        if (res > 0) return res;
        return UpdateBalance();
    }
    @Override public int SubsBalance(BigDecimal delta) {
        return ChangeBalance(delta.negate());
    }
    @Override public int AddBalance(BigDecimal delta) {
        return ChangeBalance(delta);
    }
    @Override public BigDecimal getBalance() {return balance; }

    protected ABank() {
        bankInventory = Bukkit.getServer().createInventory(null, 54, "Хранилище банка");
        try {
            QueryMaster query = new QueryMaster(QueryMaster.QueryType.SELECT, "bank");
            query.SelectFields(new String[]{columnName[2]});
            query.AddWhere("id = 1");
            ResultSet resultSet = query.ExecuteElection();
            resultSet.next();
            balance = resultSet.getBigDecimal(columnName[2]);
            if (balance == null) throw new Exception("balance is bull");
            resultSet.getStatement().close();
        } catch (Exception ex){
            log.info("Exception "+ex+"\n"+ex.getCause());
            QueryMaster creatingBank = new QueryMaster(QueryMaster.QueryType.CREATE, "bank");
            creatingBank.AddNewField(new Field(SQLtype.INTEGER,"id",true,true,true));
            creatingBank.AddNewField(new Field(SQLtype.REAL, "emission"));
            creatingBank.AddNewField(new Field(SQLtype.REAL, "balance", true));
            try {creatingBank.Execute();} catch (SQLException kex) {log.info(kex+"\n"+ex.getStackTrace());}
            balance = new BigDecimal(0);
            QueryMaster insBal = new QueryMaster(QueryMaster.QueryType.INSERT,"bank");
            insBal.AddValue("balance",balance.toString());
            try {insBal.Execute();} catch (SQLException kex) {log.info(kex+"\n"+ex.getStackTrace());}
        }

        try { //чтение инвентаря банка, если есть
            ResultSet rs = QueryMaster.Select(invTable, null, null);
            while (rs.next()){
                int index = rs.getInt(invColumnNames[0]);
                String s64 = rs.getString(invColumnNames[1]);
                byte[] arr = Base64.getDecoder().decode(s64);
                BukkitObjectInputStream BOIS = new BukkitObjectInputStream(new ByteArrayInputStream(arr));
                BankOwns.add(index, (ItemStack) BOIS.readObject());
            }
            rs.getStatement().close();
            bankInventory.setContents(BankOwns.toArray(ItemStack[]::new));
        } catch (Exception ex){
            log.info("Reading bank inventory is impossible: "+ex);
        }
    }

    public void InvShow(Player player){
        Inventory inv = Bukkit.createInventory(player, 18, "Продажа:");
        player.openInventory(inv);
    }

    public void getItems(ItemStack[] receivingItma) {

        for (ItemStack itma : receivingItma){
            if (itma == null) continue;
            else BankOwns.add(itma);
        }
        bankInventory.setContents(BankOwns.toArray(ItemStack[]::new));
    }
    public void saveBankInv(){
        QueryMaster.Drop(invTable);
        QueryMaster.Create(new Table(invTable, new Field[]
                {new Field(SQLtype.INTEGER, invColumnNames[0], true, true),
                 new Field(SQLtype.TEXT, invColumnNames[1])}));
        try {
            ByteArrayOutputStream BAOS = new ByteArrayOutputStream();
            BukkitObjectOutputStream BOOS = new BukkitObjectOutputStream(BAOS);
            for (ItemStack inst : BankOwns) {
                BOOS.writeObject(inst);
                String s64 = Base64.getEncoder().encodeToString(BAOS.toByteArray());
                QueryMaster.Insert(invTable, new rec[]
                        {new rec(invColumnNames[0], Integer.toString(BankOwns.indexOf(inst))),
                         new rec(invColumnNames[1], s64, true)}, null);
            }
        } catch (IOException ioex) {
            log.severe("ERR: encoding inventory array\n"+ioex+"\n"+ioex.getStackTrace());
        }
    }
    public void BankInvShow(Player player){
        player.openInventory(bankInventory);
    }

    public static ABank getInstance()
    {
        if (instance == null)
            instance = new ABank();
        return instance;
    }
}