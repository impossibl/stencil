package com.impossibl.stencil.engine;

public class InvocationException extends ExecutionException {

  private static final long serialVersionUID = -5527033329538312329L;

  public InvocationException() {
	}

  public InvocationException(String message, Throwable cause, ExecutionLocation location) {
    super(message, cause, location);
  }

  public InvocationException(String message, ExecutionLocation location) {
    super(message, location);
  }

}
