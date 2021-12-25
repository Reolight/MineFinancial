package reolina.MineFinancial.AControl;
import reolina.MineFinancial.AControl.AMarket.AMSeller;
import reolina.MineFinancial.AControl.AMarket.AMarket;

public class AControl {
//будет заниматься инициализацией и управлением всех объектов
    static private AControl instance;
    static public ABank Bank;
    static public AMarket Market;

    static public final AControl getInstance()
    {
        if (instance == null)
            instance =  new AControl();
        return instance;
    }

    private AControl()
    {
        Bank = ABank.getInstance();
        APlayer.init();
        AClan.init();
        Market = AMarket.getInstance(); //Рынок должен грузится после игроков, кланов и банка (ибо владельцы предметов их экземпляры)
        AReminder.init();
        ATransaction.init();
        ACredit.init();
        AMSeller.init();
    }
}
