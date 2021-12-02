package reolina.MineFinancial.definition;

import reolina.MineFinancial.AControl.account;

public class deposit extends account {
    int ID;
    account Investor;
    Type InvestorType;
    double Rate; //процентная ставка вклада
    int DaysLimit;

    static void OnDayChange(deposit dep) //new day handler
    {
        if (dep.OwnerType == Type.player)
        {

        } else if (dep.OwnerType == Type.clan)
        {

        }
    }

    public deposit(account investorName, double rate, int daysLimit)
    {
        Investor = investorName;
        Rate = rate;
        DaysLimit = daysLimit;
    }
}
