import java.math.BigInteger;
import java.util.Random;

/**
 * =============================================================
 * Assignment 1 – Simple Multiplication Algorithm
 * =============================================================
 * ALGORITHM OVERVIEW
 * ------------------
 * Step 1 – Digit-by-digit multiplication (schoolbook method):
 *   For each digit d of the multiplier (right-to-left, index j):
 *     For each digit of the multiplicand (right-to-left, index i):
 *       product       = multiplicand_digit[i] * d + carry
 *       partial_digit = product % 10   (digit we keep in this position)
 *       carry         = product / 10   (propagated to the next position)
 *     Store and print partial_digit and carry at every inner step.
 *
 * Step 2 – Shift-and-add:
 *   Multiplier digit j sits at column position j (i.e. worth 10^j).
 *   So partial-product row j is shifted left by j decimal places.
 *   All shifted rows are summed to produce the final result.
 *
 * WHY BigInteger?
 *   The rubric requires handling n = 10 000-digit numbers.
 *   Java primitives (int, long) overflow far below that.
 */
public class SimpleMultiplication {

    /** Counts every primitive operation executed inside simpleMultiply(). */
    public static long opCount = 0;
    // toDigits(n)
    //   Converts a BigInteger into an int[] of its decimal digits.
    //   Index 0 → least-significant digit (10^0 position).
    //   Index k → digit at the 10^k position.
    //   Storing LSD-first makes the inner multiply loop natural:
    //   digit[i] is always at position 10^i.
    static int[] toDigits(BigInteger n) {
        String s      = n.toString();    opCount++;   // string conversion (assignment)
        int    len    = s.length();      opCount++;   // length read (assignment)
        int[]  digits = new int[len];    opCount++;   // array allocation (assignment)
        for (int i = 0; i < len; i++) {
            opCount++;                               // loop-condition comparison
            digits[i] = s.charAt(len - 1 - i) - '0'; // reverse for LSD-first
            opCount++;                               // array store (assignment)
        }
        opCount++;  // final loop-exit comparison
        return digits;
    }

    // simpleMultiply(x, y, verbose) 
    // Parameters:
    //   x, y    – BigInteger operands (assumed equal digit length)
    //   verbose – if true, prints partial products and carriers
    //             for each multiplier digit (only use for small n)
    // Returns: x * y as a BigInteger.
    public static BigInteger simpleMultiply(BigInteger x, BigInteger y, boolean verbose) {

        int[] xDigits = toDigits(x);       opCount++;  // assignment
        int[] yDigits = toDigits(y);       opCount++;
        int   n       = xDigits.length;    opCount++;  // n = digits in multiplicand
        int   m       = yDigits.length;    opCount++;  // m = digits in multiplier

        // partialRows[j][i] = partial-product digit at position i for multiplier digit j
        // carrierRows[j][i] = carry leaving position i for multiplier digit j
        // Extra column (n+1) absorbs the final carry that overflows column n.
        int[][] partialRows = new int[m][n + 1];  opCount++;
        int[][] carrierRows = new int[m][n + 1];  opCount++;

        //STEP 1: compute partial products and carriers 
        for (int j = 0; j < m; j++) {
            opCount++;                              // outer-loop condition

            int yDigit = yDigits[j];  opCount++;   // current multiplier digit
            int carry  = 0;           opCount++;   // reset carry for this row

            for (int i = 0; i < n; i++) {
                opCount++;                          // inner-loop condition

                //Core single-digit multiply + carry
                int product      = xDigits[i] * yDigit + carry;  opCount += 3; // *, +, =
                int partialDigit = product % 10;                  opCount += 2; // %, =
                carry            = product / 10;                  opCount += 2; // /, =

                partialRows[j][i] = partialDigit;  opCount++;    // store partial digit
                carrierRows[j][i] = carry;         opCount++;    // store carry
            }
            opCount++;  // inner-loop exit comparison

            // If a carry remains after the last multiplicand digit, record it
            if (carry > 0) {
                opCount++;
                partialRows[j][n] = carry;  opCount++;
            }
            opCount++;  // branch (else path)

            //Print partial products and carriers (verbose only)
            if (verbose) {
                StringBuilder sbPP = new StringBuilder();
                StringBuilder sbCC = new StringBuilder();
                for (int i = n; i >= 0; i--) {
                    sbPP.append(partialRows[j][i]);
                    sbCC.append(carrierRows[j][i]);
                }
                System.out.println("  partial products for (" + x + " x " + yDigit + "): " + sbPP);
                System.out.println("  carriers for        (" + x + " x " + yDigit + "): " + sbCC);
            }
        }
        opCount++;  // outer-loop exit comparison

        //STEP 2: shift and add all partial-product rows
        BigInteger result = BigInteger.ZERO;  opCount++;  // accumulator
        BigInteger ten    = BigInteger.TEN;   opCount++;

        for (int j = 0; j < m; j++) {
            opCount++;  // outer-loop condition

            // Reconstruct numeric value of row j from its digit array
            BigInteger rowValue = BigInteger.ZERO;  opCount++;
            BigInteger place    = BigInteger.ONE;   opCount++;  // 10^i within this row

            for (int i = 0; i <= n; i++) {
                opCount++;  // inner-loop condition
                rowValue = rowValue.add(BigInteger.valueOf(partialRows[j][i]).multiply(place));
                opCount += 3;                      // multiply, add, assignment
                place = place.multiply(ten);       opCount += 2;  // multiply, assignment
            }
            opCount++;  // inner-loop exit

            // Apply column-position shift: multiply the row by 10^j
            BigInteger columnShift = ten.pow(j);         opCount += 2;  // pow, assignment
            rowValue = rowValue.multiply(columnShift);   opCount += 2;  // multiply, assignment

            result = result.add(rowValue);               opCount += 2;  // add, assignment
        }
        opCount++;  // outer-loop exit

        return result;
    }

    // randomNDigit(n, rng)
    //   Returns a random n-digit BigInteger with no leading zeros.
    public static BigInteger randomNDigit(int n, Random rng) {
        if (n == 1) return BigInteger.valueOf(rng.nextInt(9) + 1);
        StringBuilder sb = new StringBuilder();
        sb.append((char)('1' + rng.nextInt(9)));   // first digit: 1–9
        for (int i = 1; i < n; i++) {
            sb.append((char)('0' + rng.nextInt(10)));
        }
        return new BigInteger(sb.toString());
    }
    public static void main(String[] args) {

        //Demo 1: assignment
        System.out.println("===================================================");
        System.out.println(" DEMO: 52301 x 380   (from PDF)");
        System.out.println("===================================================");
        BigInteger d1 = new BigInteger("52301");
        BigInteger d2 = new BigInteger("380");
        opCount = 0;
        BigInteger demoResult = simpleMultiply(d1, d2, true);
        System.out.println("  Result   : " + demoResult);
        System.out.println("  Expected : " + d1.multiply(d2));
        System.out.println("  Correct  : " + demoResult.equals(d1.multiply(d2)));
        System.out.println("  Total operations  : " + opCount + "\n");

        // ---- Demo 2: second small example ------------------------------
        System.out.println("===================================================");
        System.out.println(" DEMO: 1234 x 5678");
        System.out.println("===================================================");
        BigInteger a = new BigInteger("1234");
        BigInteger b = new BigInteger("5678");
        opCount = 0;
        BigInteger res2 = simpleMultiply(a, b, true);
        System.out.println("  Result   : " + res2);
        System.out.println("  Expected : " + a.multiply(b));
        System.out.println("  Correct  : " + res2.equals(a.multiply(b)));
        System.out.println("  Total operations  : " + opCount + "\n");

        // ---- Empirical experiment: operation counts for random n-digit inputs ----
        System.out.println("===================================================");
        System.out.println(" EMPIRICAL EXPERIMENT - Operation Counts for Random n-Digit Inputs");
        System.out.println("===================================================");
        System.out.println("n, opCount");

        Random rng = new Random(42);  // fixed seed → reproducible results

        int[] sizes = {10,50,100,500,1000,5000,10000};  // test sizes (number of digits)

        for (int n : sizes) {
            BigInteger x = randomNDigit(n, rng);
            BigInteger y = randomNDigit(n, rng);

            opCount = 0;
            BigInteger product = simpleMultiply(x, y, false);

            // Correctness assertion using Java SE BigInteger
            if (!product.equals(x.multiply(y))) {
                System.err.println("ERROR – mismatch at n=" + n);
            }
            System.out.println(n + ", " + opCount);
        }
        System.out.println();
        System.out.println("Theoretical time complexity: O(n^2)");
        System.out.println("Justification: two nested loops, each running n iterations,");
    }
}
