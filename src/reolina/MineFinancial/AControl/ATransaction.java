package reolina.MineFinancial.AControl;

import com.mojang.brigadier.Message;
import org.bukkit.ChatColor;
import reolina.MineFinancial.QueryMasterConstructor.*;
import reolina.MineFinancial.definition.Type;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.Map;
import java.util.HashMap;

public class ATransaction{
    static Logger log = Logger.getLogger("Minecraft");
    static public ArrayList<ATransaction> UnflyedList = new ArrayList<>();

    public int id;
    static private int lastID;
    IBalance sender;
    Type senderType;
    IBalance getter;
    Type getterType;
    BigDecimal amount;
    String itemID; int ItemAmount; String meta;
    private int errorCode;
    public boolean isFlyed;
    private static final String table = "transactions";
    private static final String[] columnNames = {"id", "payer", "payer_type","receiver","receiver_type", //4
                                                "transfer_amount","item_id","item_amount","meta","isflyed"}; //9

    static private int RecordTransaction(ATransaction trans)
    {
        QueryMaster.Insert(table, new rec[] {new rec(columnNames[0], Integer.toString(trans.id)),
                                            new rec(columnNames[1], trans.sender.getName(), true),
                                            new rec(columnNames[2], trans.senderType.toString(), true),
                                            new rec(columnNames[3], trans.getter.getName(), true),
                                            new rec(columnNames[4], trans.getterType.toString(), true),
                                            new rec(columnNames[5], trans.amount.toString()),
                                          new rec(columnNames[9], trans.isFlyed ? "true" : "false", true)}, null);
        return 0;
    }

    static public void init(){
        try{
            ResultSet rs = QueryMaster.Select(table,
                    new String[] {columnNames[0],columnNames[1],columnNames[2],columnNames[3],columnNames[4],
                            columnNames[5],columnNames[6],columnNames[7],columnNames[8],columnNames[9]}, null);
            while (rs.next()){
                if (!rs.getBoolean(columnNames[9])){
                    UnflyedList.add(new ATransaction(rs.getInt(columnNames[0]), rs.getString(columnNames[1]),
                            rs.getString(columnNames[2]), rs.getString(columnNames[3]), rs.getString(columnNames[4]),
                            rs.getBigDecimal(columnNames[5]),rs.getBoolean(columnNames[9])));
                }
                //transactionList.add(new ATransaction())
            }
        } catch (Exception ex) {
            QueryMaster.Create(new Table(table, new Field[]
                    {new Field(SQLtype.INTEGER, columnNames[0], true, true),
                            new Field(SQLtype.TEXT, columnNames[1], true),
                            new Field(SQLtype.TEXT, columnNames[2], true),
                            new Field(SQLtype.TEXT, columnNames[3], true),
                            new Field(SQLtype.TEXT, columnNames[4], true),
                            new Field(SQLtype.REAL, columnNames[5], true),
                            new Field(SQLtype.TEXT, columnNames[6]),
                            new Field(SQLtype.TEXT, columnNames[7]),
                            new Field(SQLtype.TEXT, columnNames[8]),
                            new Field(SQLtype.TEXT, columnNames[9], true),
                    }));
        }
    }

    public String Message(){
        switch (errorCode){
            case 300: return new String(ChatColor.RED+((senderType == Type.player) ? "У игрока " + ChatColor.AQUA: "На счету клана " + ChatColor.LIGHT_PURPLE )+
                            sender+ChatColor.RED+"недостаточно средств");
            case 301: return new String(ChatColor.RED+((senderType == Type.player) ? "Игрок-плательщик " + ChatColor.AQUA: "Клан-плательщик " + ChatColor.LIGHT_PURPLE )+
                    sender+ChatColor.RED+": ошибка записи в базу данных");
            case 310: return new String(ChatColor.RED+((getterType == Type.player) ? "У игрока " + ChatColor.AQUA: "На счету клана " + ChatColor.LIGHT_PURPLE )+
                    getter+ChatColor.RED+"недостаточно средств");
            case 311: return new String(ChatColor.RED+((getterType == Type.player) ? "Игрок-плательщик " + ChatColor.AQUA: "Клан-плательщик " + ChatColor.LIGHT_PURPLE )+
                    getter + ChatColor.RED+": ошибка записи в базу данных");
            case 399: return "При транзакции произошла ошибка";
            default: return "Error code "+errorCode;
        }
    }
    public int fly(ATransaction t){
        int SenderResult = t.sender.SubsBalance(amount);
        int GetterResult = 0;
        if (SenderResult == 0) {
            GetterResult = t.getter.AddBalance(amount);
            if (GetterResult == 0) t.isFlyed = true;
        }
        if (SenderResult == 100) t.errorCode = 300;
        if (SenderResult == 200) t.errorCode = 301;
        if (GetterResult == 100) t.errorCode = 310;
        if (GetterResult == 200) t.errorCode = 311;
        if (SenderResult > 0 || GetterResult > 0) t.errorCode = 399;
        return t.errorCode;

    }

    public ATransaction(IBalance sender, Type senderType, //sender
                        IBalance getter, Type getterType, //getter(?)
                        BigDecimal amount){
        id = ++lastID;
        this.sender = sender;
        this.senderType = senderType;
        this.getter = getter;
        this.getterType = getterType;
        this.amount = amount;
    }

    public ATransaction(int id, String sender, String senderType, //sender
                        String getter, String getterType, //getter(?)
                        BigDecimal amount,
                        boolean flyed){
        this.id = id;
        this.senderType = Type.valueOf(senderType);
        this.getterType = Type.valueOf(getterType);
        if (sender == "bank")
            this.sender = ABank.getInstance();
        else this.sender = (this.senderType == Type.player) ? APlayer.list.get(sender) : AClan.clans.get(sender);
        if (sender == "bank")
            this.getter = ABank.getInstance();
        else this.getter = (this.getterType == Type.player) ? APlayer.list.get(sender) : AClan.clans.get(sender);
        this.amount = amount;
        this.isFlyed = flyed;
    }
    //public ATransaction(IBalance sender, IBalance getter, ITEM/BLOCK???);

}
