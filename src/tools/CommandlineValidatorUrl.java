package tools;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Docs : http://jcommander.org/
 * @author samuelvandamme
 */
public class CommandlineValidatorUrl implements IParameterValidator {

    @Override
    public void validate(String name, String value) throws ParameterException {
        try {
            new URL(value);
        } catch (MalformedURLException ex) {
            throw new ParameterException("Parameter " + name + " should be an URL.");
        }
    }
    
}
