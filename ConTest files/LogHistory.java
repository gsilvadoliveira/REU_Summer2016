package analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.eclipse.jdt.annotation.NonNull;

public class LogHistory implements Iterable<LogEvent>
{
    // Suppressed due to missing library annotations.
    @SuppressWarnings("null") @NonNull public static final String LS = System.lineSeparator();

    private final ArrayList<LogEvent> elements_;

    public LogHistory()
    {
        elements_ = new ArrayList<>();
    }

    public LogHistory(ArrayList<LogEvent> elements)
    {
        elements_ = elements;
    }

    public void add(LogEvent element)
    {
        elements_.add(element);
    }

    public LogHistory merge(LogHistory other)
    {
        ArrayList<LogEvent> resultEvents = new ArrayList<>(elements_);
        resultEvents.addAll(other.elements_);
        Collections.sort(resultEvents);
        return new LogHistory(resultEvents);
    }

    public LogHistoryDiff diff(LogHistory other)
    {
        ArrayList<TimelessLogEvent> elements1 = downgrade();
        ArrayList<TimelessLogEvent> elements2 = other.downgrade();

        ArrayList<TimelessLogEvent> firstOnly = new ArrayList<>();

        for (TimelessLogEvent event: elements1)
        {
            if (!elements2.remove(event))
                firstOnly.add(event);
        }
        return new LogHistoryDiff(firstOnly, elements2);
    }

    public ArrayList<TimelessLogEvent> downgrade()
    {
        ArrayList<TimelessLogEvent> result = new ArrayList<>();
        for (LogEvent element: elements_)
            result.add(element.downgrade());
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        result.append("Log Hitory:");
        result.append(LS);
        for (LogEvent event: elements_)
        {
            result.append("\t");
            result.append(event);
            result.append(LS);
        }
        result.append("End of Log History");

        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String res = result.toString();
        return res;
    }

    @Override
    public Iterator<LogEvent> iterator()
    {
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull Iterator<LogEvent> result = elements_.iterator();
        return result;
    }
}
