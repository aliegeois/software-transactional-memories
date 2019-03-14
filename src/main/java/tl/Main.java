package tl;

import java.util.concurrent.atomic.AtomicInteger;

public class Main {
	private static AtomicInteger clock = new AtomicInteger(0);

	public static void main(String[] args) {
		Register<Integer> x = new STMRegister<>(10, clock);
		increment(x);
	}

	public static void increment(Register<Integer> x) {
		Transaction t = new STMTransaction(clock);
		while(!t.isCommited()) {
			try {
				t.begin();
				x.write(t, x.read(t) + 1);
				t.tryToCommit();
			} catch (AbortException e) {
				e.printStackTrace();
			}
		}
		Transaction t2 = new STMTransaction(clock);
		Integer value = null;
		while(!t2.isCommited()) {
			try {
				t2.begin();
				value = x.read(t2);
				t2.tryToCommit();
			} catch (AbortException e) {
				e.printStackTrace();
			}
		}
		System.out.println(value);
	}
}