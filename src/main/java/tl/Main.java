package tl;

import java.util.concurrent.atomic.AtomicInteger;

public class Main {
	private static AtomicInteger clock = new AtomicInteger(0);
	private static int finished = 0;
	private static final int nbThreads = 8;
	private static Integer value = null;

	public static void main(String[] args) {
		Register<Integer> x = new STMRegister<>(0, clock);

		Thread ts[] = new Thread[nbThreads];
		for(int i = 0; i < nbThreads; i++) {
			final int j = i;
			ts[i] = new Thread() {
				public void run() {
					increment(x, j);
				}
			};
		}

		for(int i = 0; i < nbThreads; i++)
			ts[i].start();
	}

	public static void increment(Register<Integer> x, int j) {
		Transaction t = new STMTransaction(clock, j);
		while(!t.isCommited()) {
			try {
				t.begin();
				Integer monInt = x.read(t);
				for(int i = 0; i < 100; i++)
					monInt = new Integer(monInt.intValue() + 1);
				x.write(t, monInt);
				value = x.read(t);
				t.tryToCommit();
			} catch (AbortException e) {
				System.out.println(j + ": Commit aborted, waiting for a new one...");
				//e.printStackTrace();
			}
		}
		finish();
	}

	private static void finish() {
		if((++finished) == nbThreads) {
			System.out.println("Value changed to " + value);
		}
	}
}