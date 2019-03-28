package tl;

import java.util.concurrent.atomic.AtomicInteger;

public class Main {
	private static AtomicInteger clock = new AtomicInteger();
	private static int finished = 0;
	private static final int nbThreads = 8;
	private static Register<Integer> value = new STMRegister<>(0, clock);

	public static void main(String[] args) {
		Register<Integer> x = new STMRegister<>(0, clock);

		Thread ts[] = new Thread[nbThreads];
		for(int i = 0; i < nbThreads; i++) {
			ts[i] = new Thread() {
				public void run() {
					increment(x);
				}
			};
		}

		for(int i = 0; i < nbThreads; i++)
			ts[i].start();
	}

	public static void increment(Register<Integer> x) {
		Transaction t = new STMTransaction(clock);
		while(!t.isCommited()) {
			try {
				t.begin();
				Integer monInt = x.read(t);
				for(int i = 0; i < 100; i++)
					monInt = new Integer(monInt.intValue() + 1);
				x.write(t, monInt);
				value.write(t, x.read(t));
				t.tryToCommit();
			} catch (AbortException e) {
				// e.printStackTrace();
			}
		}
		finish();
	}

	private static void finish() {
		if((++finished) == nbThreads) {
			int fin = 0;
			Transaction t = new STMTransaction(clock);
			while(!t.isCommited()) {
				try {
					t.begin();
					fin = value.read(t);
					t.tryToCommit();
				} catch(AbortException e) {
					e.printStackTrace();
				}
			}
			System.out.println("Value changed to " + fin);
		}
	}
}