package tools;

import com.beust.jcommander.Parameter;

/**
 * Docs : http://jcommander.org/
 * @author samuelvandamme
 */
public class CommandlineValidator {
    @Parameter(description = "Website", required = true, validateWith = CommandlineValidatorUrl.class)
    public String website = new String();
    
    @Parameter(names = {"-t", "--threads" }, description = "Threads")
    public Integer threads = new Integer(10);
}
