package application;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import tl.AbortException;
import tl.Register;
import tl.STMTransaction;
import tl.Transaction;

public class Main {
	private static AtomicInteger clock = new AtomicInteger();
	private static int nbClients = 50;
	private static int nbRegisters = 50;
	private static int nbModifications = 15;
	public static void main(String[] args) {
		Server server = new Server(clock, nbRegisters);
		List<Thread> threads = new ArrayList<>();
		List<Client> clients = new ArrayList<>();

		for(int i = 0; i < nbClients; i++) {
			final int j = i;
			threads.add(new Thread() {
				@Override
				public void run() {
					clients.get(j).start();
				}
			});
			clients.add(new Client(server, clock, nbModifications));
		}
		for(int i = 0; i < nbClients; i++) {
			// clients.get(i).start();
			threads.get(i).start();
		}

		for(int i = 0; i < nbClients; i++) {
			try {
				threads.get(i).join();
			} catch(InterruptedException e) {
				// e.printStackTrace();
			}
		}

		int total = 0;
		Transaction t = new STMTransaction(clock);
		while(!t.isCommited()) {
			try {
				t.begin();
				for(int i = 0; i < nbRegisters; i++) {
					Register<Integer> r = server.get(i);
					total += r.read(t);
				}
				t.tryToCommit();
			} catch(AbortException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Valeur finale: " + total + ", valeur attendue: " + (nbClients * nbModifications));
	}
}
