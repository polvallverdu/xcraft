package dev.polv.xcraft.forge;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * Runs {@code /xcraft test} on the server thread. Before 1.12 the server runs RCON commands on the RCON thread,
 * and the test changes the world (entities, drops), which must happen on the server thread.
 */
public final class ServerThread {
	private static final Queue<Runnable> TASKS = new ConcurrentLinkedQueue<Runnable>();
	private static volatile Thread thread;

	private ServerThread() {
	}

	/** Called from a server world tick handler. */
	public static void tick() {
		thread = Thread.currentThread();
		Runnable task;
		while ((task = TASKS.poll()) != null) task.run();
	}

	public static String call(final Commands.Test test) {
		if (thread == null || Thread.currentThread() == thread) return test.run();
		FutureTask<String> task = new FutureTask<String>(new Callable<String>() {
			@Override
			public String call() {
				return test.run();
			}
		});
		TASKS.add(task);
		try {
			return task.get(30, TimeUnit.SECONDS);
		} catch (Exception e) {
			return "xcraft-test error " + e;
		}
	}
}
