package reolina.MineFinancial.AControl;

import com.mysql.fabric.xmlrpc.base.Array;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.libs.jline.internal.Log;
import org.bukkit.entity.Player;
import reolina.MineFinancial.QueryMasterConstructor.Field;
import reolina.MineFinancial.QueryMasterConstructor.QueryMaster;
import reolina.MineFinancial.QueryMasterConstructor.SQLtype;
import reolina.MineFinancial.QueryMasterConstructor.rec;
import reolina.MineFinancial.definition.CRole;
import reolina.MineFinancial.definition.FRole;
import reolina.MineFinancial.definition.Type;

import java.sql.ResultSet;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

public class AClan extends account implements IBalance {
    static public Map<String, AClan> clans = new HashMap<>(); //список кланов: имя клана - клан
    public String clanLeader;   //лидер конкретного клана
    public Map<String, CRole> membersRole = new HashMap<>(); //список игроков и роли (для экземляра клана)
    static private final String table = "clans"; //в конструктор таблиц и для обращения
    static private final String[] columnNames = {"name","balance","leader","members","crole"}; //аналогично
    static Logger log = Logger.getLogger("Minecraft");
    public String Name;
    Type OwnerType = Type.clan;

    static private CRole[] cr = CRole.values();
    static private String[] RebuildStringsMemCR(Map<String, CRole> mems){
            //метод лепит всех игроков и их роли в две строки, удобные для ввода в БД
        Log.debug("<[RebuildStringMemCR]>");
        StringBuilder perUpdateV = new StringBuilder("");
        StringBuilder perUpdateM = new StringBuilder("");
        var cRoles = mems.values();
        var members = mems.keySet();
        for (var c : cRoles)
            if (perUpdateV.length()<1)
                perUpdateV.append(c);
            else
                perUpdateV.append(" "+c);
        for (var m : members)
            if (perUpdateM.length()<1)
                perUpdateM.append(m);
            else
                perUpdateM.append(" "+m);
        Log.debug("3: "+perUpdateM+"|4: "+perUpdateV);
        Log.debug(">[RebuildStringMemCR]<");
        return new String[] {perUpdateM.toString(/*column 3*/), perUpdateV.toString(/*column 4*/)};
    }
    static private void UpdateMembers(String clanName){
        String[] strings = RebuildStringsMemCR(clans.get(clanName).membersRole);
        QueryMaster upd = new QueryMaster(QueryMaster.QueryType.UPDATE, table);
        upd.AddValue(columnNames[3], strings[0].toString(), true);
        upd.AddValue(columnNames[4], strings[1], true);
        upd.AddWhere(columnNames[0]+" = \""+clanName+"\"");
        try{upd.Execute(); }
        catch (Exception ex) {log.severe("Something SEVERE happened: "+ex.toString()+"\n"+ex.getStackTrace());}
    }
    static private void UpdateClanName(String oldName, String newName){
        QueryMaster upd = new QueryMaster(QueryMaster.QueryType.UPDATE, table);
        upd.AddValue(columnNames[0], newName, true);
        upd.AddWhere(columnNames[0]+" = \""+oldName+"\"");
        try{upd.Execute(); }
        catch (Exception ex) {log.severe("Something SEVERE on renaming clan in DB:\n"+ex.toString()+"\n"+ex.getStackTrace());}
    }
    static private int UpdateBalance(AClan clan){
        QueryMaster upd = new QueryMaster(QueryMaster.QueryType.UPDATE, table);
        upd.AddValue(columnNames[1], clan.balance.toString());
        upd.AddWhere(columnNames[0]+" = \""+clan.Name+"\"");
        try{upd.Execute(); return 0;}
        catch (Exception ex) {log.severe("Something SEVERE on updating clan balance DB:\n"+ex.toString()+"\n"+ex.getStackTrace());
            return 200;}
    }
    static private void UpdateLeaderName(String clanName, String newLeader){
        QueryMaster upd = new QueryMaster(QueryMaster.QueryType.UPDATE, table);
        upd.AddValue(columnNames[2], newLeader, true);
        upd.AddWhere(columnNames[0]+" = \""+clanName+"\"");
        try{upd.Execute(); }
        catch (Exception ex) {log.severe("Something SEVERE on renaming clan in DB:\n"+ex.toString()+"\n"+ex.getStackTrace());}
        UpdateMembers(clanName);
    }
    static private void RecordClan(AClan newClan){
        String[] strings = RebuildStringsMemCR(newClan.membersRole);
        QueryMaster recc = new QueryMaster(QueryMaster.QueryType.INSERT, table);
        recc.AddValue(columnNames[0], newClan.Name, true);
        recc.AddValue(columnNames[1], newClan.GetBalance().toString());
        recc.AddValue(columnNames[2], newClan.clanLeader, true );
        recc.AddValue(columnNames[3], strings[0], true);
        recc.AddValue(columnNames[4], strings[4], true);
        try {recc.Execute(); }
        catch (Exception e) {log.severe("ADDING CLAN ERR: "+e+"\n"+e.getStackTrace());}
    }
    static public int RemoveClan(String sender, AClan clanToRemove){
        if (clanToRemove.membersRole.get(sender) == CRole.leader) {
            QueryMaster.Delete(table, new rec(columnNames[0], clanToRemove.Name, true));
            for (String s : clanToRemove.membersRole.keySet()){
                APlayer.list.get(s).ChangeClan("");
                Bukkit.getPlayer(s).sendMessage(ChatColor.DARK_AQUA+"Клан "+
                        ChatColor.DARK_PURPLE+clanToRemove+ChatColor.DARK_AQUA+" распущен");
            }
            return 0;
        } else return 401;
    }

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

    static public String GetClanList(){
        StringBuilder ClanList = new StringBuilder();
        if (!clans.isEmpty()) {
            ClanList.append("Список кланов ("+clans.size()+")\n");
            for (String s : clans.keySet()){
                ClanList.append(s+", ");
            }
            ClanList.delete(ClanList.lastIndexOf(","),ClanList.length());
        } else ClanList.append("Кланов нет");
        return ClanList.toString();
    }
    public String GetMembersList(){
        StringBuilder MemberList = new StringBuilder();
        for (String s : membersRole.keySet()){
            MemberList.append(membersRole.get(s) == CRole.leader ? ChatColor.RED+s+ChatColor.WHITE+", " :
                    membersRole.get(s) == CRole.economist ? ChatColor.GREEN+s+ChatColor.WHITE+", " : s+", ");
        }
        MemberList.delete(MemberList.lastIndexOf(","),MemberList.length());
        return MemberList.toString();
    }
    static public int RenameClan(String oldName, String newName){
        if (clans.containsKey(newName))
            return 102; //the name is busy;
        else {
            AClan aClan = clans.get(oldName);
            for (String pl : aClan.membersRole.keySet()) { //переименовываем клан у всех
                APlayer.list.get(pl).ChangeClan(newName);
            }
            clans.remove(oldName); //удаляем временно клан со старым названием
            aClan.Name = newName;
            clans.put(newName, aClan); //возвращаем на место с новым именем
            UpdateClanName(oldName, newName);
            return 0;
        }
    }
    static public boolean PromoteCRole(String clanName, String memberName)    {
        AClan aClan = clans.get(clanName);
        CRole crole = aClan.membersRole.get(memberName);
        if (crole.ordinal() < cr.length){
            crole = (cr[crole.ordinal()+1]);
            UpdateMembers(clanName);}
        else return false;
        return true;
    }
    static public boolean DemoteCRole(String clanName, String memberName){
        AClan aClan = clans.get(clanName);
        CRole crole = aClan.membersRole.get(memberName);
        if (crole.ordinal() > 0){
            crole = (cr[crole.ordinal()-1]);
            UpdateMembers(clanName);}
        else return false;
        return true;
    }
    static public boolean SetCRole(String clanName, FRole memberName, CRole newCRole){
        AClan aClan = clans.get(clanName);
        CRole crole = aClan.membersRole.get(memberName);
        crole = newCRole;
        UpdateMembers(clanName);
        return true;
    }
    static public String SearchPlayer(String playerName) /*имя клана, к которому принадлежит игрок*/{
        var listOfClans = clans.values();
        for (AClan clan : listOfClans) {
            if (clan.membersRole.containsKey(playerName))
                return clan.Name;
        }
        return null;
    }
    static public int CreateClan(String clanName, String sender){
        var listOfClans = clans.values();
        if (listOfClans.contains(clanName))
            return 102;
        else{
            if (SearchPlayer(sender) == null) {
                clans.put(clanName, new AClan(clanName,new BigDecimal(0), sender,
                        new String[] { sender } ,
                        new String[] {CRole.leader.toString()}));
                APlayer.list.get(sender).ChangeClan(clanName);
                AClan acl = clans.get(clanName);
                String[] s = RebuildStringsMemCR(clans.get(clanName).membersRole);
                QueryMaster.Insert(table, new rec[]{new rec(columnNames[0],clanName, true),
                                        new rec(columnNames[1], clans.get(clanName).balance.toString()),
                                        new rec(columnNames[2], sender, true),
                                        new rec(columnNames[3], s[0], true),
                                        new rec(columnNames[4], s[1], true)}, null);
                return 0;
            } else return 403;
        }
    }

    public void SendClanMessage(String Message){
        for (String s : membersRole.keySet()){
            Bukkit.getPlayer(s).sendMessage(Message);
        }
    }
    public void SendClanMessageExept(String Message, String[] Except){
        List<String> NamesExcept = Arrays.asList(Except);
        for (String s : membersRole.keySet()){
            if (NamesExcept.contains(s)) continue;
            Bukkit.getPlayer(s).sendMessage(Message);
        }
    }
    public int AddMember(String playerName){
        if (SearchPlayer(playerName) != null)
            return 403;
        membersRole.put(playerName, CRole.member);
        APlayer.list.get(playerName).ChangeClan(this.Name);
        UpdateMembers(this.Name);
        return 0;
    }
    public int AddMember(String playerName, CRole cRole){
        if (SearchPlayer(playerName) != null)
            return 403;
        if (cRole != CRole.leader) {
            membersRole.put(playerName, cRole);
            APlayer.list.get(playerName).ChangeClan(this.Name);
            UpdateMembers(this.Name);
        }
        else return 401; //не клановый лидер пытается получить доступ к команде лидера клана
        return 0;
    }
    public int RemovePlayer(String playerName){
        if (!membersRole.containsKey(playerName)) return 402;
        CRole cRole = membersRole.get(playerName);
        if (cRole != CRole.leader)
            membersRole.remove(playerName);
        else return 401;
        APlayer.list.get(playerName).ChangeClan("");
        UpdateMembers(Name);
        return 0;
    }

    public int ChangeLeader(String sender, String newLeaderName){ //следует ли сделать static???
        if (clanLeader == sender){
            if (membersRole.keySet().contains(newLeaderName)){
                CRole cRole = membersRole.get(newLeaderName);
                cRole = CRole.leader;
                cRole = membersRole.get(clanLeader); //ссылка теперь на лидера
                cRole = CRole.economist; //снижаем до экономиста
                clanLeader = newLeaderName;
                UpdateLeaderName(this.Name, newLeaderName);
                return 0;
            } else return 1;//(ChatColor.AQUA+newLeaderName+ChatColor.RED+" не является членом клана "+ChatColor.LIGHT_PURPLE+Name);
        } else {
            return 2;//(ChatColor.RED+"Вы не являетесь лидером клана "+ChatColor.LIGHT_PURPLE+Name);
        }
    }

    public static void init(){
        try{
            QueryMaster allclans = new QueryMaster(QueryMaster.QueryType.SELECT, table);
            ResultSet clansList = allclans.ExecuteElection();
            while (clansList.next()){
                clans.put(clansList.getString(columnNames[0]),
                        new AClan(clansList.getString(columnNames[0]),
                                clansList.getBigDecimal(columnNames[1]),
                                clansList.getString(columnNames[2]),
                                clansList.getString(columnNames[3]).split(" "),
                                clansList.getString(columnNames[4]).split(" ")));
            }
        } catch (Exception ex){
            log.warning("Seems there is no table "+ex+"\n"+ex.getStackTrace());
            QueryMaster createClans = new QueryMaster(QueryMaster.QueryType.CREATE, table);
            createClans.AddNewField(new Field(SQLtype.TEXT, columnNames[0], true, true));
            createClans.AddNewField(new Field(SQLtype.REAL, columnNames[1], true));
            createClans.AddNewField(new Field(SQLtype.TEXT, columnNames[2], true));
            createClans.AddNewField(new Field(SQLtype.TEXT, columnNames[3], false));
            createClans.AddNewField(new Field(SQLtype.TEXT, columnNames[4], false));
            try {
                createClans.Execute();
            } catch (Exception e) {
                log.severe("ERR in clan table creation: "+e+"\n"+e.getStackTrace());
            }
            }
        }

    public AClan(String name, BigDecimal balance, String clanLeader, String[] members, String[] croles) {
        this.balance = balance;
        Name = name;
        this.clanLeader = clanLeader;
        for (int i = 0; i < members.length; i++){
            membersRole.put(members[i], CRole.valueOf(croles[i]));
        }
        //playerList = listOfMembers;
        }
}
