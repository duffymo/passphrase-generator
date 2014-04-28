package passphrase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.TreeMap;

/**
 * Corpus reads in the Google word corpus into a Map for further processing.
 * @author Michael
 * @link
 * @since 4/24/2014 8:38 PM
 */
public class Corpus {
    private Map<String, Integer> dictionary;
    private Map<Character, Integer> histogram;

    public Corpus(Reader reader) {
        this.dictionary = readDictionary(reader);
    }

    private Map<String, Integer> readDictionary(Reader reader) {
        Map<String, Integer> dictionary = new TreeMap<String, Integer>();
        BufferedReader br = null;
        if (reader != null) {
            try {
                br = new BufferedReader(reader);
                String line = "";
                while ((line = br.readLine()) != null) {
                    String [] tokens = line.split("\\s+");
                    if ((tokens != null) && (tokens.length > 2)) {
                        dictionary.put(tokens[0], Integer.valueOf(tokens[1]));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                Utils.close(br);
            }

        }
        return dictionary;
    }
}
