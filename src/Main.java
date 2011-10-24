import domein.MainThread;
import com.beust.jcommander.JCommander;
import tools.CommandlineValidator;

/**
 *
 * @author samuelvandamme
 */
//test
public class Main {

    public static void main(String[] args) {
        new Main(args);
    }

    public Main(String[] args) {
        // Controle van commandline parameters
        CommandlineValidator validator = new CommandlineValidator();
        new JCommander(validator, args);

        // Main loop
        MainThread algo = new MainThread(validator.website.get(0));
        
        // Set commandline options
        algo.setThreads(validator.threads);
        algo.setDataDir(validator.dir);
        
        // Start
        algo.start();
    }
}
