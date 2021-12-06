package reolina.MineFinancial.AControl;

import reolina.MineFinancial.QueryMasterConstructor.Field;
import reolina.MineFinancial.QueryMasterConstructor.QueryMaster;
import reolina.MineFinancial.QueryMasterConstructor.SQLtype;
import reolina.MineFinancial.QueryMasterConstructor.rec;
import reolina.MineFinancial.definition.FRole;
import reolina.MineFinancial.definition.Type;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.logging.Logger;

public class APlayer extends account implements IBalance{
    static public Map<String, APlayer> list = new HashMap<>();
    static private final String table = "players";
    static private final String[] columnNames = {"nickname", "balance", "frole", "clan"};
    static Logger log = Logger.getLogger("Minecraft");
    protected String MemberOfClan;

    public String Name;
    Type OwnerType = Type.player;
    public FRole frole;

    static private void UpdateClan(APlayer player) {
        QueryMaster.Update(table, new rec[]{new rec(columnNames[3], player.MemberOfClan, true)},
                new rec(columnNames[0], player.Name, true));
    }
    public void ChangeClan(String clanName){
        MemberOfClan = clanName;
        UpdateClan(this);
    }
    static protected int UpdateBalance(APlayer player) {
        QueryMaster updBal = new QueryMaster(QueryMaster.QueryType.UPDATE, table);
        updBal.AddValue(columnNames[1], player.balance.toString());
        updBal.AddWhere(columnNames[0]+" = \""+player.Name+"\"");
        try {updBal.Execute(); } catch (Exception ex) {
            log.severe("Something SEVERE happened (updating balance)\n: " + ex.toString() + "\n" + ex.getStackTrace());
            return 200;
        }
        return 0;
    }

    public String getMemberOfClan() {return MemberOfClan; }
    @Override public String getName() {
        return Name;
    }
    @Override public int ChangeBalance(BigDecimal delta) {
        if (balance.subtract(delta).compareTo(_zero) < 0)
            return 100;
        balance = balance.subtract(delta);
        return UpdateBalance(this);
    }
    @Override public int SubsBalance(BigDecimal delta) {
        delta = _zero.subtract(delta);
        int res = ChangeBalance(delta);
        return res;
    }
    @Override public int AddBalance(BigDecimal delta) {
        return ChangeBalance(delta);
    }

    ///Changing FRore
    static private FRole[] fr = FRole.values();
    static private void UpdateFRole(String playerName, FRole newfr){
        QueryMaster upd = new QueryMaster(QueryMaster.QueryType.UPDATE, table);
        upd.AddValue(columnNames[2], newfr.toString(), true);
        upd.AddWhere(columnNames[0]+" = \""+playerName+"\"");
        try{upd.Execute(); }
        catch (Exception ex) {log.severe("Something SEVERE happened: "+ex.toString()+"\n"+ex.getStackTrace());}
    }
    static public boolean PromoteFRole(String playerName)    {
        APlayer aPlayer = list.get(playerName);
        if (aPlayer.frole.ordinal() < fr.length){
            aPlayer.frole = (fr[aPlayer.frole.ordinal()+1]);
            UpdateFRole(playerName, aPlayer.frole);}
        else return false;
        return true;
    }
    static public boolean DemoteFRole(String playerName){
        APlayer aPlayer = list.get(playerName);
        if (aPlayer.frole.ordinal() > 0){
            aPlayer.frole = (fr[aPlayer.frole.ordinal()-1]);
            UpdateFRole(playerName, aPlayer.frole);}
        else return false;
        return true;
    }
    static public boolean SetFRole(String playerName, FRole newFRole){
        APlayer aPlayer = list.get(playerName);
        aPlayer.frole = newFRole;
        UpdateFRole(playerName, aPlayer.frole);
        return true;
    }

    ///Adding players
    static public void AddPlayer(String playerName){
        list.put(playerName, new APlayer(playerName, new BigDecimal(0), FRole.commoner, null));
        QueryMaster insertNew = new QueryMaster(QueryMaster.QueryType.INSERT, table);
        insertNew.AddValue(columnNames[0], playerName, true);
        insertNew.AddValue(columnNames[1], "0");
        insertNew.AddValue(columnNames[2], FRole.commoner.toString(), true);
        try{insertNew.Execute();}
            catch (Exception ex) {log.severe("Something SEVERE happened (Adding new player)\n: "+ex.toString()+"\n"+ex.getStackTrace());}
    }

    static public void init() {
        try {
            QueryMaster selectAll = new QueryMaster(QueryMaster.QueryType.SELECT, table);
            ResultSet rsPlayers = selectAll.ExecuteElection();
            while (rsPlayers.next()){
                String name = rsPlayers.getString(columnNames[0]);
                list.put(name, new APlayer(name,
                                rsPlayers.getBigDecimal(columnNames[1]),
                                FRole.valueOf(rsPlayers.getString(columnNames[2])),
                                rsPlayers.getString(columnNames[3])));
            }
        } catch (SQLException sqlex){
            log.warning("ERROR: "+sqlex.toString()+"\n"+sqlex.getStackTrace());
            QueryMaster creatingPlayerTable = new QueryMaster(QueryMaster.QueryType.CREATE, table);
            creatingPlayerTable.AddNewField(new Field(SQLtype.TEXT, columnNames[0], true, true));
            creatingPlayerTable.AddNewField(new Field(SQLtype.REAL, columnNames[1], true));
            creatingPlayerTable.AddNewField(new Field(SQLtype.TEXT, columnNames[2], true));
            creatingPlayerTable.AddNewField(new Field(SQLtype.TEXT, columnNames[3]));
            try{creatingPlayerTable.Execute();} catch (SQLException ex) {log.severe("FATAL: "+ex.toString()+"\n"+ex.getStackTrace());}
          //No one will be added, as not created table supposes new server;
        }
    }

    protected APlayer(String name, BigDecimal balance, FRole frole, String clanMember) {
        Name = new String(name);
        this.balance = balance;
        this.frole = frole;
        MemberOfClan = clanMember;
    }
}
