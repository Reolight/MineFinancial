package reolina.MineFinancial.QueryMasterConstructor;

public class rec {
    public String name;
    public String value;
    public rec (String name, String value){this.name=name;this.value=value;}
    public rec (String name, String value, boolean isText) {
        this.name = name;
        this.value = "\""+value+"\"";
    }
}
