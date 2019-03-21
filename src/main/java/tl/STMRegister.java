package tl;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class STMRegister<T> implements Register<T> {
	private class Local {
		public T value;
		public int date;
		
		public Local(T value, int date) {
			this.value = value;
			this.date = date;
		}
	}

	public static AtomicInteger nextId = new AtomicInteger(0);
	public final int id = nextId.getAndIncrement();
	public ThreadLocal<Local> lcx = new ThreadLocal<>();
	private ReentrantLock lock = new ReentrantLock();

	private volatile T value;
	private volatile int date;
	private AtomicInteger clock;
	
	public STMRegister(T value, AtomicInteger clock) {
		this.value = value;
		this.clock = clock;
		this.date = clock.get();
	}

	@Override
	public T read(Transaction t) throws AbortException {
		System.out.println("Register read");
		if(lcx.get() != null) {
			Local l = lcx.get();
			System.out.println("Register has local value: (" + l.date + ":" + l.value + ")");
			return l.value;
		} else {
			System.out.println("Register doesn't have local value, creating one...");
			lcx.set(new Local(value, date));
			System.out.println("Local value created");
			t.addReadRegister(this);
			if(lcx.get().date > t.getBirthDate()) {
				System.out.println("Register has been changed since last commit, abort!");
				throw new AbortException();
			} else {
				Local l = lcx.get();
				System.out.println("Returning local value: (" + l.date + ":" + l.value + ")");
				return l.value;
			}
		}
	}

	@Override
	public void write(Transaction t, T v) throws AbortException {
		System.out.println("Register write");
		if(lcx.get() == null) {
			System.out.println("Register doesn't have local value, creating one...");
			lcx.set(new Local(value, date));
			System.out.println("Local value created");
		}
		Local l = lcx.get();
		System.out.println("Changing local value from " + l.value + " to " + v);
		//l.value = v;
		lcx.set(new Local(v, lcx.get().date));
		System.out.println("Local value changed to " + v);
		t.addWriteRegister(this);
	}

	@Override
	public STMRegister<T> copy() {
		return new STMRegister<T>(value, clock);
	}

	@Override
	public int compareTo(Register<T> o) {
		return id - ((STMRegister<T>)o).id;
	}

	@Override
	public int getDate() {
		return date;
	}

	@Override
	public void setDate(int ndate) {
		date = ndate;
	}

	@Override
	public void commit(int ndate) {
		date = ndate;
		value = lcx.get().value;
		lcx.set(null);
	}

	@Override
	public void reset() {
		lcx.set(null);
	}

	@Override
	public void lock() {
		lock.lock();
	}

	@Override
	public void unlock() {
		lock.unlock();
	}
}