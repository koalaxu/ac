package ac.data.base;

import java.util.Random;

public class DeterministicRandom {
	public DeterministicRandom() {
	}
	
	public void SetSeed(long seed) {
		this.seed = seed;
	}
	
	public Random GetRandom() {
		random.setSeed(seed);
		seed = random.nextLong();
		return random;
	}
	
	private transient Random random = new Random();
	public long seed;
}
