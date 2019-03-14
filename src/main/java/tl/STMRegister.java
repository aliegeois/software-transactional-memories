package tl;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
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
	public ThreadLocal<Local> lcx = null;

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
		if(lcx.get() != null) {
			return lcx.get().value;
		} else {
			lcx.set(new Local(value, date));
			t.addReadRegister(this);
			if(lcx.get().date > t.getBirthDate()) {
				throw new AbortException();
			} else {
				return lcx.get().value;
			}
		}
	}

	@Override
	public void write(Transaction t, T v) throws AbortException {
		if(lcx.get() == null) {
			lcx.set(new Local(value, date));
		}
		lcx.get().value = v;
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
	public void lock() {
		lock.lock();
	}

	@Override
	public void unlock() {
		lock.unlock();
	}
}