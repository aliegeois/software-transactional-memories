package application;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import tl.AbortException;
import tl.Register;
import tl.STMTransaction;
import tl.Transaction;

public class Client {
	private AtomicInteger clock;

	private List<Register<Integer>> registers = new ArrayList<>();

	public Client(Server server, AtomicInteger clock, int nbRegisters) {
		this.clock = clock;

		int k = 0;
		List<Integer> registersTaken = new ArrayList<>();
		for(int i = 0; i < nbRegisters; i++)
			registersTaken.add(k++);
		
		// System.out.println(registersTaken);
		for(int i = 0; i < nbRegisters; i++)
			this.registers.add(server.get(registersTaken.remove((int)(Math.random() * registersTaken.size()))));
	}

	public void start() {
		Transaction t = new STMTransaction(clock);
		while(!t.isCommited()) {
			try {
				t.begin();
				for(int i = 0; i < registers.size(); i++) {
					Register<Integer> r = registers.get(i);
					r.write(t, r.read(t) + 1);
				}
				t.tryToCommit();
			} catch(AbortException e) {
				// e.printStackTrace();
			}
		}
	}
}