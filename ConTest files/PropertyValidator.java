package utility;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.eclipse.jdt.annotation.Nullable;

import com.kivancmuslu.www.debug.Debug;

public class PropertyValidator
{
    private final Properties props_;

    public PropertyValidator(Properties props)
    {
        props_ = props;
    }

    public File validateExistingFolderProperty(String property) throws IllegalArgumentException
    {
        String value = props_.getProperty(property);
        String val = validateNonNull(property, value);
        File file = new File(val);
        validateFileExists(property, file);
        if (!file.isDirectory())
            throw new IllegalArgumentException("Property (" + property + ") value is not a folder.");
        return file;
    }

    private static void validateFileExists(String property, File file)
    {
        if (!file.exists())
            throw new IllegalArgumentException("Property (" + property + ") value does not exist.");
    }

    public File validateExistingFileProperty(String property)
    {
        String value = props_.getProperty(property);
        String val = validateNonNull(property, value);
        File file = new File(val);
        validateFileExists(property, file);
        if (!file.isFile())
            throw new IllegalArgumentException("Property (" + property + ") value is not a file.");
        return file;
    }

    public String validateStringProperty(String property) throws IllegalArgumentException
    {
        String value = props_.getProperty(property);
        return validateNonNull(property, value);
    }

    public boolean validateBooleanProperty(String property)
    {
        String value = props_.getProperty(property);
        String val = validateNonNull(property, value);
        try
        {
            return Boolean.parseBoolean(val);
        }
        catch (Throwable e)
        {
            throw new IllegalArgumentException("Property (" + property
                                               + ") value is not a boolean.");
        }
    }

    private static String validateNonNull(String property, @Nullable String value)
        throws IllegalArgumentException
    {
        if (value == null)
            throw new IllegalArgumentException("Property (" + property + ") value is null.");
        return value;
    }

    public <T> void validateEnumArrayProperty(String property, T[] enumValues, List<T> result)
        throws IllegalArgumentException
    {
        String value = props_.getProperty(property);
        String val = validateNonNull(property, value);
        String[] elements = val.split(",");

        for (int a = 0; a < elements.length; a++)
        {
            String element = elements[a];
            for (T enumValue: enumValues)
            {
                if (enumValue.toString().toLowerCase().equals(element.toLowerCase()))
                    result.add(enumValue);
            }
            if (result.size() != a + 1)
            {
                result.add(null);
                if (!element.toLowerCase().equals("null"))
                    throw new IllegalArgumentException("Unknown enum value (" + element
                                                       + ") for property (" + property
                                                       + "). Permitted values = "
                                                       + Debug.join(enumValues, ", "));
            }
        }
    }

    public int validateInteger(String property)
    {
        String value = props_.getProperty(property);
        String val = validateNonNull(property, value);
        try
        {
            return Integer.parseInt(val);
        }
        catch (Throwable e)
        {
            throw new IllegalArgumentException("Property (" + property
                                               + ") value is not an integer.");
        }
    }
}
