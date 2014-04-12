package passphrase;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * PassphraseGenerator brings Diceware and xkcd 'password entropy' to life
 * @author Michael
 * @link http://world.std.com/~reinhold/diceware.html
 * @line http://blog.shay.co/password-entropy/
 * @since 4/11/2014 8:17 PM
 */
public class PassphraseGenerator {
    public static final int NUM_DIE = 5;
    public static final int MAX_WORDS = 6;
    private Map<Integer, String> wordlist;
    private int alphabetSize;
    private Die [] dice;

    /**
     * Command line driver
     * @param args on the command line.  No arguments to generate a randome pass phrase from Diceware.
     *             Pairs of passphrase/alphabet size to calculate existing pass phrase entropy.
     *  (e.g. "correct horse battery staple" 27 "carrot ways base split" 27 f#Mo1e)*TjC8 72)
     */
    public static void main(String[] args) {
        try {
            if (args.length == 0) {  // Generate a random pass phrase
                Reader reader = createWordlistReader("diceware.wordlist.asc.txt");
                PassphraseGenerator generator = new PassphraseGenerator(reader);
                List<String> words = new ArrayList<String>();
                for (int i = 1; i <= MAX_WORDS; ++i) {
                    int key = generator.generateKey();
                    String word = generator.getWord(key);
                    if (word != null) {
                        words.add(word);
                    }
                }
                Map<String, Double> entropy = generator.calculateEntropy(words);
                for (String pp : entropy.keySet()) {
                    double s = entropy.get(pp);
                    double secondsToCrack = generator.calculateSecondsToCrack(s, 1000.0);
                    System.out.println();
                    System.out.println(String.format("passphrase      : '%s'", pp));
                    System.out.println(String.format("passphrase len  : %d", pp.length()));
                    System.out.println(String.format("alphabet size   : %d", generator.getAlphabetSize()));
                    System.out.println(String.format("bits of entropy : %-15.6f", s));
                    System.out.println(String.format("seconds to crack: %-15.6e", secondsToCrack));
                }
            } else { // Calculate entropy for n/2 pass phrases from command line
                for (int i = 0; i < args.length; i += 2) {
                    String passphrase = args[i];
                    int alphabetSize = Integer.parseInt(args[i+1]);
                    Reader reader = createPassphraseReader(passphrase);
                    PassphraseGenerator generator = new PassphraseGenerator(reader);
                    generator.setAlphabetSize(alphabetSize);
                    double s = generator.calculateEntropy(passphrase, generator.getAlphabetSize());
                    double secondsToCrack = generator.calculateSecondsToCrack(s, 1000.0);
                    System.out.println();
                    System.out.println(String.format("passphrase      : '%s'", passphrase));
                    System.out.println(String.format("passphrase len  : %d", passphrase.length()));
                    System.out.println(String.format("alphabet size   : %d", generator.getAlphabetSize()));
                    System.out.println(String.format("bits of entropy : %-15.6f", s));
                    System.out.println(String.format("seconds to crack: %-15.6e", secondsToCrack));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method to create the word list reader
     * @param resourceName containing the word list
     * @return Reader from classpath or file system.
     */
    private static Reader createWordlistReader(String resourceName) {
        Reader reader = null;
        try {
            String path = "/" + resourceName;
            InputStream is = PassphraseGenerator.class.getResourceAsStream(path);
            if (is != null) {
                System.out.println(String.format("Loading wordlist from CLASSPATH %s", resourceName));
                reader = new InputStreamReader(is);
            } else {
                path = "resources/" + resourceName;
                System.out.println(String.format("Loading wordlist from file system %s", path));
                reader = new FileReader(path);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return reader;
    }

    /**
     * Create a faux map of key/word pairs from a given pass phrase.
     * @param passphrase from which we'll generate key/word pairs
     * @return Reader containing the key/word pairs, one per line, separated by whitespace
     */
    private static Reader createPassphraseReader(String passphrase) {
        Reader reader = new StringReader("");
        if (passphrase != null) {
            String [] tokens = passphrase.split("\\s+");
            StringBuilder stringBuilder = new StringBuilder(2*passphrase.length());
            int key = 11111;
            for (int i = 0; i < tokens.length; ++i) {
                stringBuilder.append(key + i).append(' ').append(tokens[i]).append(System.getProperty("line.separator"));
            }
            reader = new StringReader(stringBuilder.toString());
        }
        return reader;
    }

    /**
     * Constructor
     * @param reader for the key/word pairs.
     * @throws IOException if the read fails
     */
    public PassphraseGenerator(Reader reader) throws IOException {
        init(reader, null);
    }

    /**
     * Constructor
     * @param reader for the key/word pairs.
     * @param seed for initializing the die randomizer
     * @throws IOException if the read fails
     */
    public PassphraseGenerator(Reader reader, long seed) throws IOException {
        init(reader, seed);
    }

    /**
     * Initialize the pass phrase generator
     * @param reader  for the key/word pairs.
     * @param seed for initializing the die randomizer; can be null for no seed.
     * @throws IOException if the read fails
     */
    private void init(Reader reader, Long seed) throws IOException {
        this.wordlist = readWordlist(reader);
        this.alphabetSize = calculateAlphabetSize();
        this.dice = new Die[NUM_DIE];
        for (int i = 0; i < NUM_DIE; ++i) {
            this.dice[i] = (seed == null ? new Die() : new Die(seed));
        }
    }

    /**
     * Generate a random key value by rolling NUM_DIE six-sided dies
     * @return key value generated from NUM_DIE six-sided dies.
     */
    public int generateKey() {
        StringBuilder key = new StringBuilder(NUM_DIE);
        for (int i = 0; i < NUM_DIE; ++i) {
            int value = this.dice[i].roll();
            key.append(value);
        }
        return Integer.parseInt(key.toString());
    }

    /**
     * Look up a word in the dictionary
     * @param key for the lookup - NUM_DIE values 1-6
     * @return word for the given key or null if no such key
     */
    public String getWord(int key) {
        return this.wordlist.get(key);
    }

    /**
     * Read access to the alphabet size
     * @return alphabet size
     */
    public int getAlphabetSize() {
        return alphabetSize;
    }

    /**
     * Write access to the alphabet size; necessary for entropy calculation
     * @param alphabetSize value
     * @throws java.lang.IllegalArgumentException if the alphabet size is less than or equal to zero.
     */
    public void setAlphabetSize(int alphabetSize) {
        if (alphabetSize <= 0) throw new IllegalArgumentException("alphabet size must be positive");
        this.alphabetSize = alphabetSize;
    }

    /**
     * Calculate the alphabet size for the corpus read into the pass phrase generator.
     * @return alphabet size
     */
    public int calculateAlphabetSize() {
        Set<Character> alphabet = new TreeSet<Character>();
        for (String word : this.wordlist.values()) {
            for (int i = 0; i < word.length(); ++i) {
                alphabet.add(word.charAt(i));
            }
        }
        return alphabet.size();
    }

    /**
     * Calculate the entropy for the given pass phrase using the generator alphabet size
     * @param passphrase to calculate entropy for
     * @return entropy for the passphrase
     */
    public double calculateEntropy(String passphrase) {
        return calculateEntropy(passphrase, this.alphabetSize);
    }


    /**
     * Calculate the entropy for the given pass phrase and alphabet size
     * @param passphrase to calculate entropy for
     * @param alphabetSize for the calculation
     * @return entropy for the passphrase
     */
    public double calculateEntropy(String passphrase, int alphabetSize) {
        double entropy = 0.0;
        if (passphrase != null) {
            entropy = passphrase.length()*Math.log(alphabetSize)/Math.log(2);
        }
        return entropy;
    }

    /**
     * Calculate entropies for a List of words
     * @param words to calculate entropy
     * @return Map of word/entropy pairs.
     */
    public Map<String, Double> calculateEntropy(List<String> words) {
        Map<String, Double> entropy = new LinkedHashMap<String, Double>(words.size());
        if (words != null) {
            for (int i = 0; i < words.size(); ++i) {
                StringBuilder passphrase = new StringBuilder(512);
                passphrase.append(words.get(0));
                for (int j = 0; j < i; ++j) {
                    passphrase.append(' ').append(words.get(j+1));
                }
                entropy.put(passphrase.toString(), calculateEntropy(passphrase.toString()));
            }
        }
        return entropy;
    }

    /**
     * Calculate seconds to crack a pass phrase with the given entropy
     * @param entropy for the pass phrase
     * @param guessesPerSecond assumed to crack
     * @return seconds required to crack the pass phrase.
     */
    public double calculateSecondsToCrack(double entropy, double guessesPerSecond) {
        double secondsToCrack = 0.0;
        if (guessesPerSecond > 0.0) {
            secondsToCrack = Math.pow(2.0, entropy)/guessesPerSecond;
        }
        return secondsToCrack;
    }

    /**
     * Helper method to read the key/word pairs
     * @param reader pointing to key/word pairs, one per line
     * @return Map of key/word pairs
     * @throws IOException if the read fails
     */
    private Map<Integer, String> readWordlist(Reader reader) throws IOException {
        Map<Integer, String> wordList = new HashMap<Integer, String>();
        BufferedReader br = null;
        try {
            if (reader != null) {
                br = new BufferedReader(reader);
                String line;
                while ((line = br.readLine()) != null) {
                    String [] tokens = line.split("\\s+");
                    if ((tokens.length > 1)) {
                        wordList.put(Integer.parseInt(tokens[0]), tokens[1]);
                    }
                }
            }
        } finally {
            close(br);
        }
        return wordList;
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
