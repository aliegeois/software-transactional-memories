package tl;

import java.util.TreeSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class STMTransaction implements Transaction {
	private boolean commited = false;
	private int birthDate = 0;
	private int commitDate = 0;
	private AtomicInteger clock;
	private int i;
	
	private Set<Register> lrs = new TreeSet<Register>();
	private Set<Register> lws = new TreeSet<Register>();

	public STMTransaction(AtomicInteger clock, int i) {
		this.clock = clock;
		this.i = i;
	}

	@Override
	public void begin() {
		System.out.println(i + ": Transaction begin, resetting local variables...");
		for(Register r : lrs)
			r.reset();
		for(Register r : lws)
			r.reset();
		System.out.println(i + ": Local variables reset");

		birthDate = clock.get();
		System.out.println(i + ": birthDate set to " + birthDate);
	}

	@Override
	public void tryToCommit() throws AbortException {
		System.out.println(i + ": Transaction tryToCommit");
		Set<Register> all = new TreeSet<Register>();
		all.addAll(lrs);
		all.addAll(lws);
		System.out.println(i + ": Locking all registers...");
		for(Register r : all)
			r.lock();
		System.out.println(i + ": Registers locked");
		
		System.out.println(i + ": Checking commit date of registers...");
		for(Register r : lrs) {
			if(r.getDate() > birthDate) {
				System.out.println(i + ": Error: register changed since last commit, releasing all locks...");
				for(Register f : all)
					f.unlock();
				System.out.println(i + ": All locks released");
				throw new AbortException();
			}
		}
		System.out.println(i + ": All dates checked");
		//commitDate = clock.getAndIncrement();
		commitDate = clock.incrementAndGet();
		System.out.println(i + ": New commit date: " + commitDate);
		System.out.println(i + ": Changing commit date of all registers...");
		for(Register r : lws) {
			//r.setDate(commitDate);
			r.commit(commitDate);
		}
		System.out.println(i + ": All dates updated");

		commited = true;

		System.out.println(i + ": Releasing all locks...");
		for(Register r : all)
			r.unlock();
		System.out.println(i + ": All locks released");
	}

	@Override
	public boolean isCommited() {
		return commited;
	}

	@Override
	public void addReadRegister(Register r) {
		this.lws.add(r);
	}

	@Override
	public void addWriteRegister(Register r) {
		this.lrs.add(r);
	}

	@Override
	public int getBirthDate() {
		return birthDate;
	}
}