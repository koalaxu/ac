package ac;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import ac.data.ConfigData;
import ac.data.GameData;
import ac.engine.Action;
import ac.engine.Engine;
import ac.engine.EngineRunner;
import ac.engine.ai.GameInterface;
import ac.engine.ai.RandomAI;
import ac.engine.data.DataAccessor;
import ac.ui.swing.Components;
import data.FileUtil;

public class GameConsole {
	public GameConsole() {
		FileUtil.Init();
		GameData data = GameData.Init("01_cq");
		DataAccessor accessor = new DataAccessor(data);
		config = accessor.GetConfig();
		engine = new Engine(accessor, new GameInterface(RandomAI.creator));
		engine.Init();
		components = new Components(new InitializeGame(), new LoadGame());
		components.ShowGameSelection();
		//components.SetUp(accessor, add_action, pause, resume);
	}
	
	private class Run extends Thread {
		@Override
		public void run() {
			while (!paused) {
				EngineRunner runner = new EngineRunner(engine, engine_lock, components::Repaint, action_queue);
				//System.err.println("Runner start.");
				runner.start();
				try {
					Thread.sleep(config.ms_per_day);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//System.err.println("Try to start.");
				engine_lock.lock();
				engine_lock.unlock();
				components.Repaint();
			}
		}
	}
	
	private class Pause implements Runnable {
		@Override
		public void run() {
			System.err.println("Stop");
			ui_lock.lock();
			paused = true;
			ui_lock.unlock();
			System.err.println("Stopped");
		}
		
	}
	
	private class Resume implements Runnable {
		@Override
		public void run() {
			ui_lock.lock();
			paused = false;
			ui_lock.unlock();
			new Run().start();
		}
	}
	
	private class InitializeGame implements Consumer<DataAccessor> {
		@Override
		public void accept(DataAccessor accessor) {
			engine = new Engine(accessor, new GameInterface(RandomAI.creator));
			engine.Init();
			components.SetUp(accessor, add_action, pause, resume);
		}
	}
	
	private class LoadGame implements Consumer<DataAccessor> {
		@Override
		public void accept(DataAccessor accessor) {
			engine = new Engine(accessor, new GameInterface(RandomAI.creator));
			components.SetUp(accessor, add_action, pause, resume);
		}
	}
	
	private class AddAction implements Consumer<Action> {
		@Override
		public void accept(Action action) {
			ui_lock.lock();
			if (paused) {
				engine.GetActionExecutor().Execute(action);
			} else {
				action_queue.add(action);
			}
			ui_lock.unlock();
		}
	}

	public static void main(String[] args) {
		new GameConsole();
		//console.start();
	}
	
	private ConfigData config;
	private Engine engine;
	private Components components;
	private Lock engine_lock = new ReentrantLock();
	private Lock ui_lock = new ReentrantLock();
	private Runnable pause = new Pause();
	private Runnable resume = new Resume();
	private Consumer<Action> add_action = new AddAction();
	private boolean paused = true;
	private ConcurrentLinkedQueue<Action> action_queue = new ConcurrentLinkedQueue<Action>();

}
