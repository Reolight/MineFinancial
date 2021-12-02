package reolina.MineFinancial.definition;

import java.math.BigDecimal;

public interface ICreditListener {
    void OnPaid(BigDecimal payment);
}
