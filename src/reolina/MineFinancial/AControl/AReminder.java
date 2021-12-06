package reolina.MineFinancial.AControl;

import com.mysql.jdbc.Buffer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import reolina.MineFinancial.QueryMasterConstructor.*;
import reolina.MineFinancial.definition.ERemindersAction;
import reolina.MineFinancial.definition.Type;

import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class AReminder {
    static int lastID = 0;
    static final String table = "reminders";
    static final String[] columnNames = {"id", "getter", "sender", "reminder", "need_reply", "action"};
    static Logger log = Logger.getLogger("Minecraft");
    int id;
    String sender;
    String getter;
    String reminder;
    boolean isApproval;
    boolean isInDatabase;
    ERemindersAction ERAction;
    static ArrayList<AReminder> remList = new ArrayList<>();

    static public boolean Reply(int id, boolean reply){
        Optional<AReminder> optionalAReminder = remList.stream().filter(ar -> ar.id == id).findFirst();
        if (optionalAReminder.isPresent()){
            AReminder aReminder = optionalAReminder.get();
            switch (aReminder.ERAction) {
                case ClanInvitation:
                    if (reply) {
                        AClan.clans.get(aReminder.sender).AddMember(aReminder.getter);
                    } else Bukkit.getPlayer(AClan.clans.get(aReminder.sender).clanLeader)
                            .sendMessage(ChatColor.AQUA+ aReminder.getter+ChatColor.BLUE+" отклонил предложение о вступлении в клан");
                    break;
                case ClanApplying: //посылает запрос sender, получает лидер клана getter
                    if (reply) {
                        AClan.clans.get(aReminder.getter).AddMember(aReminder.sender);
                    } else Bukkit.getPlayer(aReminder.sender).sendMessage(ChatColor.AQUA+aReminder.getter+ChatColor.BLUE+" отклонил вашу заявку о вступлении в клан "
                        + ChatColor.LIGHT_PURPLE+ APlayer.list.get(aReminder.getter).MemberOfClan);
                    break;
                case ClanDeletion:
                    if (reply) {
                        int remR = AClan.RemoveClan(aReminder.getter, AClan.clans.get(aReminder.getter));
                        if (remR > 0) Bukkit.getPlayer(aReminder.getter).sendMessage(ChatColor.RED+"Ошибка "+remR);
                    } else Bukkit.getPlayer(aReminder.sender).sendMessage(ChatColor.GRAY+""+ChatColor.ITALIC+"Удаление клана отменено");
                    break;
            }
            if (aReminder.isInDatabase) {
                QueryMaster.Delete(table, new rec(columnNames[0], Integer.toString(id)));
                remList.remove(aReminder);
            }
            return true;
        } else return false;
    }

    static void Send(AReminder remToSend){
        var pl = Bukkit.getPlayer(remToSend.getter);
        pl.sendMessage((remToSend.ERAction == null ? "" : ChatColor.ITALIC+""+ChatColor.GRAY + remToSend.id+": " +ChatColor.RESET) + remToSend.reminder
                +(remToSend.isApproval ? ChatColor.ITALIC+""+ChatColor.GRAY + " (/rem ID [y|n})" : ""));
        if (!(remToSend.isApproval) && remToSend.isInDatabase) {
            log.info("deleting reminder: !remToSend.isApproval is "+(!remToSend.isApproval)+" remToSend.isInDatabase is "+ remToSend.isInDatabase+" and in toto: "
                +(!(remToSend.isApproval) && remToSend.isInDatabase));
            QueryMaster.Delete(table, new rec(columnNames[0], Integer.toString(remToSend.id)));
            remList.remove(remToSend);
        }
    }
    public static void SendAll(String getter){
        ArrayList<AReminder> rem = remList.stream().filter(aReminder -> aReminder.getter.equalsIgnoreCase(getter))
                .collect(Collectors.toCollection(ArrayList::new));
        for (AReminder a : rem)
            Send(a);
    }

    public static void RemindCount(String getter) {
        ArrayList<AReminder> rem = remList.stream().filter(aReminder -> aReminder.getter.equalsIgnoreCase(getter))
                .collect(Collectors.toCollection(ArrayList::new));
        Bukkit.getPlayer(getter).sendMessage(ChatColor.BOLD+""+ChatColor.GREEN+"Получено уведомлений: "+
                (rem.size() == 0 ? ChatColor.GRAY : ChatColor.GOLD)+rem.size()+ChatColor.GREEN+"."); //отчёт о количестве уведомлений
    }

    static public void init(){
        try{
            ResultSet rs = QueryMaster.Select(table, null, null);
            lastID = -1;
            while (rs.next()){
                ERemindersAction era = null;
                try{ era = ERemindersAction.valueOf(rs.getString(columnNames[5])); } catch (Exception e) {}
                remList.add(new AReminder(rs.getInt(columnNames[0]), rs.getString(columnNames[1]), rs.getString(columnNames[2]),
                        rs.getString(columnNames[3]), rs.getString(columnNames[4]) == "true",
                        era));
                lastID++;
            }
        } catch (Exception on){
            log.warning("EX: "+on.toString()+"\n"+on.getStackTrace());
            QueryMaster.Create(new Table(table, new Field[]
                    {new Field(SQLtype.INTEGER, columnNames[0], true, true),
                    new Field(SQLtype.TEXT, columnNames[1], true),
                    new Field(SQLtype.TEXT, columnNames[2]),
                    new Field(SQLtype.TEXT, columnNames[3]),
                    new Field(SQLtype.TEXT, columnNames[4], true),
                    new Field(SQLtype.TEXT, columnNames[5])}));
            lastID = 1;
        }
    }

    static public void SaveRemindersForPlayer(String player_name) {
        ArrayList<AReminder> playersRem = remList.stream().filter(aReminder -> (aReminder.getter == player_name && !aReminder.isInDatabase))
                .collect(Collectors.toCollection(ArrayList::new));
        for (AReminder ar : playersRem) {
            QueryMaster.Insert(table, new rec[]
                    {new rec(columnNames[0], Integer.toString(ar.id)),
                            new rec(columnNames[1], ar.getter, true),
                            new rec(columnNames[2], ar.sender, true),
                            new rec(columnNames[3], ar.reminder, true),
                            new rec(columnNames[4], ar.isApproval ? "true" : "false", true),
                            new rec(columnNames[5], ar.ERAction == null ? "" : ar.ERAction.toString(), true)}, null);
            ar.isInDatabase = true;
            //remList.remove(ar); или делать обращение к БД, когда игрок заходит? Тогда все оффлайновые должны сохраняться в БД и не попадать в этот список
        }
    }
        //для создания извне
    public AReminder(String getter, String sender, String remind, boolean isApproval, ERemindersAction eraction) {
        Optional<AReminder> opt = remList.stream().filter(aReminder -> (aReminder.getter == getter))
                .filter(aReminder -> aReminder.sender == sender && aReminder.reminder == remind)
                .findFirst();
        if (opt.isEmpty()) { //защита от дублирования уже созданных запросов
            this.sender = sender;
            if (eraction != null)
                ERAction = eraction;
            if (APlayer.list.get(getter) == null)
                throw new NullPointerException("Нет игрока c именем " + ChatColor.AQUA + getter);
            this.getter = getter;
            reminder = remind;
            this.isApproval = isApproval;
            id = ++lastID;
            remList.add(this); //добавляем в список напоминалок
        } else isInDatabase = opt.get().isInDatabase;
        if (Bukkit.getPlayer(this.getter).isOnline()) {
            AReminder.Send(opt.isPresent() ? opt.get() : this);
        } else if (!isInDatabase) {
            QueryMaster.Insert(table, new rec[]
                    {new rec(columnNames[0], opt.isPresent() ? Integer.toString(opt.get().id) : Integer.toString(id)),
                            new rec(columnNames[1], opt.isPresent() ? opt.get().getter : this.getter, true),
                            new rec(columnNames[2], opt.isPresent() ? opt.get().sender : this.sender, true),
                            new rec(columnNames[3], opt.isPresent() ? opt.get().reminder : reminder, true),
                            new rec(columnNames[4], opt.isPresent() ? (opt.get().isApproval ? "true" : "false" ) : (this.isApproval ? "true" : "false"), true),
                            new rec(columnNames[5], opt.isPresent() ? (opt.get().ERAction == null ? "" : opt.get().ERAction.toString()) :
                                    (ERAction == null ? "" : ERAction.toString()), true)}, null);
            isInDatabase = true;
        }
    }

    private AReminder(int _id, String getter, String sender, String remind, boolean isApproval, ERemindersAction eraction) {
        this.sender = sender;
        if (eraction != null)
            ERAction = eraction;
        this.getter = getter;
        reminder = remind;
        this.isApproval = isApproval;
        id = _id;
        isInDatabase = true;
    }
}