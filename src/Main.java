import java.io.IOException;

/**
 * Main for the Station Uptime Calculator program.
 * Prints ERROR on invalid input.
 */
public class Main {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("ERROR");
            return;
        }

        try {
            InputParsingValidation parser = new InputParsingValidation();
            parser.parse(args[0]);

            StationUptime uptimeService = new StationUptime(parser);
            uptimeService.computeAndPrint();
        } catch (InputFormatException | IOException e) {
            // All invalid input/file errors result in "ERROR"
            // System.out.println("Debug: " + e.getMessage());
            System.out.println("ERROR");
        }
    }
}