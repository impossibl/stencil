package com.impossibl.stencil.engine;

public class InvocationException extends ExecutionException {

  private static final long serialVersionUID = -5527033329538312329L;

  public InvocationException() {
	}

	public InvocationException(String message, ExecutionLocation location, Throwable cause) {
		super(message, location, cause);
	}

  public InvocationException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvocationException(String message, ExecutionLocation location) {
    super(message, location);
  }

	public InvocationException(String message) {
		super(message);
	}

	public InvocationException(Throwable cause) {
		super(cause);
	}

}
