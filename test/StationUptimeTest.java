import org.junit.jupiter.api.Test;
import java.math.BigInteger;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class StationUptimeTest {

    private Interval iv(long s, long e) {
        return new Interval(BigInteger.valueOf(s), BigInteger.valueOf(e));
    }

    @Test
    public void testFullUptimeSingleCharger() {
        InputParsingValidation parser = new InputParsingValidation();

        parser.stationToChargers.put(0L, List.of(1001L));
        parser.chargerAll.put(1001L, List.of(iv(0, 100)));
        parser.chargerUp.put(1001L, List.of(iv(0, 100)));

        StationUptime service = new StationUptime(parser);
        assertEquals(100, service.computeUptime(0L));
    }

    @Test
    public void testZeroUptime() {
        InputParsingValidation parser = new InputParsingValidation();

        parser.stationToChargers.put(1L, List.of(10L));
        parser.chargerAll.put(10L, List.of(iv(0, 100)));
        // no up intervals

        StationUptime service = new StationUptime(parser);
        assertEquals(0, service.computeUptime(1L));
    }

    @Test
    public void testSpanBasedUptime() {
        InputParsingValidation parser = new InputParsingValidation();

        parser.stationToChargers.put(2L, List.of(1004L));

        parser.chargerAll.put(1004L, List.of(
                iv(0, 50000),
                iv(100000, 200000)
        ));
        parser.chargerUp.put(1004L, List.of(
                iv(0, 50000),
                iv(100000, 200000)
        ));

        StationUptime service = new StationUptime(parser);

        assertEquals(75, service.computeUptime(2L)); // expected
    }

    @Test
    public void testMultipleChargersSpanMerge() {
        InputParsingValidation parser = new InputParsingValidation();

        parser.stationToChargers.put(99L, List.of(1L, 2L));

        parser.chargerAll.put(1L, List.of(iv(0, 100)));     // up
        parser.chargerUp.put(1L, List.of(iv(0, 100)));

        parser.chargerAll.put(2L, List.of(iv(200, 300)));   // down

        StationUptime service = new StationUptime(parser);

        // reporting window = 0 - > 300
        // available = 100
        // uptime = 33%
        assertEquals(33, service.computeUptime(99L));
    }
}
