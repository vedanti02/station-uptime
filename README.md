# Overview

This project implements a Station Uptime Calculator for an EV charging network.

The program:

- Reads an input file describing stations, chargers, and availability intervals.
- Validates the file according to all preconditions.
- Computes uptime for each station.
- Outputs <StationID> <Uptime%> (rounded down).
- Outputs ERROR if the input is invalid.

The implementation uses clean Object-Oriented Java design, comprehensive validation, and full JUnit 5 tests.

# Project Structure

<img width="467" height="370" alt="image" src="https://github.com/user-attachments/assets/b77e24b6-c9b4-4c85-8d0a-e556127b0d31" />

out/ is (generated automatically by IntelliJ or command-line)

# Working

1. Parsing and Validation

- InputParsingValidation performs all validation:
- Checks for required headers:
  - [Stations]
  - [Charger Availability Reports]

Ensures:

- Station IDs are unique
- Charger IDs are unique across all stations
- Report charger IDs are defined in [Stations]
- Start < End for intervals
- Boolean values are true or false
- All values are unsigned integers

Invalid input -> throw InputFormatException -> Main prints "ERROR".

2. Uptime Calculation

- A station’s uptime is:
  - Percentage of time any charger at the station was up divided by the total time span during which any charger reported.
 Reporting Time (important)
- Reporting Time
`reporting = latest_end_time − earliest_start_time`
- Available Time: Merge all “up” intervals across chargers and sum the merged lengths.
- Final uptime:
  `uptime = floor(available * 100 / reporting)`
  
# How to Compile & Run

Option 1 — Using IntelliJ IDEA (Recommended)
IntelliJ automatically compiles .class files into the out/ folder if specified in the file properties
Anway, to use terminal:

#### If your .java files are under src/, navigate to root -> compile with:
`javac -d out src/*.java`

#### Navigate to out/ -> Run with:
`java Main ../input_1.txt`

# Logging

- The program uses Java’s built-in java.util.logging to record:
- Parsing progress 
- Warnings about malformed data 
- Uptime computation status 
- Any internal errors
- Logs Are Saved to logs/ Directory. Each run generates a new timestamped log file.


# Testing (JUnit 5)

Set up JUnit5:
1. File -> Project Structure -> Libraris
2. Click +(Add)
3. Choose From Maven
4. Enter:
   `org.junit.jupiter:junit-jupiter:5.9.3`
5. Apply -> OK

#### Tests are located under /test.

### Execute tests in IntelliJ:

1. Mark /src as Sources Root (Right click on src -> Mark Directory as -> Sources Root)
2. Mark /test as Test Sources Root (Right click on src -> Mark Directory as -> Test sources Root)
3. IntelliJ will prompt you to add JUnit 5 — click Yes
4. Right-click any test → Run


### Test Coverage

-InputParsingValidationTest
  - Missing headers
  - Duplicate IDs
  - Invalid intervals
  - Unknown charger IDs
  - Invalid booleans
  - Valid parsing

- UptimeServiceTest
  - Full uptime
  - Zero uptime
  - Span-based uptime
  - Multi-charger tests
  - Overlapping intervals

- MainIntegrationTest
  - Valid full program run
  - Invalid input → prints ERROR

### Edge Cases

- Station has only down intervals -> `0%`
- Gaps in reporting time -> counted as downtime
- Multiple chargers at same station
- Reports in any order
- Large timestamps (uses `BigInteger`)
- Any malformed input -> `ERROR`

# Complexity Analysis

Let R = number of availability intervals.
- Parsing: O(R)
- Sorting: O(R log R)
- Total: O(R log R)
- Space: O(R + S + C)

# Conclusion

This solution:

- Fully implements the challenge specification
- Correctly computes uptime using span-based reporting windows
- Uses a clean OOP architecture
- Includes full JUnit testing
- Works with IntelliJ or CLI
- Handles all edge cases defined by the prompt




