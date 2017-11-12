package analysis;

import java.util.ArrayList;

public class LogHistoryDiff
{
    private final ArrayList<TimelessLogEvent> firstOnly_;
    private final ArrayList<TimelessLogEvent> secondOnly_;

    public LogHistoryDiff(ArrayList<TimelessLogEvent> firstOnly,
                          ArrayList<TimelessLogEvent> secondOnly)
    {
        firstOnly_ = firstOnly;
        secondOnly_ = secondOnly;
    }

    public ArrayList<TimelessLogEvent> getFirstOnly()
    {
        return firstOnly_;
    }

    public ArrayList<TimelessLogEvent> getSecondOnly()
    {
        return secondOnly_;
    }
}
