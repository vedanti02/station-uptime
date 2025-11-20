import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        setupFileLogger();

        if (args.length != 1) {
            System.out.println("ERROR");
            return;
        }

        try {
            InputParsingValidation parser = new InputParsingValidation();
            parser.parse(args[0]);

            StationUptime uptime = new StationUptime(parser);
            uptime.computeAndPrint();

        } catch (Exception e) {
            logger.severe("Fatal Error: " + e.getMessage());
            System.out.println("ERROR");
        }
    }

    private static void setupFileLogger() {
        try {
            File logDir = new File("logs");
            if (!logDir.exists()) logDir.mkdir();

            // File name logs/log_2025-01-17_14-30-05.txt
            String ts = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String fileName = "../logs/log_" + ts + ".txt";

            FileHandler handler = new FileHandler(fileName, true);
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.ALL);

            Logger root = Logger.getLogger("");
            for (Handler h : root.getHandlers()) {
                root.removeHandler(h);
            }

            root.addHandler(handler);

            root.setLevel(Level.INFO);
        } catch (IOException e) {
            System.err.println("Could not initialize file logging: " + e.getMessage());
        }
    }
}
