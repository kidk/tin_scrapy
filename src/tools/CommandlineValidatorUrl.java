package tools;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Docs : http://jcommander.org/
 * @author samuelvandamme
 */
public class CommandlineValidatorUrl implements IParameterValidator {

    @Override
    public void validate(String name, String value) throws ParameterException {
        System.out.println(name + value);
        try {
            URI url = new URI(value);
        } catch (URISyntaxException ex) {
            throw new ParameterException("The first parameter should be an URL. ");
        }
        
        
    }
    
}
