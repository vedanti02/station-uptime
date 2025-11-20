import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.math.BigInteger;
import java.util.logging.Logger;

/**
 * Reads and validates the input file, producing in-memory representations.
 */
public class InputParsingValidation {

    private static final Logger logger = Logger.getLogger(InputParsingValidation.class.getName());
    private final Set<Long> chargers = new HashSet<>();
    public final Map<Long, List<Long>> stationToChargers = new HashMap<>();
    public final Map<Long, List<Interval>> chargerAll = new HashMap<>();
    public final Map<Long, List<Interval>> chargerUp = new HashMap<>();


    /** Parses the input file; throws InputFormatException on invalid format. */
    public void parse(String path) throws InputFormatException, IOException {
        logger.info("Parsing started for file: " + path);

        List<String> lines = readCleanLines(path);
        if (lines.isEmpty()) {
            logger.severe("File is empty");
            throw new InputFormatException("File is empty.");
        }
        int idx = 0;

        if (!lines.get(idx).equals("[Stations]")) {
            logger.severe("Missing [Stations] header");
            throw new InputFormatException("Missing [Stations] header.");
        }
        idx++;

        idx = parseStations(lines, idx);

        if (idx >= lines.size() || !lines.get(idx).equals("[Charger Availability Reports]")) {
            logger.severe("Missing [Charger Availability Reports] header");
            throw new InputFormatException("Missing [Charger Availability Reports] header or unexpected EOF.");
        }
        idx++;

        availabilityReports(lines, idx);
        logger.info("Parsing completed successfully.");
    }

    /** Reads non-empty trimmed lines from the file. */
    private List<String> readCleanLines(String path) throws IOException {
        List<String> lines = new ArrayList<>();
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
            if (parts.length < 2) {
                logger.warning("Invalid station line: " + lines.get(idx));
                throw new InputFormatException("Station line must have ID and at least one Charger ID.");
            }

            long stationId = parseUnsignedInt(parts[0], "Station ID");

            if (stationToChargers.containsKey(stationId)) {
                logger.warning("Duplicate station ID: " + stationId);
                throw new InputFormatException("Station ID is not unique: " + stationId);
            }

            List<Long> chargerList = new ArrayList<>();

            for (int i = 1; i < parts.length; i++) {
                long cid = parseUnsignedInt(parts[i], "Charger ID");
                if (chargers.contains(cid)) {
                    logger.warning("Duplicate charger ID encountered: " + cid);
                    throw new InputFormatException("Charger ID is not unique across all stations: " + cid);
                }
                chargers.add(cid);
                chargerList.add(cid);
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
            if (parts.length != 4) {
                logger.warning("Invalid availability report line: " + lines.get(i));
                throw new InputFormatException("Report line must have exactly 4 parts.");
            }

            long chargerId = parseUnsignedInt(parts[0], "Report Charger ID");

            if (!chargers.contains(chargerId)) {
                logger.warning("Report references unknown charger ID: " + chargerId);
                throw new InputFormatException("Report Charger ID not found in station definitions: " + chargerId);
            }

            BigInteger start = parseUnsignedBig(parts[1], "Start Time");
            BigInteger end = parseUnsignedBig(parts[2], "End Time");

            if (start.compareTo(end) >= 0) {
                logger.warning("Invalid interval (start >= end) for charger " + chargerId);
                throw new InputFormatException("Start time must be less than end time.");
            }

            boolean up;
            String upStr = parts[3].toLowerCase();
            if (upStr.equals("true")) up = true;
            else if (upStr.equals("false")) up = false;
            else {
                logger.warning("Invalid boolean: " + parts[3]);
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
            if (v < 0 || v > 0xFFFFFFFFL) { //0xFFFFFFFFL -> 2^32 -1
                logger.warning(name + " out of 32-bit range: " + s);
                throw new InputFormatException(name + " out of 32-bit range: " + s);
            }
            return v;
        } catch (NumberFormatException e) {
            logger.warning(name + " is not a valid number: " + s);
            throw new InputFormatException(name + " is not a valid number: " + s);
        }
    }

    /** Parses unsigned 64-bit integer (Times) into BigInteger. */
    private BigInteger parseUnsignedBig(String s, String name) throws InputFormatException {
        try {
            BigInteger bi = new BigInteger(s);
            if (bi.compareTo(BigInteger.ZERO) < 0) {
                logger.warning(name + " cannot be negative: " + s);
                throw new InputFormatException(name + " cannot be negative: " + s);
            }
            return bi;
        } catch (NumberFormatException e) {
            logger.warning(name + " is not a valid number: " + s);
            throw new InputFormatException(name + " is not a valid large number: " + s);
        }
    }
}
