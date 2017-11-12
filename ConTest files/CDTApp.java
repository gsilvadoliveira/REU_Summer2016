package app;

import java.util.Scanner;

import org.eclipse.jdt.annotation.Nullable;

import conTest.CDT;
import conTest.CDTBase;
import conTest.SharedConstants;

public class CDTApp implements SharedConstants
{
    public static enum CDTType
    {
        TASK, NAIVE, NORMAL
    }

    public static final String NAIVE_TRIGGER_CONTROLLER = "conTest.DBTriggerController";
    public static final String STATIC_TRIGGER_CONTROLLER = "conTest.StaticTriggerController";
    public static final String NAIVE_TEST_CONTROLLER = "conTest.AllTestController";
    public static final String STATIC_TEST_CONTROLLER = "conTest.SmartTestController";

    public static String LOG_PATH = System.getProperty("user.home") + FS + "." + CDT_PREFIX
                                    + ".log";

    public static enum ExperimentType
    {
        DOUBLE_NAIVE(NAIVE_TRIGGER_CONTROLLER, NAIVE_TEST_CONTROLLER, false, false), //
        STATIC_NAIVE(STATIC_TRIGGER_CONTROLLER, NAIVE_TEST_CONTROLLER, false, false), //
        DOUBLE_STATIC(STATIC_TRIGGER_CONTROLLER, STATIC_TEST_CONTROLLER, false, false), //
        DS_COALESCE(STATIC_TRIGGER_CONTROLLER, STATIC_TEST_CONTROLLER, true, false), //
        DS_INCREMENTAL(STATIC_TRIGGER_CONTROLLER, STATIC_TEST_CONTROLLER, false, true), //
        DS_FULL(STATIC_TRIGGER_CONTROLLER, STATIC_TEST_CONTROLLER, true, true);

        String triggerController_;
        String testController_;
        boolean coalesceTests_;
        boolean incrementalTests_;

        ExperimentType(String triggerController, String testController, boolean coalesceTests,
                       boolean incrementalTests)
        {
            triggerController_ = triggerController;
            testController_ = testController;
            coalesceTests_ = coalesceTests;
            incrementalTests_ = incrementalTests;
        }

        public void setup()
        {
            CDT.TRIGGER_CONTROLLER_CLASS = triggerController_;
            CDT.TEST_CONTROLLER_CLASS = testController_;
            CDT.COALESCE_TESTS = coalesceTests_;
            CDT.INCREMENTAL_TESTS = incrementalTests_;
        }
    }

    public static CDTType APP_TYPE = CDTType.TASK;

    public static volatile boolean alive_ = false;
    public static final Object condition_ = new Object();

    public static void main(String[] args) throws InterruptedException
    {
        run();
    }

    public static void setup(@Nullable CDTType cdtType, @Nullable ExperimentType experimentType,
                             @Nullable String logPath)
    {
        if (cdtType != null)
            APP_TYPE = cdtType;

        if (logPath != null)
            LOG_PATH = logPath;

        if (APP_TYPE != CDTType.NORMAL && experimentType != null)
            throw new RuntimeException("Experiment type can only be set for full CDT.");

        CDTBase.reinitialize(experimentType);
    }

    public static void run() throws InterruptedException
    {
        Thread cdtThread = new Thread(CDTBase.getInstance());
        cdtThread.setDaemon(false);
        cdtThread.start();

        if (APP_TYPE == CDTType.TASK)
        {
            try (Scanner reader = new Scanner(System.in))
            {
                while (reader.hasNext())
                {
                    String command = reader.nextLine();
                    if (command.equals("exit"))
                        break;
                }
            }
        }
        else
        {
            alive_ = true;
            while (alive_)
            {
                synchronized (condition_)
                {
                    condition_.wait();
                }
            }
        }

        CDTBase.getInstance().kill();
        cdtThread.join();
    }

    public static void kill()
    {
        alive_ = false;
        synchronized (condition_)
        {
            condition_.notify();
        }
    }
}
