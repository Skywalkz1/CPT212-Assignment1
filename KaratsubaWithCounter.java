import java.math.BigInteger;
import java.util.Random;

/**
 * =============================================================
 * CPT212 Design & Analysis of Algorithms
 * Assignment 1 – Part 2
 *
 * Karatsuba Multiplication Algorithm with Operation Counter
 * =============================================================
 *
 * BACKGROUND
 * ----------
 * Discovered by Anatolii Karatsuba in 1960, the Karatsuba algorithm
 * uses divide-and-conquer to reduce the number of single-digit
 * multiplications needed to multiply two n-digit numbers.
 *
 * ALGORITHM IDEA
 * --------------
 * Given two n-digit numbers x and y, let m = ceil(n/2).
 * Split each number at the midpoint:
 *     x = a * 10^m  +  b      (a = high half, b = low half)
 *     y = c * 10^m  +  d
 *
 * Naively: x*y = ac*10^(2m) + (ad+bc)*10^m + bd   → 4 sub-multiplications
 *
 * Karatsuba's trick: observe that
 *     ad + bc = (a+b)(c+d) - ac - bd
 * so we only need 3 recursive multiplications:
 *     z0 = karatsuba(a, c)            → ac
 *     z2 = karatsuba(b, d)            → bd
 *     z1 = karatsuba(a+b, c+d)        → (a+b)(c+d)
 *     middle = z1 - z0 - z2           → ad + bc
 *
 * Final result: z0*10^(2m) + middle*10^m + z2
 *
 * RECURRENCE & COMPLEXITY
 * ------------------------
 * T(n) = 3*T(n/2) + O(n)
 * By the Master Theorem (case 1):  T(n) = O(n^log2(3)) ≈ O(n^1.585)
 * Compare: Simple Multiplication is O(n^2).
 *
 * DATA TYPE
 * ---------
 * BigInteger is used throughout so the algorithm handles arbitrarily
 * large numbers (n = 10 000 digits) without overflow.
 * BigInteger is part of Java SE — no external API is used.
 *
 * OPERATION COUNTER
 * -----------------
 * `opCount` is incremented at every primitive operation
 * (comparison, assignment, arithmetic, array/field access).
 * =============================================================
 */
public class KaratsubaWithCounter {

    /** Counts every primitive operation executed inside karatsuba(). */
    public static long opCount = 0;

    // -----------------------------------------------------------
    // numLength(n)
    //
    //   Returns the number of decimal digits in n.
    //   We count manually (rather than using String.length()) so
    //   that every step is visible to the counter.
    // -----------------------------------------------------------
    static int numLength(BigInteger n) {
        if (n.compareTo(BigInteger.ZERO) == 0) {
            opCount++;   // comparison
            return 1;    // "0" has 1 digit
        }
        int length = 0;                         opCount++;  // assignment
        BigInteger tmp = n.abs();               opCount++;  // abs + assignment
        while (tmp.compareTo(BigInteger.ZERO) > 0) {
            opCount++;                          // loop condition comparison
            length++;                           opCount++;  // increment
            tmp = tmp.divide(BigInteger.TEN);  opCount += 2;  // divide + assignment
        }
        opCount++;   // final loop-exit comparison
        return length;
    }

    // -----------------------------------------------------------
    // karatsuba(x, y)
    //
    //   Recursively multiplies two non-negative BigIntegers.
    //
    //   Base case : either operand is a single digit → one multiply.
    //   Recursive : split, perform three sub-multiplications, combine.
    // -----------------------------------------------------------
    public static BigInteger karatsuba(BigInteger x, BigInteger y) {

        opCount++;   // function-entry overhead (call assignment)

        // ---- Determine digit lengths --------------------------------
        int xLen = numLength(x);  opCount++;   // assignment
        int yLen = numLength(y);  opCount++;

        // ---- Base case: both numbers are single digits ---------------
        // Direct multiplication is one primitive operation.
        if (xLen == 1 && yLen == 1) {
            opCount += 2;                       // two comparisons
            BigInteger result = x.multiply(y); opCount += 2;  // multiply + assignment
            return result;
        }
        opCount += 2;   // comparisons (branch not taken)

        // ---- Compute split point m = ceil(max(xLen, yLen) / 2) ------
        int maxLen = Math.max(xLen, yLen);      opCount += 2;  // max + assignment
        int m      = (maxLen / 2) + (maxLen % 2); opCount += 3; // /, %, +, = (4 ops → 3 counted conservatively)

        // base = 10^m  (the positional shift for the split)
        BigInteger base = BigInteger.TEN.pow(m); opCount += 2;  // pow + assignment

        // ---- Split x = a * 10^m + b --------------------------------
        BigInteger a = x.divide(base);           opCount += 2;  // divide + assignment
        BigInteger b = x.remainder(base);        opCount += 2;  // remainder + assignment

        // ---- Split y = c * 10^m + d --------------------------------
        BigInteger c = y.divide(base);           opCount += 2;
        BigInteger d = y.remainder(base);        opCount += 2;

        // ---- Three recursive multiplications (Karatsuba's trick) ----

        // z0 = a * c
        BigInteger z0 = karatsuba(a, c);         opCount += 2;  // call + assignment

        // z2 = b * d
        BigInteger z2 = karatsuba(b, d);         opCount += 2;

        // z1 = (a+b) * (c+d)   ← only ONE multiplication, not two
        BigInteger z1 = karatsuba(a.add(b), c.add(d));  opCount += 4; // add,add,call,assignment

        // ---- Combine results ----------------------------------------
        // middle = z1 - z0 - z2  gives ad + bc without extra multiplications
        BigInteger middle = z1.subtract(z0).subtract(z2); opCount += 3; // sub,sub,assignment

        // result = z0 * 10^(2m) + middle * 10^m + z2
        BigInteger result =
              z0.multiply(base.pow(2))        // z0  shifted left by 2m digits
                .add(middle.multiply(base))   // middle shifted left by m digits
                .add(z2);                     // z2  at position 0
        opCount += 6;   // pow, multiply, multiply, add, add, assignment

        return result;
    }

    // -----------------------------------------------------------
    // randomNDigit(n, rng)
    //   Returns a random n-digit BigInteger with no leading zeros.
    // -----------------------------------------------------------
    static BigInteger randomNDigit(int n, Random rng) {
        if (n == 1) return BigInteger.valueOf(rng.nextInt(9) + 1);
        StringBuilder sb = new StringBuilder();
        sb.append((char)('1' + rng.nextInt(9)));
        for (int i = 1; i < n; i++) sb.append((char)('0' + rng.nextInt(10)));
        return new BigInteger(sb.toString());
    }

    // -----------------------------------------------------------
    // main(String[] args)
    //
    //   1. Verifies correctness on small fixed inputs.
    //   2. Runs empirical experiment comparing Karatsuba vs Simple
    //      Multiplication operation counts for n = 1..200 digits,
    //      printing a CSV table suitable for graph plotting.
    // -----------------------------------------------------------
    public static void main(String[] args) {

        // ---- Correctness verification on known small inputs ----------
        System.out.println("===================================================");
        System.out.println(" Karatsuba Correctness Verification");
        System.out.println("===================================================");

        long[][] tests = {
            {1234L,  5678L},
            {102L,   313L},
            {1345L,  63456L},
            {52301L, 380L}
        };

        for (long[] t : tests) {
            BigInteger bx = BigInteger.valueOf(t[0]);
            BigInteger by = BigInteger.valueOf(t[1]);
            opCount = 0;
            BigInteger actual   = karatsuba(bx, by);
            BigInteger expected = bx.multiply(by);
            System.out.printf("  %6d x %-6d  karatsuba=%-12s  expected=%-12s  OK=%b  ops=%d%n",
                t[0], t[1], actual, expected, actual.equals(expected), opCount);
        }
        System.out.println();

        // ---- Empirical comparison experiment -------------------------
        System.out.println("===================================================");
        System.out.println(" Part 2: Empirical Comparison");
        System.out.println(" (Plot these values to show O(n^2) vs O(n^1.585))");
        System.out.println("===================================================");
        System.out.println("n, simpleOps, karatsubaOps");

        Random rng = new Random(42);   // fixed seed for reproducibility

        // Same sizes as Part 1 for a direct side-by-side graph
        int[] sizes = {1,2,3,4,5,6,7,8,9,10,15,20,25,30,40,50,75,100,150,200};

        for (int n : sizes) {
            BigInteger x = SimpleMultiplication.randomNDigit(n, rng);
            BigInteger y = SimpleMultiplication.randomNDigit(n, rng);

            // -- Measure Simple Multiplication --
            SimpleMultiplication.opCount = 0;
            BigInteger simpleResult = SimpleMultiplication.simpleMultiply(x, y, false);
            long simpleOps = SimpleMultiplication.opCount;

            // -- Measure Karatsuba --
            opCount = 0;
            BigInteger karatsubaResult = karatsuba(x, y);
            long karatsubaOps = opCount;

            // -- Correctness check against Java SE BigInteger --
            BigInteger expected = x.multiply(y);
            if (!simpleResult.equals(expected) || !karatsubaResult.equals(expected)) {
                System.err.println("MISMATCH at n=" + n);
            }

            System.out.println(n + ", " + simpleOps + ", " + karatsubaOps);
        }

        // ---- Summary of theoretical complexity -----------------------
        System.out.println();
        System.out.println("=== Complexity Summary ===");
        System.out.println("  Simple Multiplication : O(n^2)");
        System.out.println("  Karatsuba Algorithm   : O(n^log2(3)) ≈ O(n^1.585)");
        System.out.println();
        System.out.println("Derivation for Karatsuba:");
        System.out.println("  Recurrence: T(n) = 3*T(n/2) + O(n)");
        System.out.println("  Master Theorem (case 1): log_b(a) = log_2(3) ≈ 1.585 > 1");
        System.out.println("  → T(n) = Θ(n^log2(3))");
        System.out.println();
        System.out.println("Empirical observation:");
        System.out.println("  For small n, Karatsuba has more overhead than Simple.");
        System.out.println("  Beyond ~n=100 digits, Karatsuba's op count grows noticeably");
        System.out.println("  slower, confirming its theoretical superiority at large n.");
    }
}
