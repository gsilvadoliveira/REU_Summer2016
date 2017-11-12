package conTest;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNull;

import database.DBOperations;

public class CDTTriggerOptimizer implements SharedConstants
{
    private final static CDTTriggerOptimizer instance_ = new CDTTriggerOptimizer();

    public static final int MAX_TRIGGER_LENGTH = 63;

    public static final boolean DEBUG_CACHE = true;

    public static CDTTriggerOptimizer getInstance()
    {
        return instance_;
    }

    // Mapping from trigger name (the name used in the database) to a set of test ids.
    private final ConcurrentHashMap<String, HashSet<String>> triggerMap_;

    // Mapping from trigger details (tableName + when) to trigger name (the name used in the
    // database).
    private final ConcurrentHashMap<String, String> triggerCache_;

    private CDTTriggerOptimizer()
    {
        triggerMap_ = new ConcurrentHashMap<>();
        triggerCache_ = new ConcurrentHashMap<>();
    }

    public void clear()
    {
        triggerMap_.clear();
        triggerCache_.clear();
    }

    public void createTrigger(String testID, TD type, String tableName) throws SQLException
    {
        String when = type.getWhen();
        String triggerDetails = tableName.toLowerCase() + when;

        if (triggerCache_.containsKey(triggerDetails))
        {
            if (DEBUG_CACHE)
                CDTBase.getInstance().logInfo("Trigger for: " + triggerDetails
                                                  + " already exists. Not re-creating it.");
            String triggerName = triggerCache_.get(triggerDetails);
            HashSet<String> testIDs = triggerMap_.get(triggerName);
            testIDs.add(testID);
        }
        else
        {
            String triggerName = type.getTriggerName();
            if (triggerName.length() > MAX_TRIGGER_LENGTH)
            {
                // Suppressed due to missing library annotations.
                @SuppressWarnings("null") @NonNull String newName =
                    triggerName.substring(0, MAX_TRIGGER_LENGTH);
                CDTBase.getInstance().logWarning("Trigger name: " + triggerName
                                                     + " is too long. Using " + newName
                                                     + " instead");
                triggerName = newName;
            }

            DBOperations.createTrigger(getInstance(), triggerName, tableName, type.getWhen(), null,
                                       CDT_TRIGGER_FUNCTION_NAME, false);

            triggerCache_.put(triggerDetails, triggerName);
            if (DEBUG_CACHE)
                CDTBase.getInstance().logInfo("Put " + triggerDetails + " = " + triggerName
                                                  + " to cache.");
            HashSet<String> testIDs = new HashSet<>();
            testIDs.add(testID);
            triggerMap_.put(triggerName, testIDs);
        }
    }

    public HashSet<String> getTestIDsAssociatedWith(String triggerName)
    {
        HashSet<String> result = triggerMap_.get(triggerName);
        if (result == null)
            throw new IllegalStateException("No tests are associated with trigger: " + triggerName
                                            + ". This should not have happened.");
        return result;
    }
}
