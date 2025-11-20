import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

public class InputParsingValidationTest {

    private Path createTempInput(String content, Path dir) throws IOException {
        Path file = dir.resolve("input.txt");
        Files.writeString(file, content);
        return file;
    }

    @Test
    public void testValidParsing(@TempDir Path dir) throws Exception {
        String input =
                "[Stations]\n" +
                        "0 1001 1002\n" +
                        "1 1003\n" +
                        "[Charger Availability Reports]\n" +
                        "1001 0 50 true\n" +
                        "1002 50 100 false\n";

        InputParsingValidation parser = new InputParsingValidation();
        parser.parse(createTempInput(input, dir).toString());

        assertEquals(2, parser.stationToChargers.size());
        assertTrue(parser.stationToChargers.get(0L).contains(1001L));
        assertTrue(parser.stationToChargers.get(0L).contains(1002L));

        assertEquals(1, parser.chargerUp.get(1001L).size());
        assertEquals(1, parser.chargerAll.get(1001L).size());
    }

    @Test
    public void testMissingStationsHeader(@TempDir Path dir) throws IOException {
        String input =
                "0 1001\n" +
                        "[Charger Availability Reports]\n" +
                        "1001 0 10 true\n";

        InputParsingValidation parser = new InputParsingValidation();
        assertThrows(InputFormatException.class,
                () -> parser.parse(createTempInput(input, dir).toString()));
    }

    @Test
    public void testMissingReportsHeader(@TempDir Path dir) throws IOException {
        String input =
                "[Stations]\n" +
                        "0 1001\n";

        InputParsingValidation parser = new InputParsingValidation();
        assertThrows(InputFormatException.class,
                () -> parser.parse(createTempInput(input, dir).toString()));
    }

    @Test
    public void testDuplicateChargerIdAcrossStations(@TempDir Path dir) throws IOException {
        String input =
                "[Stations]\n" +
                        "0 1001\n" +
                        "1 1001\n" +   // duplicate
                        "[Charger Availability Reports]\n" +
                        "1001 0 10 true\n";

        InputParsingValidation parser = new InputParsingValidation();
        assertThrows(InputFormatException.class,
                () -> parser.parse(createTempInput(input, dir).toString()));
    }

    @Test
    public void testUndefinedChargerInReports(@TempDir Path dir) throws IOException {
        String input =
                "[Stations]\n" +
                        "0 1001\n" +
                        "[Charger Availability Reports]\n" +
                        "2000 0 10 true\n"; // undefined

        InputParsingValidation parser = new InputParsingValidation();
        assertThrows(InputFormatException.class,
                () -> parser.parse(createTempInput(input, dir).toString()));
    }

    @Test
    public void testInvalidInterval(@TempDir Path dir) throws IOException {
        String input =
                "[Stations]\n" +
                        "0 1001\n" +
                        "[Charger Availability Reports]\n" +
                        "1001 20 10 true\n";

        InputParsingValidation parser = new InputParsingValidation();
        assertThrows(InputFormatException.class,
                () -> parser.parse(createTempInput(input, dir).toString()));
    }

    @Test
    public void testInvalidBoolean(@TempDir Path dir) throws IOException {
        String input =
                "[Stations]\n" +
                        "0 1001\n" +
                        "[Charger Availability Reports]\n" +
                        "1001 0 10 maybe\n";

        InputParsingValidation parser = new InputParsingValidation();
        assertThrows(InputFormatException.class,
                () -> parser.parse(createTempInput(input, dir).toString()));
    }
}
