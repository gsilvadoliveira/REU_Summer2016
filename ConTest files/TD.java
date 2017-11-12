package conTest;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.kivancmuslu.www.arrays.ArrayOperations;

// TD stands for TriggerData.
public class TD
{
    private static final String TRIGGER_AFTER = "AFTER ";
    private static final String TRIGGER_ALL = "INSERT OR UPDATE OR DELETE";
    private static final String TRIGGER_INSERT_OR_DELETE = "INSERT OR DELETE";
    private static final String TRIGGER_AFTER_ALL = TRIGGER_AFTER + TRIGGER_ALL;
    private static final String TRIGGER_AFTER_INSERT_OR_DELETE = TRIGGER_AFTER
                                                                 + TRIGGER_INSERT_OR_DELETE;
    private static final String TRIGGER_UPDATE = "UPDATE";
    private static final String TRIGGER_UPDATE_OF = TRIGGER_UPDATE + " OF ";
    private static final String TRIGGER_AFTER_UPDATE_OF = TRIGGER_AFTER + TRIGGER_UPDATE_OF;
    private static final String TRIGGER_AFTER_UPDATE = TRIGGER_AFTER + TRIGGER_UPDATE;

    public static final TDT AFTER_ALL = new TDT(TRIGGER_AFTER_ALL);
    public static final TDT AFTER_INSERT_OR_DELETE = new TDT(TRIGGER_AFTER_INSERT_OR_DELETE);
    public static final TDT AFTER_UPDATE = new TDT(TRIGGER_AFTER_UPDATE);

    public static final String[] KEYWORDS = new String[] {"AFTER", "INSERT", "OR", "UPDATE",
                                                          "DELETE", "OF"};

    private final TDT template_;
    private final String triggerName_;

    TD(String namePrefix, TDT template)
    {
        template_ = template;

        StringBuilder result = new StringBuilder();
        result.append(namePrefix);
        String[] parts = getWhen().split(" ");
        for (String part: parts)
        {
            // Suppressed due to missing array element annotations.
            @SuppressWarnings("null") @NonNull String safePart = part;
            result.append("_");
            if (isKeyword(safePart))
                result.append(Character.toLowerCase(part.charAt(0)));
            else
                result.append(safePart);
        }
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String name = result.toString();
        triggerName_ = name;
    }

    private static boolean isKeyword(String part)
    {
        return ArrayOperations.contains(KEYWORDS, part);
    }

    public static TD updateOf(String namePrefix, String columnName)
    {
        return new TD(namePrefix, new TDT(TRIGGER_AFTER_UPDATE_OF + columnName));
    }

    public String getTriggerName()
    {
        return triggerName_;
    }

    @Override
    public int hashCode()
    {
        return triggerName_.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object object)
    {
        if (object instanceof TD)
            return equals((TD) object);
        return false;
    }

    public boolean equals(TD other)
    {
        return triggerName_.equals(other.triggerName_);
    }

    public String getWhen()
    {
        return template_.getWhen();
    }

    // TDT stands for TriggerDataTemplate
    public static class TDT
    {
        private final String when_;

        public TDT(String when)
        {
            when_ = when;
        }

        String getWhen()
        {
            return when_;
        }

        public TD toTD(String namePrefix)
        {
            return new TD(namePrefix, this);
        }
    }
}
