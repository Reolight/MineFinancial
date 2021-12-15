package reolina.MineFinancial.AControl;


import java.util.ArrayList;
import java.util.HashMap;

public class ARequiest { //нужно ли наследование? Запрос содержит вознаграждение (денежное), что сближет его со счётом
    static HashMap<IBalance, ARequiest> requiestList = new HashMap<>();
}
