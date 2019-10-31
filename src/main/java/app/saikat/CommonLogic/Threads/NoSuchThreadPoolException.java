package app.saikat.CommonLogic.Threads;

public class NoSuchThreadPoolException extends Exception {

	private static final long serialVersionUID = 1L;

	public NoSuchThreadPoolException(String name) {
		super(String.format("No threadpool found by name %s.", name));
	}
}
