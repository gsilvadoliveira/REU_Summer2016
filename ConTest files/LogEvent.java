package analysis;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jdt.annotation.NonNull;

public class LogEvent implements Comparable<LogEvent>
{
    @Override
    public int compareTo(LogEvent other)
    {
        return (int) (timestamp_.getTime() - other.timestamp_.getTime());
    }

    private final Date timestamp_;
    private final String action_;
    private final String information_;

    private LogEvent(Date timestamp, String action, String information)
    {
        timestamp_ = timestamp;
        action_ = action;
        information_ = information;
    }

    public Date getTimestamp()
    {
        return timestamp_;
    }

    public String getAction()
    {
        return action_;
    }

    public String getInformation()
    {
        return information_;
    }

    public static LogEvent parse(String logLine) throws ParseException
    {
        if (!logLine.contains("!!!"))
            throw new RuntimeException("Log line must contain '!!!'");

        String temp = logLine.substring(logLine.indexOf("!!!") + "!!!".length()).trim();
        String[] parts = temp.split(" @ ");
        if (parts.length != 2 && parts.length != 3)
            throw new RuntimeException("Log line needs to contain 2 or 3 parts.");

        String dateString = parts[0].trim();
        String action = "";
        String information;
        if (parts.length == 2)
            information = parts[1].trim();
        else
        {
            action = parts[1].trim();
            information = parts[2].trim();
        }

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull Date timestamp = format.parse(dateString);
        // Suppressed due to missing library annotations (String.trim()).
        @SuppressWarnings("null") @NonNull String act = action;
        // Suppressed due to missing library annotations (String.trim()).
        @SuppressWarnings("null") @NonNull String inf = information;
        return new LogEvent(timestamp, act, inf);
    }

    public TimelessLogEvent downgrade()
    {
        return new TimelessLogEvent(action_, information_);
    }

    @Override
    public String toString()
    {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        return "[Event: timestamp = " + format.format(timestamp_) + ", action = " + action_
               + ", information = " + information_ + " ]";
    }
}
