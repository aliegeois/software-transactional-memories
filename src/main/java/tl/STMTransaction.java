package tl;

import java.util.TreeSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class STMTransaction implements Transaction {
	private boolean commited = false;
	private int birthDate = 0;
	private int commitDate = 0;
	private AtomicInteger clock;
	
	private Set<Register> lrs = new TreeSet<Register>();
	private Set<Register> lws = new TreeSet<Register>();

	public STMTransaction(AtomicInteger clock) {
		this.clock = clock;
	}

	@Override
	public void begin() {
		for(Register r : lrs)
			r.reset();
		for(Register r : lws)
			r.reset();
		lrs.clear();
		lws.clear();

		birthDate = clock.get();
	}

	@Override
	public void tryToCommit() throws AbortException {
		Set<Register> all = new TreeSet<Register>();
		all.addAll(lrs);
		all.addAll(lws);
		for(Register r : all) // lock all registers (in order)
			r.lock();
		
		for(Register r : lrs) {
			if(r.getDate() > birthDate) {
				for(Register f : all)
					f.unlock();
				throw new AbortException();
			}
		}
		// commitDate = clock.getAndIncrement();
		commitDate = clock.incrementAndGet();
		for(Register r : lws) {
			//r.setDate(commitDate);
			r.commit(commitDate);
		}

		commited = true;

		for(Register r : all)
			r.unlock();
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