import java.math.BigInteger;
import java.util.*;

/**
 * Computes station uptime using merged intervals.
 */
public class StationUptime {

    private final InputParsingValidation parser;

    public StationUptime(InputParsingValidation parser) {
        this.parser = parser;
    }

    /** Computes uptime for each station & prints it. */
    public void computeAndPrint() {
        List<Long> stations = new ArrayList<>(parser.stationToChargers.keySet());
        Collections.sort(stations);

        for (long stationId : stations) {
            int uptime = computeUptime(stationId);
            System.out.println(stationId + " " + uptime);
        }
    }

    /** Computes uptime % for a station. */
    int computeUptime(long stationId) {
        List<Interval> all = new ArrayList<>();
        List<Interval> up = new ArrayList<>();

        for (long cid : parser.stationToChargers.get(stationId)) {
            if (parser.chargerAll.containsKey(cid))
                all.addAll(parser.chargerAll.get(cid));
            if (parser.chargerUp.containsKey(cid))
                up.addAll(parser.chargerUp.get(cid));
        }

        BigInteger reporting = computeSpan(all);
        BigInteger available = mergedLength(up);

        if (reporting.equals(BigInteger.ZERO)) return 0;

        BigInteger pct = available.multiply(BigInteger.valueOf(100))
                .divide(reporting);

        if (pct.compareTo(BigInteger.valueOf(100)) > 0) return 100;
        if (pct.compareTo(BigInteger.ZERO) < 0) return 0;

        return pct.intValue();
    }

    /** Returns the union length of merged intervals. */
    private BigInteger mergedLength(List<Interval> intervals) {
        if (intervals.isEmpty()) return BigInteger.ZERO;

        intervals.sort((a, b) -> {
            int cmp = a.start.compareTo(b.start);
            return (cmp != 0) ? cmp : a.end.compareTo(b.end);
        });

        BigInteger total = BigInteger.ZERO;
        BigInteger cs = intervals.get(0).start;
        BigInteger ce = intervals.get(0).end;

        for (int i = 1; i < intervals.size(); i++) {
            Interval iv = intervals.get(i);
            if (iv.start.compareTo(ce) <= 0) {
                if (iv.end.compareTo(ce) > 0) ce = iv.end;
            } else {
                total = total.add(ce.subtract(cs));
                cs = iv.start;
                ce = iv.end;
            }
        }
        return total.add(ce.subtract(cs));
    }

    private BigInteger computeSpan(List<Interval> intervals) {
        if (intervals.isEmpty()) return BigInteger.ZERO;

        BigInteger minStart = intervals.get(0).start;
        BigInteger maxEnd = intervals.get(0).end;

        for (Interval iv : intervals) {
            if (iv.start.compareTo(minStart) < 0) {
                minStart = iv.start;
            }
            if (iv.end.compareTo(maxEnd) > 0) {
                maxEnd = iv.end;
            }
        }

        return maxEnd.subtract(minStart);
    }

}
