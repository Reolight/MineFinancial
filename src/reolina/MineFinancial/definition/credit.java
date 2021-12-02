package reolina.MineFinancial.definition;

import reolina.MineFinancial.AControl.account;

import java.math.BigDecimal;

public class credit implements ICreditListener {
    int ID;
    private BigDecimal CreditAmount;
    private BigDecimal LeftAmount; //left to pay
    private BigDecimal PaidAmount; //Already paid amount
    private double Rate;
    private int DaysToPenalty;
    account Borrower;
    account Creditor;

    public BigDecimal getCreditAmount() {
        return CreditAmount;
    }

    public BigDecimal getLeftAmount() {
        return LeftAmount;
    }

    public double getRate() {
        return Rate;
    }

    public int getDaysToPenalty() {
        return DaysToPenalty;
    }

    public credit(double creditAmount, double rate, account borrower, account creditor){
        CreditAmount = new BigDecimal(creditAmount);
        Rate = rate;
        Borrower = borrower;
        Creditor = creditor;
        LeftAmount = new BigDecimal(creditAmount + (Rate + 1));
    }

    public credit(double creditAmount, double left, double paid, double rate, account borrower, account creditor)
    {
        this(creditAmount, rate, borrower, creditor);
        LeftAmount = new BigDecimal(left);
        PaidAmount = new BigDecimal(paid);
    }

    @Override
    public void OnPaid(BigDecimal payment) {
        LeftAmount.subtract(payment);
        PaidAmount.add(payment);
    }

    public void OnDayChange() //raising debt event
    {
        if (DaysToPenalty == 0)
            LeftAmount = new BigDecimal(LeftAmount.doubleValue() * Rate);
        else if (DaysToPenalty > 0)
            DaysToPenalty--;
    }
}
