package utility;

import com.kivancmuslu.www.debug.Debug;

public class DBTable
{
    private final String name_;
    private final String[] columns_;

    public DBTable(String name, String... columns)
    {
        name_ = name;
        columns_ = columns;
    }

    public String getName()
    {
        return name_;
    }

    public String[] getColumns()
    {
        return columns_;
    }

    @Override
    public String toString()
    {
        return name_ + ": " + Debug.join(columns_, ", ");
    }
}
