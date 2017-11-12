package app;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import utility.PropertyValidator;
import app.CDTApp.CDTType;
import app.CDTApp.ExperimentType;

import com.kivancmuslu.www.console.ArgumentType;
import com.kivancmuslu.www.console.ConsoleParser;
import com.kivancmuslu.www.process.Command;
import com.kivancmuslu.www.process.Output;
import com.kivancmuslu.www.timer.Duration;
import com.kivancmuslu.www.timer.Timer;

import conTest.CDTBase;
import conTest.SharedOperations;
import database.DBInterface;
import database.DBOperations;

public class ExperimentRunner
{
    public static final String EXPERIMENT_DATABASE = "cdt_experiment";
    // Suppressed due to missing library annotations.
    @SuppressWarnings("null") @NonNull public static final String LS = System.lineSeparator();

    private static @Nullable CDTType cdtType_;
    private static @Nullable ExperimentType experimentType_;

    public static long SECOND = 1000;
    public static long MINUTE = SECOND * 60;
    public static boolean DEBUG = false;
    public static boolean PROFILE = false;

    // Suppressed due to missing library annotations.
    @SuppressWarnings("null") public static final String FS = File.separator;

    public static final String POSTGRES_BIN_FOLDER_PROP = "postgres_bin";
    public static File POSTGRES_BIN_FOLDER = new File(System.getProperty("user.home"));

    public static final String POSTGRES_USER_PROP = "postgres_user";
    public static String POSTGRES_USER = "";

    public static final String LOCALHOST_PROP = "localhost";
    public static boolean LOCALHOST = false;

    public static final String ROOT_FOLDER_PROP = "root_folder";
    public static File ROOT_FOLDER = new File(System.getProperty("user.home"));

    public static final String SELECT_FILE_PROP = "select_file";
    public static File SELECT_FILE = new File(System.getProperty("user.home"));

    public static final String UPDATE_FILE_PROP = "update_file";
    public static File UPDATE_FILE = new File(System.getProperty("user.home"));

    public static final String EXPERIMENTS_PROP = "experiments";
    public static ArrayList<ExperimentType> EXPERIMENTS = new ArrayList<>();

    public static final String CDT_TYPES_PROP = "cdt_types";
    public static ArrayList<CDTType> CDT_TYPES = new ArrayList<>();

    public static final String UPDATE_FREQUENCY_PROP = "update_frequency";
    public static long UPDATE_FREQUENCY = MINUTE * 1;

    public static final String TIME_LIMIT_PROP = "time_limit";
    public static long TIME_LIMIT = MINUTE * 30;

    public static final String NO_SIMULATIONS_PROP = "no_simulations";
    public static int NO_SIMULATIONS = 1;

    // Suppressed due to complex program logic.
    @SuppressWarnings("null") private static Formatter writer_;
    private static @Nullable Thread cdtRunner_;

    public static void main(String[] args) throws FileNotFoundException, IOException
    {
        ConsoleParser parser = new ConsoleParser("CDT Experiment Runner");
        parser.addArgument(ArgumentType.FILE, "configuration file");
        parser.assertUsage(args);

        File file = parser.getValidatedFile(0);
        Properties props = new Properties();
        try (InputStream is = new FileInputStream(file))
        {
            props.load(is);
        }

        PropertyValidator validator = new PropertyValidator(props);
        POSTGRES_BIN_FOLDER = validator.validateExistingFolderProperty(POSTGRES_BIN_FOLDER_PROP);
        POSTGRES_USER = validator.validateStringProperty(POSTGRES_USER_PROP);
        LOCALHOST = validator.validateBooleanProperty(LOCALHOST_PROP);
        ROOT_FOLDER = validator.validateExistingFolderProperty(ROOT_FOLDER_PROP);
        SELECT_FILE = validator.validateExistingFileProperty(SELECT_FILE_PROP);
        UPDATE_FILE = validator.validateExistingFileProperty(UPDATE_FILE_PROP);

        UPDATE_FREQUENCY = validator.validateInteger(UPDATE_FREQUENCY_PROP) * SECOND;
        TIME_LIMIT = validator.validateInteger(TIME_LIMIT_PROP) * MINUTE;
        NO_SIMULATIONS = validator.validateInteger(NO_SIMULATIONS_PROP);

        validator.validateEnumArrayProperty(EXPERIMENTS_PROP, ExperimentType.values(), EXPERIMENTS);
        validator.validateEnumArrayProperty(CDT_TYPES_PROP, CDTType.values(), CDT_TYPES);

        // Suppressed due to complex program logic.
        @SuppressWarnings("null") @NonNull String dbName =
            DBInterface.getInstance().getDatabaseName();

        Timer.start();

        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String selectPoolPath = SELECT_FILE.getAbsolutePath();
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String updatePoolPath = UPDATE_FILE.getAbsolutePath();

        File selectPool = new File(selectPoolPath);
        File updatePool = new File(updatePoolPath);

        File outputFolder = new File(ROOT_FOLDER, "cdt_perf_experiments");
        if (!outputFolder.exists())
            outputFolder.mkdirs();

        for (int run = 0; run < EXPERIMENTS.size(); run++)
        {
            experimentType_ = EXPERIMENTS.get(run);
            cdtType_ = CDT_TYPES.get(run);
            runExperiment(dbName, selectPool, updatePool, outputFolder);
        }

        long elapsed = Timer.stop();
        println("The whole experiment took: " + elapsed + " ns ("
                + Duration.toHumanReadableString(elapsed) + ")");
        writer_.close();
    }

    private static void runExperiment(String dbName, File selectPool, File updatePool,
                                      File outputFolder)
    {
        long runtime = TIME_LIMIT / MINUTE;
        long update = UPDATE_FREQUENCY / MINUTE;
        String logFileName =
            "cdt(" + cdtType_ + ")_experiment(" + experimentType_ + ")" + runtime + "m" + "_"
                + update + "m" + ".txt";
        String logFileNameInitial = logFileName;

        File logFile = new File(outputFolder, logFileName);
        int counter = 0;
        while (logFile.exists())
        {
            // Suppressed due to missing library annotations.
            @SuppressWarnings("null") @NonNull String temp =
                logFileNameInitial.replace(".txt", "_" + counter + ".txt");
            logFileName = temp;
            logFile = new File(outputFolder, logFileName);
            counter++;
        }
        initializeWriter(logFile);

        printConfiguration(selectPool.getAbsolutePath(), updatePool.getAbsolutePath());

        // Create simulation workload if not exists:
        randomize(selectPool, new File(outputFolder, "simulation_select_pool.txt"));
        randomize(updatePool, new File(outputFolder, "simulation_update_pool.txt"));

        Timer.start();
        println("Starting experiment: cdt type = " + cdtType_ + ", experiment type = "
                + experimentType_);

        createExperimentDatabase(dbName);
        warmUp();
        runCDTAsync(outputFolder, logFileName, cdtType_, experimentType_);
        runSimulations(outputFolder, logFileName);
        joinCDTThread();

        switchToDatabase(null);
        dropExperimentDatabase();
        long elapsed = Timer.stop();
        println("Run took: " + elapsed + " ns (" + Duration.toHumanReadableString(elapsed) + ")"
                + LS);
    }

    private static void randomize(File input, File output)
    {
        if (!output.exists())
        {
            try (Formatter writer = new Formatter(output))
            {
                ArrayList<String> queries = SharedOperations.readQueries(input);
                Collections.shuffle(queries);
                for (String query: queries)
                    writer.format("%s%n%s%n", query, ";");
            }
            catch (FileNotFoundException e)
            {
                println("Cannot randomize queries...");
                printException(e);
                System.exit(-1);
            }
        }
    }

    private static void printConfiguration(@Nullable String selectPoolPath,
                                           @Nullable String updatePoolPath)
    {
        // Print configuration...
        println("Started experiment. Configuration:");
        println("Select pool path = " + selectPoolPath);
        println("Update pool path = " + updatePoolPath);
        println("CDT Type = " + cdtType_);
        println("Experiment type = " + experimentType_);
    }

    private static void initializeWriter(File file)
    {
        try
        {
            writer_ = new Formatter(file);
        }
        catch (FileNotFoundException e)
        {
            System.out.println("Cannot create the log writer.");
            e.printStackTrace(System.out);
            System.exit(-1);
        }
    }

    private static void runSimulations(File outputFolder, String logFilePath)
    {
        Thread[] simulationThreads = new Thread[NO_SIMULATIONS];
        for (int a = 0; a < NO_SIMULATIONS; a++)
        {
            String simulationName = "simulation_" + (a+1);
            final File selectPool = new File(outputFolder, "simulation_select_pool.txt");
            final File updatePool = new File(outputFolder, "simulation_update_pool.txt");
            final File queryRunnerOutputFile = new File(outputFolder, simulationName + "_" + logFilePath);
            simulationThreads[a] = new Thread(simulationName)
            {
                @Override
                public void run()
                {
                    try
                    {
                        QueryRunnerApp.run(selectPool, updatePool, queryRunnerOutputFile, TIME_LIMIT,
                                           UPDATE_FREQUENCY);
                    }
                    catch (FileNotFoundException | SQLException e)
                    {
                        println("Cannot run " + getName() + ".");
                        printException(e);
                    }
                }
            };
        }
        
        for (Thread simulationThread: simulationThreads)
        {
            println("Running " + simulationThread.getName() + " async...");
            simulationThread.start();
        }
        
        println("Joining simulations...");
        for (Thread simulationThread: simulationThreads)
        {
            try
            {
                simulationThread.join();
                println(simulationThread.getName() + " completed.");
            }
            catch (InterruptedException e)
            {
                println("Cannot join " + simulationThread.getName() + ".");
                printException(e);
            }
        }
    }

    @SuppressWarnings("unused")
    private static String enclose(String filePath, String prefix, String postfix)
    {
        return prefix + "_" + (filePath.replace(".txt", "_" + postfix + ".txt"));
    }

    private static void joinCDTThread()
    {
        Thread cdtRunner = cdtRunner_;
        if (cdtRunner != null)
        {
            println("Joining CDT thread.");
            CDTApp.kill();
            try
            {
                cdtRunner.join();
            }
            catch (InterruptedException e)
            {
                println("Cannot join to the CDT thread.");
                printException(e);
            }
            println("CDT thread completed with success." + LS);
        }
    }

    private static void warmUp()
    {
        if (DEBUG)
            return;

        try
        {
            println("Warming up...");
            String query = "SELECT * from lineitem";
            try (ResultSet resultSet = DBOperations.executeQuery(query))
            {
                while (resultSet.next())
                {
                    @SuppressWarnings("unused") int row = resultSet.getRow();
                }
            }
            finally
            {
                DBOperations.commit();
            }
            println("Completed warming up." + LS);
        }
        catch (SQLException e)
        {
            println("Cannot warmup.");
            println(e);
        }
    }

    private static void createExperimentDatabase(String dbName)
    {
        Timer.start();
        dropExperimentDatabase();
        println("Copying the database into a temporary one.");
        try
        {
            Command command;
            String osName = System.getProperty("os.name");
            if (osName.toLowerCase().contains("win"))
                command =
                    new Command(POSTGRES_BIN_FOLDER + FS + "createdb.exe", "-U", POSTGRES_USER,
                                "-T", dbName, EXPERIMENT_DATABASE);
            else
                command =
                    new Command(POSTGRES_BIN_FOLDER + FS + "createdb", "-U", POSTGRES_USER, "-T",
                                dbName, EXPERIMENT_DATABASE);

            if (LOCALHOST)
                command.addArguments("-h", "localhost");

            Output result = command.execute(false);
            String stdErr = result.getStdErr();
            if (!stdErr.equals(""))
            {
                println("There was an error while creating the experiment database: " + stdErr);
            }
            String stdOut = result.getStdOut();
            if (!stdOut.trim().equals(""))
                println(stdOut + LS);
        }
        catch (IOException | InterruptedException e)
        {
            println("Cannot create the experiment database.");
            printException(e);
            System.exit(-1);
        }
        finally
        {
            long elapsed = Timer.stop();
            println("Creating experiment database took: " + Duration.toHumanReadableString(elapsed)
                    + LS);
        }

        switchToDatabase(EXPERIMENT_DATABASE);
    }

    private static void switchToDatabase(@Nullable String dbName)
    {
        try
        {
            DBInterface.getInstance().changeDatabase(dbName);
            println("Switched database connection to: " + dbName + LS);
        }
        catch (SQLException e)
        {
            println("Cannot connect to database: " + dbName);
            printException(e);
            System.exit(-1);
        }
    }

    private static void dropExperimentDatabase()
    {
        try
        {

            Command command;
            String osName = System.getProperty("os.name");
            if (osName.toLowerCase().contains("win"))
                command =
                    new Command(POSTGRES_BIN_FOLDER + FS + "dropdb.exe", "-U", POSTGRES_USER,
                                "--if-exists", EXPERIMENT_DATABASE);
            else
                command =
                    new Command(POSTGRES_BIN_FOLDER + FS + "dropdb", "-U", POSTGRES_USER,
                                "--if-exists", EXPERIMENT_DATABASE);

            if (LOCALHOST)
                command.addArguments("-h", "localhost");

            Output result = command.execute(false);
            String stdErr = result.getStdErr();
            if (!stdErr.equals(""))
            {
                println("There was an error while dropping the experiment database: " + stdErr);
            }
            String stdOut = result.getStdOut();
            if (!stdOut.trim().equals(""))
            {
                println(stdOut + LS);
            }
        }
        catch (IOException | InterruptedException e)
        {
            println("Cannot drop the experiment database.");
            printException(e);
        }
    }

    private static void printException(Exception e)
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        e.printStackTrace(ps);
        println(os);
    }

    private static void runCDTAsync(File outputFolder, String logFilePath,
                                    final @Nullable CDTType cdtType,
                                    final @Nullable ExperimentType experimentType)
    {
        if (cdtType == null)
            return;

        File cdtOutputFile = new File(outputFolder, "cdt_" + logFilePath);
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull final String cdtOutputPath =
            cdtOutputFile.getAbsolutePath();

        println("Running CDT asynchronously...");
        CDTApp.setup(cdtType, experimentType, cdtOutputPath);
        Thread cdtRunner = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    CDTApp.run();
                }
                catch (InterruptedException e)
                {
                    println("Cannot run CDT.");

                }
            }
        };
        cdtRunner_ = cdtRunner;
        cdtRunner.start();

        try
        {
            println("Waiting until CDT is intiialized...");
            CDTBase.getInstance().waitUntilInitialization();
            println("CDT is initialized." + LS);
        }
        catch (InterruptedException e)
        {
            println("Could not wait until CDT is initialized.");
            printException(e);
        }
    }

    static void println(Object message)
    {
        System.out.println(message);
        writer_.format("%s%n", message);
        writer_.flush();
    }
}
