package combat;

import java.util.Random;

public class RandomProvider {
    private final Random rng = new Random();
    public int nextInt(int bound) { return rng.nextInt(bound); }
}
