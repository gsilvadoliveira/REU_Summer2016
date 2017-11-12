package conTest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

import com.kivancmuslu.www.debug.Debug;

public class SmartTestController implements TestController
{
    @Override
    public void triggerActivated(HashMap<String, Object> triggerData)
    {
        Object oldData = triggerData.get("old");
        if (!(oldData instanceof Map))
        {
            CDTBase.getInstance().logWarning("Broken trigger data: "
                                                 + Debug.join(triggerData, ", ")
                                                 + ". Cannot parse old trigger data.");
            return;
        }

        Object newData = triggerData.get("new");
        if (!(newData instanceof Map))
        {
            CDTBase.getInstance().logWarning("Broken trigger data: "
                                                 + Debug.join(triggerData, ", ")
                                                 + ". Cannot parse new trigger data.");
            return;
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> oData = (Map<String, Object>) oldData;
        @SuppressWarnings("unchecked")
        Map<String, Object> nData = (Map<String, Object>) newData;
        
        Object triggerName = triggerData.get("name");
        if (triggerName != null && (triggerName instanceof String))
        {
            String tName = (String) triggerName;
            HashSet<String> testIDs =
                CDTTriggerOptimizer.getInstance().getTestIDsAssociatedWith(tName);
            for (String testID: testIDs)
            {
                // Suppressed due to missing library annotations.
                @SuppressWarnings("null") @NonNull String safeTestID = testID;
                CDTBase.getInstance().scheduleTestWithID(safeTestID, oData, nData);
            }
        }
        else
            CDTBase.getInstance().logWarning("Broken trigger data: "
                                                 + Debug.join(triggerData, ", ")
                                                 + ". Does not contain key 'name'.");
    }
}
