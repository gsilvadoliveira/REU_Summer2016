package conTest;

import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNull;

import testing.Test;

public class AllTestController implements TestController
{
    @Override
    public void triggerActivated(HashMap<String, Object> triggerData)
    {
        // We don't care the trigger data, all tests will be scheduled to run again.
        Test[] tests = CDTBase.getInstance().getInitializedTests();
        for (Test test: tests)
        {
            // Suppressed due to missing library annotations.
            @SuppressWarnings("null") @NonNull Test safeTest = test;
            CDTBase.getInstance().scheduleTest(safeTest);
        }
    }
}
