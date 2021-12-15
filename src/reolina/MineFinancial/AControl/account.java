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
    @Override public BigDecimal getBalance() { return balance; }
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

    @Override public String getName() {
        return null;
    }
    @Override public Type getOwnerType(){
        return null;
    }

    @Override public int ChangeBalance(BigDecimal delta) {
        if (balance.add(delta).compareTo(_zero) < 0)
            return 100;
        balance = balance.add(delta);
        return 0;
    }
    @Override public int SubsBalance(BigDecimal delta) {
        delta = delta.negate();
        int res = ChangeBalance(delta);
        return res;
    }
    @Override public int AddBalance(BigDecimal delta) {
        return ChangeBalance(delta);
    }
}
