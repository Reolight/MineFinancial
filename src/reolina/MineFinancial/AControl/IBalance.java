package reolina.MineFinancial.AControl;

import java.math.BigDecimal;

public interface IBalance {

    int ChangeBalance(BigDecimal delta);
    int SubsBalance(BigDecimal delta);
    int AddBalance(BigDecimal delta);
}
