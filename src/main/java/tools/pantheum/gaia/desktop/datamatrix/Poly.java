package tools.pantheum.gaia.desktop.datamatrix;

/**
 * A polynomial over {@link GaloisField256}, used by the Reed-Solomon decoder.
 *
 * <p>Coefficients are held highest-order first, so {@code {2, 0, 3}} is {@code 2x^2 + 3}, and the
 * leading coefficient is never zero except for the zero polynomial itself. Keeping that invariant
 * in one place is what makes the error-correction code readable: degree is just
 * {@code length - 1}, with no special cases scattered through the algorithm.
 *
 * <p>Immutable — every operation returns a new polynomial.
 */
final class Poly {

    static final Poly ZERO = new Poly(new int[]{0});
    static final Poly ONE  = new Poly(new int[]{1});

    private final int[] coefficients;

    Poly(int[] coefficients) {
        if (coefficients.length == 0) {
            throw new IllegalArgumentException("a polynomial needs at least one coefficient");
        }
        int first = 0;
        while (first < coefficients.length - 1 && coefficients[first] == 0) first++;
        if (first == 0) {
            this.coefficients = coefficients;
        } else {
            this.coefficients = new int[coefficients.length - first];
            System.arraycopy(coefficients, first, this.coefficients, 0, this.coefficients.length);
        }
    }

    /** Returns {@code coefficient * x^degree}. */
    static Poly monomial(int degree, int coefficient) {
        if (coefficient == 0) return ZERO;
        int[] terms = new int[degree + 1];
        terms[0] = coefficient;
        return new Poly(terms);
    }

    int   degree()          { return coefficients.length - 1; }
    boolean isZero()        { return coefficients[0] == 0; }
    int[] coefficients()    { return coefficients; }

    /** Returns the coefficient of {@code x^degree}. */
    int coefficient(int degree) {
        return coefficients[coefficients.length - 1 - degree];
    }

    /** Evaluates the polynomial at {@code x} by Horner's method. */
    int evaluateAt(int x) {
        if (x == 0) return coefficient(0);
        int result = coefficients[0];
        for (int i = 1; i < coefficients.length; i++) {
            result = GaloisField256.add(GaloisField256.multiply(x, result), coefficients[i]);
        }
        return result;
    }

    /** Addition and subtraction are the same operation in a binary field. */
    Poly addOrSubtract(Poly other) {
        if (isZero()) return other;
        if (other.isZero()) return this;

        int[] smaller = this.coefficients;
        int[] larger  = other.coefficients;
        if (smaller.length > larger.length) {
            int[] swap = smaller; smaller = larger; larger = swap;
        }

        int[] sum = new int[larger.length];
        int lengthDifference = larger.length - smaller.length;
        System.arraycopy(larger, 0, sum, 0, lengthDifference);
        for (int i = lengthDifference; i < larger.length; i++) {
            sum[i] = GaloisField256.add(smaller[i - lengthDifference], larger[i]);
        }
        return new Poly(sum);
    }

    Poly multiply(Poly other) {
        if (isZero() || other.isZero()) return ZERO;

        int[] a = this.coefficients;
        int[] b = other.coefficients;
        int[] product = new int[a.length + b.length - 1];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b.length; j++) {
                product[i + j] = GaloisField256.add(product[i + j],
                        GaloisField256.multiply(a[i], b[j]));
            }
        }
        return new Poly(product);
    }

    Poly multiply(int scalar) {
        if (scalar == 0) return ZERO;
        if (scalar == 1) return this;
        int[] scaled = new int[coefficients.length];
        for (int i = 0; i < coefficients.length; i++) {
            scaled[i] = GaloisField256.multiply(coefficients[i], scalar);
        }
        return new Poly(scaled);
    }

    /** Returns {@code this * coefficient * x^degree}. */
    Poly multiplyByMonomial(int degree, int coefficient) {
        if (degree < 0) throw new IllegalArgumentException("negative degree: " + degree);
        if (coefficient == 0) return ZERO;
        int[] product = new int[coefficients.length + degree];
        for (int i = 0; i < coefficients.length; i++) {
            product[i] = GaloisField256.multiply(coefficients[i], coefficient);
        }
        return new Poly(product);
    }

    /** Returns {@code {quotient, remainder}} of dividing this polynomial by {@code other}. */
    Poly[] divide(Poly other) {
        if (other.isZero()) throw new ArithmeticException("division by the zero polynomial");

        Poly quotient  = ZERO;
        Poly remainder = this;

        int denominatorLeading = other.coefficient(other.degree());
        int inverseLeading     = GaloisField256.inverse(denominatorLeading);

        while (remainder.degree() >= other.degree() && !remainder.isZero()) {
            int degreeDifference = remainder.degree() - other.degree();
            int scale = GaloisField256.multiply(
                    remainder.coefficient(remainder.degree()), inverseLeading);
            quotient  = quotient.addOrSubtract(monomial(degreeDifference, scale));
            remainder = remainder.addOrSubtract(other.multiplyByMonomial(degreeDifference, scale));
        }
        return new Poly[]{ quotient, remainder };
    }
}
