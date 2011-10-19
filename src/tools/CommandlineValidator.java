package tools;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.internal.Lists;
import java.util.List;

/**
 * Docs : http://jcommander.org/
 * @author samuelvandamme
 */
public class CommandlineValidator {
    @Parameter(description = "Website", required = true)
    public List<String> website = Lists.newArrayList();
    
    @Parameter(names = {"-t", "--threads"}, description = "Threads")
    public Integer threads = new Integer(10);
}
