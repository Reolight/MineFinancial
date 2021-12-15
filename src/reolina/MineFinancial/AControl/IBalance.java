package reolina.MineFinancial.AControl;

import reolina.MineFinancial.definition.Type;

import java.math.BigDecimal;

public interface IBalance {
    Type getOwnerType(); //возвращает тип владельца счёта: банк, клан, игрок
    String getName();
    int ChangeBalance(BigDecimal delta);
    int SubsBalance(BigDecimal delta);
    int AddBalance(BigDecimal delta);
    BigDecimal getBalance();
}
