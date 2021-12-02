package reolina.MineFinancial.AControl;

import reolina.MineFinancial.definition.*;
import java.math.BigDecimal;
import java.sql.Connection;

public class account implements IBalance{
    static protected final BigDecimal _zero = new BigDecimal(0);
    protected BigDecimal balance;
    public Type OwnerType;
    static protected Connection connection;
    void DisplayBalance() {} //displaying curr balance

    //static protected boolean UpdateBalance(String name) {return false; }
    public BigDecimal GetBalance() { return balance; }
    public String GetBalanceString() {return "¥"+balance; }

    public void Sub(BigDecimal subsd)
    {
        balance.subtract(subsd);
    }

    public account() {}
    public account(BigDecimal _balance)
    {
        balance = _balance;
    }

    public int ChangeBalance(BigDecimal delta) {
        return 10;
    }

    public int SubsBalance(BigDecimal delta) {
        return 10;
    }

    public int AddBalance(BigDecimal delta) {
        return 10;
    }
}
