package passphrase;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Utils common to Corpus and PassphraseGenerator
 * @author Michael
 * @link
 * @since 4/27/2014 7:26 PM
 */
public class Utils {


    private Utils() {}

    /**
     * Create histogram of first character/count pairs
     * @param wordList as basis for histogram
     * @return Map of first character/count pairs
     */
    public static  Map<Character, Integer> createHistogram(Collection<String> wordList) {
        Map<Character, Integer> histogram = new TreeMap<Character, Integer>();
        for (String word : wordList) {
            char first = word.charAt(0);
            if (!histogram.containsKey(first)) {
                histogram.put(first, 1);
            } else {
                int count = histogram.get(first);
                histogram.put(first, ++count);
            }
        }
        return histogram;
    }

    /**
     * Close a Reader quietly
     * @param reader to close
     */
    public static void close(Reader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
