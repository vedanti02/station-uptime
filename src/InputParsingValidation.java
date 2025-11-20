import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.math.BigInteger;
/**
 * Reads and validates the input file, producing in-memory representations.
 */
public class InputParsingValidation {
    private Set<Long> chargers = new HashSet<>();
    public Map<Long, List<Long>> stationToChargers = new HashMap<>();
    public Map<Long, List<Interval>> chargerAll = new HashMap<>();
    public Map<Long, List<Interval>> chargerUp = new HashMap<>();

    /** Parses the input file; throws InputFormatException on invalid format. */
    public void parse(String path) throws InputFormatException, IOException {
        List<String> lines = readCleanLines(path);
        if (lines.isEmpty()) throw new InputFormatException("File is empty.");
        int idx = 0;

        if (!lines.get(idx).equals("[Stations]")) throw new InputFormatException("Missing [Stations] header.");
        idx++;

        idx = parseStations(lines, idx);

        if (idx >= lines.size() || !lines.get(idx).equals("[Charger Availability Reports]")) {
            throw new InputFormatException("Missing [Charger Availability Reports] header or unexpected EOF.");
        }
        idx++;

        availabilityReports(lines, idx);
    }

    /** Reads non-empty trimmed lines from the file. */
    private List<String> readCleanLines(String path) throws IOException {
        List<String> lines = new ArrayList<>();
        // Use try-with-resources for automatic resource management
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) lines.add(trimmed);
            }
        }
        return lines;
    }

    /** Parses the station->charger mappings. */
    private int parseStations(List<String> lines, int idx) throws InputFormatException {
        while (idx < lines.size() &&
                !lines.get(idx).equals("[Charger Availability Reports]")) {

            String[] parts = lines.get(idx).split("\\s+");
            if (parts.length < 2) throw new InputFormatException("Station line must have ID and at least one Charger ID.");

            long stationId = parseUnsignedInt(parts[0], "Station ID");

            if (stationToChargers.containsKey(stationId))
                throw new InputFormatException("Station ID is not unique: " + stationId);

            List<Long> chargerList = new ArrayList<>();

            for (int i = 1; i < parts.length; i++) {
                long cid = parseUnsignedInt(parts[i], "Charger ID");

                // Check uniqueness across ALL stations
                if (this.chargers.contains(cid))
                    throw new InputFormatException("Charger ID is not unique across all stations: " + cid);

                this.chargers.add(cid);   // add to global Set
                chargerList.add(cid);     // add to station list
            }

            stationToChargers.put(stationId, chargerList);
            idx++;
        }
        return idx;
    }


    /** Parses charger availability reports. */
    private void availabilityReports(List<String> lines, int idx) throws InputFormatException {
        for (int i = idx; i < lines.size(); i++) {
            String[] parts = lines.get(i).split("\\s+");
            if (parts.length != 4) throw new InputFormatException("Report line must have exactly 4 parts.");

            long chargerId = parseUnsignedInt(parts[0], "Report Charger ID");

            // Check if Charger ID is defined in the [Stations] section (Precondition)
            if (!chargers.contains(chargerId)) throw new InputFormatException("Report Charger ID not found in station definitions: " + chargerId);

            BigInteger start = parseUnsignedBig(parts[1], "Start Time");
            BigInteger end = parseUnsignedBig(parts[2], "End Time");

            // Precondition: start time must not be greater than end time
            if (start.compareTo(end) >= 0) throw new InputFormatException("Start time must be less than end time.");

            boolean up;
            String upStr = parts[3].toLowerCase();
            if (upStr.equals("true")) {
                up = true;
            } else if (upStr.equals("false")) {
                up = false;
            } else {
                throw new InputFormatException("Invalid boolean value in report: " + parts[3]);
            }

            Interval iv = new Interval(start, end);
            chargerAll.computeIfAbsent(chargerId, k -> new ArrayList<>()).add(iv);
            if (up) chargerUp.computeIfAbsent(chargerId, k -> new ArrayList<>()).add(iv);
        }
    }

    /** Parses unsigned 32-bit integer (IDs) into long. */
    private long parseUnsignedInt(String s, String name) throws InputFormatException {
        try {
            long v = Long.parseLong(s);
            // 0xFFFFFFFFL is 2^32 - 1
            if (v < 0 || v > 0xFFFFFFFFL) {
                throw new InputFormatException(name + " out of 32-bit range: " + s);
            }
            return v;
        } catch (NumberFormatException e) {
            throw new InputFormatException(name + " is not a valid number: " + s);
        }
    }

    /** Parses unsigned 64-bit integer (Times) into BigInteger. */
    private BigInteger parseUnsignedBig(String s, String name) throws InputFormatException {
        try {
            BigInteger bi = new BigInteger(s);
            if (bi.compareTo(BigInteger.ZERO) < 0) {
                throw new InputFormatException(name + " cannot be negative: " + s);
            }
            // No need to check max 64-bit value, as BigInteger handles it, and we only need non-negative.
            return bi;
        } catch (NumberFormatException e) {
            throw new InputFormatException(name + " is not a valid large number: " + s);
        }
    }
}