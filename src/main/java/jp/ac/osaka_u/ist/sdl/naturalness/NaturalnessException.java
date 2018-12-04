package jp.ac.osaka_u.ist.sdl.naturalness;

public class NaturalnessException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NaturalnessException() {
	}

	public NaturalnessException(String message) {
		super(message);
	}

	public NaturalnessException(Throwable cause) {
		super(cause);
	}

	public NaturalnessException(String message, Throwable cause) {
		super(message, cause);
	}

	public NaturalnessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
