package reolina.MineFinancial.AControl;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.logging.Logger;

public class AControl {
//будет заниматься инициализацией и управлением всех объектов
    static private AControl instance;
    static public ABank Bank;

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
        AReminder.init();
        ATransaction.init();
    }
}
