package reolina.MineFinancial.QueryMasterConstructor;

import java.util.Locale;

public class Field {
    public SQLtype sqLtype;
    String ColumnName;
    boolean isPrimary;
    boolean isAutoinc;
    boolean isNotNull;

    public Field(SQLtype _type, String valName) {
        sqLtype = _type;
        ColumnName = valName.toLowerCase(Locale.ROOT);
    }
    public Field(SQLtype _type, String valName, boolean isNotNull){
        this(_type, valName);
        this.isNotNull = isNotNull;
    }
    public Field(SQLtype _type, String valName, boolean isNotNull, boolean Primary){
        this(_type, valName, isNotNull);
        isPrimary = Primary;
        if (isPrimary && sqLtype == SQLtype.INT)
            sqLtype = SQLtype.INTEGER;
    }
    public Field(SQLtype _type, String valName, boolean isNotNull, boolean Primary, boolean isAutoincrement){
        this(_type, valName, isNotNull, Primary);
        isAutoinc = isAutoincrement;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(ColumnName +" "+sqLtype.toString());
        if (isNotNull)
            sb.append(" NOT NULL");
        if (isPrimary)
            sb.append(" PRIMARY KEY");
        if (isAutoinc)
            sb.append(" AUTOINCREMENT");
        return sb.toString();
    }
}
