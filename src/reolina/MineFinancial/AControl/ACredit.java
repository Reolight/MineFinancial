package reolina.MineFinancial.AControl;

import net.minecraft.server.network.LegacyPingHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import reolina.MineFinancial.QueryMasterConstructor.*;
import reolina.MineFinancial.definition.Type;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ACredit {
    static Logger log = Logger.getLogger("Minecraft");
    static private final String table = "credits";
    static private final String[] columnNames = {"id", "borrower", "creditor_type", "creditor", "rate", "amount", "paid", "left", "days"};

    static ItemStack creditBook;

    int ID;
    static int lastID = 0;
    private BigDecimal CreditAmount;
    private BigDecimal LeftAmount; //left to pay
    private BigDecimal PaidAmount; //Already paid amount
    private BigDecimal Rate;
    private int DaysToPenalty;
    IBalance Borrower;
    IBalance Creditor;
    static public ArrayList<ACredit> availableCredits = new ArrayList<>();
    static public ArrayList<ACredit> takenCredits = new ArrayList<>();

    public BigDecimal getCreditAmount() {
        return CreditAmount;
    }

    public BigDecimal getLeftAmount() {
        return LeftAmount;
    }

    public BigDecimal getRate() {
        return Rate;
    }

    public int getDaysToPenalty() {
        return DaysToPenalty;
    }

    static public void init(){
        try{
            ResultSet rs = QueryMaster.Select(table, null, null);
            while (rs.next()){
                ACredit tempCredit = new ACredit(rs.getInt(columnNames[0]), (rs.getString(columnNames[1])),
                        rs.getString(columnNames[2]), rs.getString(columnNames[3]), rs.getBigDecimal(columnNames[4]),
                        rs.getBigDecimal(columnNames[5]), rs.getBigDecimal(columnNames[6]), rs.getBigDecimal(columnNames[7]),
                        rs.getInt(columnNames[8]));
                lastID = tempCredit.ID;
                if (tempCredit.Borrower == null) {
                    availableCredits.add(tempCredit);
                    continue;
                }
                if (tempCredit.LeftAmount.compareTo(new BigDecimal(0)) != 0){
                    takenCredits.add(tempCredit);
                }
            }
        } catch (Exception ex){
            log.warning("ERR credit reader: "+ex+"\n"+ex.getStackTrace());
            QueryMaster.Create(new Table(table, new Field[]
                    {new Field(SQLtype.INTEGER, columnNames[0], true, true),
                    new Field(SQLtype.TEXT, columnNames[1]),
                    new Field(SQLtype.TEXT, columnNames[2], true),
                    new Field(SQLtype.TEXT, columnNames[3], true),
                    new Field(SQLtype.REAL, columnNames[4], true),
                    new Field(SQLtype.REAL, columnNames[5], true),
                    new Field(SQLtype.REAL, columnNames[6]),
                    new Field(SQLtype.REAL, columnNames[7]),
                    new Field(SQLtype.INT, columnNames[8])}));
        }
    }

    private ACredit(int id, String borrower, String creditorType, String creditor, BigDecimal rate,  BigDecimal creditAmount, BigDecimal paid, BigDecimal left, int days)
    {
        this.ID = id;
        Borrower = APlayer.list.get(borrower);
        Creditor = Type.valueOf(creditorType).ordinal() == 1 ? APlayer.list.get(creditor) : AClan.clans.get(creditor);
        Rate = rate;
        CreditAmount = creditAmount;
        PaidAmount = paid;
        LeftAmount = left;
        DaysToPenalty = days;
    }

    public void TakeCredit(APlayer borrower){
        Borrower = borrower;
        PaidAmount = new BigDecimal(0);
        LeftAmount = CreditAmount.multiply(Rate);
        QueryMaster.Update(table, new rec[]{
                new rec(columnNames[1], borrower.getName(), true),
                new rec(columnNames[6], PaidAmount.toString()),
                new rec(columnNames[7], LeftAmount.toString())
        }, new rec(columnNames[0], Integer.toString(ID)));
        borrower.AddBalance(CreditAmount);
        Bukkit.getPlayer(borrower.getName()).sendMessage(ChatColor.BLUE+Integer.toString(ID)+"Взят кредит на сумму "+ChatColor.GOLD+" ¥"+CreditAmount+ChatColor.BLUE+
                " и ставкой "+ChatColor.AQUA+(Rate.subtract(BigDecimal.ONE).multiply(new BigDecimal(100)))+"%");
        availableCredits.remove(this);
        takenCredits.add(this);
    }

    public int TakeCredit(APlayer borrower, BigDecimal creditAmount) { //на случай, если игрок хочет взять часть денег из доступного кредита
        if (creditAmount.compareTo(CreditAmount) == 0)
            TakeCredit(borrower);
        if (creditAmount.compareTo(CreditAmount) == -1)
            new ACredit(this, borrower, creditAmount);
        if (creditAmount.compareTo(CreditAmount) == 1)
            return 103;
        return 0;
    }

    static public ACredit getAvailableInstanceByID(int id){
        return availableCredits.stream().filter(aCredit -> aCredit.ID == id).findFirst().get();
    }
    static public ACredit getTakenInstanceByID(int id){
        return takenCredits.stream().filter(aCredit -> aCredit.ID == id).findFirst().get();
    }
    public ACredit(IBalance creditor, BigDecimal creditAmount, BigDecimal rate, int days){
        //if (rate == null) rate == 1.0;
        log.info("Creditor: "+creditor.getName()+". amount: "+creditAmount+". rate: "+rate+". days: "+days);
        if (creditor.getBalance().compareTo(creditAmount) < 0) {
            if (!creditor.getName().equalsIgnoreCase("bank")) {
                Bukkit.getPlayer(creditor.getOwnerType() == Type.player ? creditor.getName() : AClan.clans.get(creditor.getName()).clanLeader)
                        .sendMessage(ChatColor.RED + "Невозможно создать кредит, у не хватает средств");
            }
        }
        else {
            //log.info("cheking available credits...");
            Optional<ACredit> opt = availableCredits.stream()
                    .filter(aCredit -> (aCredit.Creditor == creditor && aCredit.Rate.compareTo(rate) == 0 && aCredit.DaysToPenalty == days && aCredit.Borrower == null))
                    .findFirst();
            //log.info("Is present already?: "+(opt.isPresent()? "true":"false"));
            if (opt.isEmpty()) {
                this.ID = ++lastID;
                Creditor = creditor;
                Rate = (rate.compareTo(BigDecimal.ZERO) == 0 ? new BigDecimal("1.0") : rate);
                CreditAmount = creditAmount;
                Creditor.SubsBalance(CreditAmount);
                DaysToPenalty = days;
                QueryMaster.Insert(table, new rec[]
                        {new rec(columnNames[0], Integer.toString(ID)),
                                new rec(columnNames[2], Creditor.getOwnerType().toString(), true),
                                new rec(columnNames[3], Creditor.getName(), true),
                                new rec(columnNames[4], Rate.toString()),
                                new rec(columnNames[5], CreditAmount.toString()),
                                new rec(columnNames[8], Integer.toString(DaysToPenalty))}, null);
                availableCredits.add(this);
                if (!creditor.getName().equalsIgnoreCase("bank"))
                    Bukkit.getPlayer(creditor.getOwnerType() == Type.player ? creditor.getName() : AClan.clans.get(creditor.getName()).clanLeader)
                                .sendMessage(ChatColor.BLUE+Integer.toString(ID)+": кредит на сумму "+ChatColor.GOLD+" ¥"+CreditAmount+ChatColor.BLUE+
                                        " и ставкой "+ChatColor.AQUA+(Rate.subtract(BigDecimal.ONE).multiply(new BigDecimal(100)))+"%"+ChatColor.BLUE+" создан");
            } else {
                opt.get().CreditAmount = opt.get().CreditAmount.add(creditAmount);
                QueryMaster.Update(table, new rec[] { new rec(columnNames[5], opt.get().CreditAmount.toString())},
                        new rec(columnNames[0], Integer.toString(opt.get().ID)));
                if (!creditor.getName().equalsIgnoreCase("bank"))
                    Bukkit.getPlayer(creditor.getOwnerType() == Type.player ? creditor.getName() : AClan.clans.get(creditor.getName()).clanLeader)
                        .sendMessage(ChatColor.BLUE+"Выделено к кредиту: "+ChatColor.GOLD+" ¥"+creditAmount);
            }
        }
    }
    private ACredit(ACredit aCreditToDivide, IBalance borrower, BigDecimal creditAmount){
            //делим кредит, второй в новую ячейку
        this.ID = ++lastID;
        Creditor = aCreditToDivide.Creditor;
        Rate = aCreditToDivide.Rate;
        CreditAmount = creditAmount;
        aCreditToDivide.CreditAmount = aCreditToDivide.CreditAmount.subtract(creditAmount); //снимаем с общего доступного кредита часть денеш
        DaysToPenalty = aCreditToDivide.DaysToPenalty;
            //теперь отдаём кредит
        Borrower = borrower;
        PaidAmount = new BigDecimal(0);
        LeftAmount = CreditAmount.multiply(Rate);
        borrower.AddBalance(CreditAmount);
        Bukkit.getPlayer(borrower.getName()).sendMessage(ChatColor.BLUE+Integer.toString(ID)+"Взят кредит на сумму "+ChatColor.GOLD+" ¥"+CreditAmount+ChatColor.BLUE+
                " и ставкой "+ChatColor.AQUA+(Rate.subtract(BigDecimal.ONE).multiply(new BigDecimal(100)))+"%");
        QueryMaster.Update(table, new rec[] {new rec(columnNames[5], aCreditToDivide.CreditAmount.toString())},
                new rec(columnNames[0], Integer.toString(aCreditToDivide.ID)));
        QueryMaster.Insert(table, new rec[]
                {new rec(columnNames[0], Integer.toString(ID)),
                        new rec(columnNames[1], Borrower.getName(), true),
                        new rec(columnNames[2], Creditor.getOwnerType().toString(), true),
                        new rec(columnNames[3], Creditor.getName(), true),
                        new rec(columnNames[4], Rate.toString()),
                        new rec(columnNames[5], CreditAmount.toString()),
                        new rec(columnNames[6], PaidAmount.toString()),
                        new rec(columnNames[7], LeftAmount.toString()),
                        new rec(columnNames[8], Integer.toString(DaysToPenalty))}, null);
        takenCredits.add(this);

    }
    public boolean isBorrowed() { return Borrower != null; }

    public void Pay(BigDecimal payment) { //запускаем транзакцию. Условия: payment не больше оставшейся уплаты.
        if (LeftAmount.compareTo(LeftAmount.subtract(payment)) < 0){
            payment = LeftAmount;
        }
        String MessageToBorrower; String MessageToCreditor;
        LeftAmount = LeftAmount.subtract(payment);
        PaidAmount = PaidAmount.add(payment);
        QueryMaster.Update(table, new rec[]
                {new rec(columnNames[6], PaidAmount.toString()),
                new rec(columnNames[7], LeftAmount.toString())}, new rec(columnNames[0], ""+ID));
        if (LeftAmount.compareTo(new BigDecimal(0))==0){
            MessageToBorrower = ChatColor.GREEN+"Заплачено "+ChatColor.GOLD+" ¥"+payment+ChatColor.GREEN+". "+
                    "Кредит погашен. Выплачено "+ChatColor.GOLD+" ¥"+PaidAmount;
            MessageToCreditor = ChatColor.DARK_GREEN+"Игрок "+ChatColor.AQUA+Borrower.getName()+ChatColor.DARK_GREEN+
                    "Заплатил "+ChatColor.GOLD+" ¥"+payment+ChatColor.DARK_GREEN+" и погасил кредит No."+ID;
            takenCredits.remove(this);
        } else {
            MessageToBorrower = ChatColor.GREEN+"Заплачено "+ChatColor.GOLD+" ¥"+payment+ChatColor.GREEN+". "+
                    "Осталось "+ChatColor.GOLD+" ¥"+LeftAmount;
            MessageToCreditor = ChatColor.DARK_GREEN+"Игрок "+ChatColor.AQUA+Borrower.getName()+ChatColor.DARK_GREEN+
                    "Заплатил "+ChatColor.GOLD+" ¥"+payment;
        }
        new ATransaction(Borrower, Creditor,payment, MessageToBorrower, MessageToCreditor);
    }

    public void OnDayChange() //raising debt event
    {
        if (DaysToPenalty == 0)
            LeftAmount = LeftAmount.multiply(Rate);
        else if (DaysToPenalty > 0)
            DaysToPenalty--;
    }

    static public boolean GetAllAvailableCredits(Player sender){
        if (availableCredits.size() == 0){
            sender.sendMessage(ChatColor.GRAY+"Доступных кредитов нет");
            return true;
        }
        for (ACredit acr : availableCredits) {
            sender.sendMessage(ChatColor.BLUE+""+ChatColor.ITALIC+acr.ID+": "+
                    (acr.Creditor.getOwnerType() == Type.player ? "игрок "+ChatColor.AQUA+ acr.Creditor.getName() :
                            (acr.Creditor.getOwnerType() == Type.clan ? "клан "+ChatColor.LIGHT_PURPLE+acr.Creditor.getName() :
                                    ChatColor.GOLD+"банк")) + ChatColor.BLUE+" предоставляет кредит в "+ChatColor.GOLD+" ¥"+acr.CreditAmount+
                                    ChatColor.BLUE+" и процентной ставкой "+ChatColor.AQUA+(acr.Rate.subtract(BigDecimal.ONE).multiply(new BigDecimal(100)))+"%");
        }
        return true;
    }

    static private String creditorTypeToString(Type type){ //просто для сокращения нижеследующего.....
        switch (type) {
            case player : return "игрока "+ChatColor.AQUA;
            case clan: return "клана "+ChatColor.LIGHT_PURPLE;
            case bank: return ChatColor.GOLD+"банка ";
            default: return null;
        }
    }

    static public void GetCreditsTakenBy(APlayer player){
        ArrayList<ACredit> taken = new ArrayList<>();
        taken = takenCredits.stream()
                .filter(aCredit -> aCredit.Borrower.getName() == player.getName())
                .collect(Collectors.toCollection(ArrayList::new));
        if (taken.size() == 0){
            Bukkit.getPlayer(player.Name).sendMessage(ChatColor.GRAY+"У вас нет непогашенных кредитов");
        } else
            for (ACredit acr : taken) {
                Bukkit.getPlayer(player.Name).sendMessage(ChatColor.GRAY+""+acr.ID+": "+ChatColor.BLUE+
                        creditorTypeToString(acr.Creditor.getOwnerType())+ (acr.Creditor.getOwnerType() != Type.bank ? acr.Creditor.getName() : "")+
                        ChatColor.BLUE+". Статус погашения: "+
                        ChatColor.GREEN+"¥"+acr.PaidAmount+"/¥"+(acr.PaidAmount.add(acr.LeftAmount))+ChatColor.GRAY+"(¥"+acr.LeftAmount+")");;
            }
    }

    static public void GetBorrowedCreditsGivenBy(IBalance player, Player sender){
        ArrayList<ACredit> given = new ArrayList<>();
        given = takenCredits.stream()
                .filter(aCredit -> (aCredit.Creditor.getName() == player.getName() && aCredit.Creditor.getOwnerType() == player.getOwnerType()))
                .collect(Collectors.toCollection(ArrayList::new));
        if (given.size() == 0) {
            sender.sendMessage(ChatColor.GRAY + "Вы не выдавали кредиты");
        } else {
            for (ACredit acr : given){
                sender.sendMessage(ChatColor.GRAY+""+acr.ID+": "+ChatColor.BLUE+"Кредит на сумму "+ChatColor.GOLD+" "+acr.CreditAmount+
                        ChatColor.BLUE+" взят игроком "+ChatColor.AQUA+acr.Borrower+ChatColor.BLUE+". Статус погашения: "+
                        ChatColor.GREEN+"¥"+acr.PaidAmount+"/¥"+(acr.PaidAmount.add(acr.LeftAmount))+ChatColor.GRAY+"(¥"+acr.LeftAmount+")");
            }
        }
    }
    static public void GetAvailableCreditsGivenBy(IBalance player, Player sender){
        ArrayList<ACredit> given = new ArrayList<>();
        given = availableCredits.stream()
                .filter(aCredit -> (aCredit.Creditor.getName() == player.getName() && aCredit.Creditor.getOwnerType() == player.getOwnerType()))
                .collect(Collectors.toCollection(ArrayList::new));
        if (given.size() == 0) {
            sender.sendMessage(ChatColor.GRAY + "Вы не выдавали кредиты");
        } else {
            for (ACredit acr : given){
                sender.sendMessage(ChatColor.GRAY+""+acr.ID+": "+ChatColor.BLUE+"Кредит на сумму "+ChatColor.GOLD+" "+acr.CreditAmount);
            }
        }
    }
}