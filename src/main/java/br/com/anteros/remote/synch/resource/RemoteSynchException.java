package br.com.anteros.remote.synch.resource;

public class RemoteSynchException extends RuntimeException {

	public RemoteSynchException() {
	}

	public RemoteSynchException(String message) {
		super(message);
	}

	public RemoteSynchException(Throwable cause) {
		super(cause);
	}

	public RemoteSynchException(String message, Throwable cause) {
		super(message, cause);
	}

	public RemoteSynchException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
