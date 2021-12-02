package reolina.MineFinancial.QueryMasterConstructor;

import java.util.HashMap;
import java.util.Map;

public class Table {
    public Field[] fields;
    public Map<String, Field> fieldMap = new HashMap<>();
    public String Name;

    public Table(String tableName, Field[] fields){
        Name = tableName;
        this.fields = fields;
        for (Field f : fields){
            fieldMap.put(f.ColumnName, f );
        }
    }
}
