package reolina.MineFinancial.definition;

import reolina.MineFinancial.AControl.account;

import java.math.BigDecimal;

public class player extends account {
    String Nickname;
    Type OwnerType = Type.player;
    FRole frole;

    public player(String name, BigDecimal balance, FRole frole)
    {
        Nickname = new String(name);
        this.balance = balance;
        this.frole = frole;
    }
}
