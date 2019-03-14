package tl;

public interface Transaction {
	public void begin();
	public void tryToCommit() throws AbortException;
	public boolean isCommited();

	public void addReadRegister(Register r);
	public void addWriteRegister(Register r);

	public int getBirthDate();
}