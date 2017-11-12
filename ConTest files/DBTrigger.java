package database;

public class DBTrigger
{
    private final String name_;
    private final String tableName_;
    
    public DBTrigger(String name, String tableName)
    {
        name_ = name;
        tableName_ = tableName;
    }

    public String getName()
    {
        return name_;
    }

    public String getTableName()
    {
        return tableName_;
    }
}
