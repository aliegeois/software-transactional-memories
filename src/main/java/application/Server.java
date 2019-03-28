package application;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import tl.Register;
import tl.STMRegister;

public class Server {
	private List<Register<Integer>> registers = new ArrayList<>();

	public Server(AtomicInteger clock, int nbRegisters) {
		for(int i = 0; i < nbRegisters; i++)
			registers.add(new STMRegister<Integer>(0, clock));
	}

	public Register<Integer> get(int i) {
		if(i >= 0 && i < registers.size()) 
			return registers.get(i);
		return null;
	}
}