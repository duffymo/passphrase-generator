package passphrase;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * Die six sided
 * @author Michael
 * @link
 * @since 4/11/2014 8:22 PM
 */
public class Die {
    public static final int NUM_SIDES = 6;
    private Random random;

    public static void main(String[] args) {
        int numRolls = ((args.length > 0) ? Integer.parseInt(args[0]) : 10);
        Die die = new Die();
        Map<Integer, Integer> distribution = new TreeMap<Integer, Integer>();
        for (int i = 0; i < numRolls; ++i) {
            int roll = die.roll();
            if (distribution.containsKey(roll)) {
                int count = distribution.get(roll);
                distribution.put(roll, ++count);
            } else {
                distribution.put(roll, 1);
            }
        }
        for (int value : distribution.keySet()) {
            int count = distribution.get(value);
            System.out.println(String.format("value: %d count: %10d fraction: %10.3f", value, count, ((double)count)/numRolls));
        }
    }

    public Die() {
        this.random = new Random();
    }

    public Die(long seed) {
        this.random = new Random(seed);
    }

    public int roll() {
        return this.random.nextInt(NUM_SIDES) + 1;
    }
}
