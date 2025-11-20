

import java.math.BigInteger;

/** time interval [start, end). */
public class Interval {
    public BigInteger start;
    public BigInteger end;

    public Interval(BigInteger s, BigInteger e) {
        this.start = s;
        this.end = e;
    }
}
