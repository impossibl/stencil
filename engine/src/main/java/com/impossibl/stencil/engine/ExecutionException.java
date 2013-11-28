package com.impossibl.stencil.engine;

public class ExecutionException extends RuntimeException {

  private static final long serialVersionUID = 376006343757200461L;
  
  ExecutionLocation location;

  public ExecutionException() {
  }

  public ExecutionException(String message, Throwable cause, ExecutionLocation location) {
    super(location + ": " + message, cause);
    this.location = location;
  }
  
  public ExecutionException(String message, ExecutionLocation location) {
    super(location + ": " + message);
    this.location = location;
  }

  public ExecutionException(Throwable cause, ExecutionLocation location) {
    super(cause);
    this.location = location;
  }

  public ExecutionLocation getLocation() {
    return location;
  }

  public void setLocation(ExecutionLocation location) {
    this.location = location;
  }

}
