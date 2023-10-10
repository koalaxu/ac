package ac.engine;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;

public class EngineRunner extends Thread {
	public EngineRunner(Engine engine, Lock lock, Runnable callback, ConcurrentLinkedQueue<Action> action_queue) {
		this.engine = engine;
		this.lock = lock;
		this.callback = callback;
		this.action_queue = action_queue;
	}
	
	@Override
	public void run() {
		lock.lock();
		while(!action_queue.isEmpty()) {
			Action action = action_queue.poll();
			engine.GetActionExecutor().Execute(action);
		}
		engine.Proceed();
		callback.run();
		lock.unlock();
	}
	
	private Engine engine;
	private Lock lock;
	private Runnable callback;
	private ConcurrentLinkedQueue<Action> action_queue;
}