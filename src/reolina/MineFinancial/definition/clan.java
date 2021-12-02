package reolina.MineFinancial.definition;

import reolina.MineFinancial.AControl.account;

import java.math.BigDecimal;
import java.util.Dictionary;

public class clan extends account {
    private String Name;
    Type OwnerType = Type.clan;
    Dictionary<player, CRole> playerList; //пока словарь

    public String getName() {
        return Name;
    }

    public clan(BigDecimal balance, String name, Dictionary listOfMembers) {
        this.balance = balance;
        Name = name;
        playerList = listOfMembers;
    }
}
