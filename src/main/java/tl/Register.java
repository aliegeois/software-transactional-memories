package tl;

public interface Register<T> extends Comparable<Register<T>> {
	public T read(Transaction t) throws AbortException;
	public void write(Transaction t, T v) throws AbortException;
	public Register<T> copy();

	public int getDate();
	public void setDate(int date);

	public void lock();
	public void unlock();
}