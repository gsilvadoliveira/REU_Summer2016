package app;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.kivancmuslu.www.arrays.ArrayOperations;
import com.kivancmuslu.www.arrays.FilterOperation;
import com.kivancmuslu.www.file.FileOperations;

public class DataEntryTaskSSGenerator
{
    // Suppressed due to missing library annotations.
    @SuppressWarnings("null") public static final @NonNull Pattern TASK_SS_PATTERN = Pattern.compile(".*_\\d+");

    public static final String DATA_ROOT_PATH = "data_entry_task" + File.separator + "data_sources";

    // Magic coordinates where the actual data starts.
    public static final int X = 38;
    public static final int Y = 168;

    // Magic constant representing the row height;
    public static final int ROW_HEIGHT = 197 - Y;

    protected static final boolean DEBUG_PROCESSED_FILE = false;

    public static void main(String[] args) throws IOException
    {
        File dataRoot = new File(DATA_ROOT_PATH);
        // Suppressed due to missing library annotations.
        ArrayList<File> dataSources = new ArrayList<>();
        getFiles(dataRoot, dataSources);
        ArrayList<File> pngFiles = ArrayOperations.filter(dataSources, new FilterOperation<File>()
        {
            @Override
            public boolean shallInclude(@Nullable File file)
            {
                if (file == null)
                    return false;
                String extension = FileOperations.extension(file);
                if (extension == null || !extension.equals("png"))
                    return false;
                String baseName = FileOperations.baseName(file);
                Matcher matcher = TASK_SS_PATTERN.matcher(baseName);
                if (matcher.find())
                {
                    if (DEBUG_PROCESSED_FILE)
                        System.out.println("Skipping processed file: " + file.getName());
                    return false;
                }
                return true;
            }
        });
        Collections.sort(pngFiles, new Comparator<File>()
        {
            @Override
            public int compare(File file1, File file2)
            {
                return file1.getName().compareTo(file2.getName());
            }
        });
        System.out.println("Found " + pngFiles.size() + " data sources.");
        System.out.println();
        for (File csvFile: pngFiles)
        {
            // Suppressed due to missing library annotations.
            @SuppressWarnings("null") @NonNull File safeFile = csvFile;
            createSSFromPNG(safeFile);
        }
    }

    private static void getFiles(File root, ArrayList<File> result)
    {
        if (root.isDirectory())
        {
            for (File file: root.listFiles())
            {
                // Suppressed due to missing array element annotations.
                @SuppressWarnings("null") @NonNull File safeFile = file;
                getFiles(safeFile, result);
            }
        }
        else
            result.add(root);
    }

    private static void createSSFromPNG(File pngFile) throws IOException
    {
        String baseName = FileOperations.baseName(pngFile);
        System.out.println("Processing file: " + pngFile.getName());

        BufferedImage image = ImageIO.read(pngFile);
        int imageHeight = image.getHeight();
        int imageWidth = image.getWidth();

        int taskCounter = 0;
        for (int currentY = Y; currentY + ROW_HEIGHT <= imageHeight; currentY += ROW_HEIGHT)
        {
            File outputFile = new File(pngFile.getParentFile(), baseName + "_" + taskCounter
                                                                + ".png");
            if (!outputFile.exists())
            {
                BufferedImage taskImage = image.getSubimage(X, currentY, imageWidth - X, ROW_HEIGHT);
                ImageIO.write(taskImage, "PNG", outputFile);
                System.out.println("Created task: " + outputFile.getName());
            }
            taskCounter++;
        }
        System.out.println("Completed processing file: " + pngFile.getName());
        System.out.println();
    }
}
