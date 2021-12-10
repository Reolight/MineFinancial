package reolina.MineFinancial.AControl;

import reolina.MineFinancial.definition.Type;

import java.math.BigDecimal;

public interface IBalance {
    Type getOwnerType();
    String getName();
    int ChangeBalance(BigDecimal delta);
    int SubsBalance(BigDecimal delta);
    int AddBalance(BigDecimal delta);
}
