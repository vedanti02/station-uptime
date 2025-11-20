import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class MainIntegrationTest {

    private Path createTempInput(String content, Path dir) throws IOException {
        Path file = dir.resolve("input.txt");
        Files.writeString(file, content);
        return file;
    }

    @Test
    public void testFullProgramOutput(@TempDir Path dir) throws Exception {
        String input =
                "[Stations]\n" +
                        "0 1001 1002\n" +
                        "1 1003\n" +
                        "2 1004\n" +
                        "[Charger Availability Reports]\n" +
                        "1001 0 50000 true\n" +
                        "1001 50000 100000 true\n" +
                        "1002 50000 100000 true\n" +
                        "1003 25000 75000 false\n" +
                        "1004 0 50000 true\n" +
                        "1004 100000 200000 true\n";

        Path file = createTempInput(input, dir);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));

        Main.main(new String[]{ file.toString() });

        System.setOut(original);

        String output = out.toString().trim();

        assertEquals("""
                     0 100
                     1 0
                     2 75""".replace("\n", System.lineSeparator()),
                output);
    }

    @Test
    public void testMainErrorOnBadInput(@TempDir Path dir) throws Exception {
        String badInput =
                "[Stations]\n" +
                        "0 1001\n"; // Missing reports section

        Path file = createTempInput(badInput, dir);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));

        Main.main(new String[]{ file.toString() });

        System.setOut(original);

        assertEquals("ERROR", out.toString().trim());
    }
}
