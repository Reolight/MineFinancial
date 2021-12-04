package reolina.MineFinancial.AControl;

import reolina.MineFinancial.QueryMasterConstructor.Field;
import reolina.MineFinancial.QueryMasterConstructor.QueryMaster;
import reolina.MineFinancial.QueryMasterConstructor.SQLtype;
import reolina.MineFinancial.QueryMasterConstructor.Table;
import reolina.MineFinancial.definition.Type;

import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.util.ArrayList;

public class AReminder {
    static int lastID = 0;
    static final String table = "reminders";
    static final String[] columnNames = {"id", "getter", "sender", "sender_type", "reminder", "need_reply"};
    int id;
    String sender;
    String getter;
    String reminder;
    Type senderType;
    boolean isApproval;
    static ArrayList<AReminder> remList = new ArrayList<>();

    public void init(){
        try{
            ResultSet rs = QueryMaster.Select(table, columnNames, null);
            while (rs.next()){
                if (rs.getBoolean(columnNames[5])) {
                    remList.add(new AReminder(rs.getInt(columnNames[0]), rs.getString(columnNames[1]), rs.getString(columnNames[2]),
                            rs.getString(columnNames[3]), rs.getString(columnNames[4]), rs.getString(columnNames[5]) == "true"));
                    lastID++;
                }
            }
        } catch (Exception on){
            QueryMaster.Create(new Table(table, new Field[]
                    {new Field(SQLtype.INTEGER, columnNames[0], true, true),
                    new Field(SQLtype.TEXT, columnNames[1], true),
                    new Field(SQLtype.TEXT, columnNames[2]),
                    new Field(SQLtype.TEXT, columnNames[3]),
                    new Field(SQLtype.TEXT, columnNames[4], true),
                    new Field(SQLtype.TEXT, columnNames[5], true)}));
            lastID = 1;
        }
    }

    public AReminder(String getter, String sender, String send_type, String remind, boolean isApproval) {
        this.sender = sender;
        this.senderType = Type.valueOf(send_type);
        this.getter = getter;
        reminder = remind;
        this.isApproval = isApproval;
        id = ++lastID;
    }

    private AReminder(int _id, String getter, String sender, String send_type, String remind, boolean isApproval) {
        this.sender = sender;
        this.senderType = Type.valueOf(send_type);
        this.getter = getter;
        reminder = remind;
        this.isApproval = isApproval;
        id = _id;
    }
}