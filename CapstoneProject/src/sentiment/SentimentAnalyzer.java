package sentiment;

import java.util.List;
import java.util.Arrays;

/**
 * SentimentAnalyzer based on Random Indexing + lexicon projection.
 * <p>
 * This class implements a lightweight sentiment analysis method:
 * <ul>
 *   <li>Random Indexing: each word deterministically maps to a D-dimensional random +1/−1 vector (derived from its hashcode).</li>
 *   <li>A "sentiment axis" is created by summing random vectors of a small, hand-curated positive lexicon and subtracting
 *       the sum of a negative lexicon.</li>
 *   <li>Headlines are scored by averaging the random vectors of tokens in the headline and projecting that average vector
 *       onto the sentiment axis.</li>
 * </ul>
 * </p>
 * <p>No large external files or libraries are needed for this approach.</p>
 */
public class SentimentAnalyzer {

    /** Dimensionality of random index vectors. */
    private static final int D = 200;

    /** List of positive seed words used to construct the positive sentiment vector. */
    private static final List<String> POS_WORDS = Arrays.asList(
        "gain", "surge", "bullish", "profit", "strong",
        "upside", "beat", "recover", "upgrade", "record"
    );

    /** List of negative seed words used to construct the negative sentiment vector. */
    private static final List<String> NEG_WORDS = Arrays.asList(
        "loss", "slump", "bearish", "drop", "miss",
        "decline", "downside", "risk", "warn", "underperform"
    );

    /** Precomputed sentiment axis vector used for projecting headline sentiment. */
    private final double[] sentimentAxis;

    /**
     * Constructs a new SentimentAnalyzer, initializing the sentiment axis
     * by building the vector difference between positive and negative seed words.
     */
    public SentimentAnalyzer() {
        sentimentAxis = buildSentimentAxis();
    }

    /**
     * Builds the sentiment axis vector by computing the mean random vector
     * of positive words minus the mean random vector of negative words.
     * 
     * @return sentiment axis vector of length {@code D}
     */
    private double[] buildSentimentAxis() {
        double[] posSum = new double[D];
        double[] negSum = new double[D];
        for (String w : POS_WORDS) {
            addInPlace(posSum, randomVector(w));
        }
        for (String w : NEG_WORDS) {
            addInPlace(negSum, randomVector(w));
        }
        scaleInPlace(posSum, 1.0 / POS_WORDS.size());
        scaleInPlace(negSum, 1.0 / NEG_WORDS.size());

        double[] axis = new double[D];
        for (int i = 0; i < D; i++) {
            axis[i] = posSum[i] - negSum[i];
        }
        return axis;
    }

    /**
     * Computes the average sentiment score of a list of headlines.
     * 
     * @param headlines list of raw headline strings; can be empty or null
     * @return average sentiment score in the range [-1.0, +1.0], or 0.0 if input is empty or null
     */
    public double getSentiment(List<String> headlines) {
        if (headlines == null || headlines.isEmpty())
            return 0.0;
        double sum = 0.0;
        for (String h : headlines) {
            sum += scoreHeadline(h);
        }
        return sum / headlines.size();
    }

    /**
     * Scores a single headline by tokenizing, averaging token random vectors,
     * and projecting onto the sentiment axis.
     * 
     * @param text headline text to score
     * @return sentiment score in [-1.0, +1.0], or 0.0 if no valid tokens found
     */
    private double scoreHeadline(String text) {
        String[] tokens = text.toLowerCase().replaceAll("[^a-z ]", " ").split("\\s+");
        double[] avg = new double[D];
        int count = 0;
        for (String w : tokens) {
            if (w.isEmpty())
                continue;
            addInPlace(avg, randomVector(w));
            count++;
        }
        if (count == 0)
            return 0.0;
        scaleInPlace(avg, 1.0 / count);

        // Compute projection: dot(avg, axis) / ||axis||
        double dot = 0.0;
        double norm2 = 0.0;
        for (int i = 0; i < D; i++) {
            dot += avg[i] * sentimentAxis[i];
            norm2 += sentimentAxis[i] * sentimentAxis[i];
        }
        return dot / Math.sqrt(norm2);
    }

    /**
     * Generates a deterministic random vector of +1/-1 values for a word,
     * based on the word's hash code.
     * 
     * @param word input word string
     * @return a {@code D}-dimensional vector with components +1.0 or -1.0
     */
    private double[] randomVector(String word) {
        double[] vec = new double[D];
        java.util.Random rnd = new java.util.Random(word.hashCode());
        for (int i = 0; i < D; i++) {
            vec[i] = rnd.nextBoolean() ? 1.0 : -1.0;
        }
        return vec;
    }

    /**
     * Adds vector {@code b} into vector {@code a} component-wise (a += b).
     * 
     * @param a vector to be updated in-place
     * @param b vector to add
     */
    private void addInPlace(double[] a, double[] b) {
        for (int i = 0; i < a.length; i++) {
            a[i] += b[i];
        }
    }

    /**
     * Scales vector {@code a} in-place by a scalar factor {@code s} (a *= s).
     * 
     * @param a vector to be scaled
     * @param s scaling factor
     */
    private void scaleInPlace(double[] a, double s) {
        for (int i = 0; i < a.length; i++) {
            a[i] *= s;
        }
    }
}
