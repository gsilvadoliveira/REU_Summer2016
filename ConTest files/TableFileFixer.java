package app;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Scanner;

import com.kivancmuslu.www.file.DirectoryOperations;
import com.kivancmuslu.www.file.FileFilter;

public class TableFileFixer
{
    public static void main(String[] args) throws FileNotFoundException
    {
        File rootFolder = new File("/homes/gws/kivanc/cdt/tpch_output/s1");

        ArrayList<File> files = DirectoryOperations.getAllFiles(rootFolder, new FileFilter()
        {
            @Override
            public boolean includeFile(File file)
            {
                return file.getName().endsWith(".tbl");
            }
        });

        for (File file: files)
        {
            System.out.println("Processing: " + file.getAbsolutePath());

            try (Scanner reader = new Scanner(file);
                 Formatter writer = new Formatter(new File(file.getParent(), "psql_"
                                                                             + file.getName())))
            {
                while (reader.hasNext())
                {
                    String line = reader.nextLine();
                    if (line.endsWith("|"))
                        line = line.substring(0, line.length() - "|".length());
                    writer.format("%s%n", line);
                }
            }

            System.out.println("Completed processing file: " + file.getAbsolutePath());
        }
    }
}
