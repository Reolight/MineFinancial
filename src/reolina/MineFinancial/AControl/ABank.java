package reolina.MineFinancial.AControl;

import reolina.MineFinancial.QueryMasterConstructor.Field;
import reolina.MineFinancial.QueryMasterConstructor.QueryMaster;
import reolina.MineFinancial.QueryMasterConstructor.SQLtype;
import reolina.MineFinancial.definition.Type;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class ABank extends account implements IBalance{ //def for bank(?)
    private static ABank instance = null;
    static Logger log = Logger.getLogger("Minecraft");
    static private final String table = "bank";
    static private final String[] columnName = {"id", "emission", "balance"};
    Type OwnerType = Type.bank;
    private int UpdateBalance()
    {
        try{
            QueryMaster updBalance = new QueryMaster(QueryMaster.QueryType.UPDATE, "bank");
            updBalance.AddValue("balance",balance.toString());
            updBalance.AddWhere("id = 1");
            updBalance.Execute();
            return 0;
        }catch (Exception ex){
            log.info(ex.toString());
            return 200;
        }
    }

    public boolean MoneyEmission(BigDecimal emission) throws SQLException {
        QueryMaster newEmission = new QueryMaster(QueryMaster.QueryType.INSERT, "bank");
        newEmission.AddValue("emission", emission.toString());
        newEmission.AddValue("balance", balance.add(emission).toString());
        try{
            newEmission.Execute();
            balance = balance.add(emission);
            UpdateBalance();
            return true;
        }catch (SQLException ex)
        {
            log.info(ex+"\n"+ex.getStackTrace());
            return false;
        }
    }

    @Override public Type getOwnerType() {
        return OwnerType;
    }

    @Override public String getName(){
        return "bank";
    }
    @Override public int ChangeBalance(BigDecimal delta) {
        int res = super.ChangeBalance(delta);
        if (res > 0) return res;
        return UpdateBalance();
    }
    @Override public int SubsBalance(BigDecimal delta) {
        return ChangeBalance(delta.negate());
    }
    @Override public int AddBalance(BigDecimal delta) {
        return ChangeBalance(delta);
    }

    protected ABank() {
        try {
            QueryMaster query = new QueryMaster(QueryMaster.QueryType.SELECT, "bank");
            query.SelectFields(new String[]{columnName[2]});
            query.AddWhere("id = 1");
            ResultSet resultSet = query.ExecuteElection();
            resultSet.next();
            balance = resultSet.getBigDecimal(columnName[2]);
            if (balance == null) throw new Exception("balance is bull");
        } catch (Exception ex){
            log.info("Exception "+ex+"\n"+ex.getCause());
            QueryMaster creatingBank = new QueryMaster(QueryMaster.QueryType.CREATE, "bank");
            creatingBank.AddNewField(new Field(SQLtype.INTEGER,"id",true,true,true));
            creatingBank.AddNewField(new Field(SQLtype.REAL, "emission"));
            creatingBank.AddNewField(new Field(SQLtype.REAL, "balance", true));
            try {creatingBank.Execute();} catch (SQLException kex) {log.info(kex+"\n"+ex.getStackTrace());}
            balance = new BigDecimal(0);
            QueryMaster insBal = new QueryMaster(QueryMaster.QueryType.INSERT,"bank");
            insBal.AddValue("balance",balance.toString());
            try {insBal.Execute();} catch (SQLException kex) {log.info(kex+"\n"+ex.getStackTrace());}
        }
    }

    public static ABank getInstance()
    {
        if (instance == null)
            instance = new ABank();
        return instance;
    }
}
