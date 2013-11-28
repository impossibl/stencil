package com.impossibl.stencil.engine;

public class UndefinedTypeException extends ExecutionException {

  private static final long serialVersionUID = 7955978535416096683L;

  public UndefinedTypeException() {
  }

  public UndefinedTypeException(String message, Throwable cause, ExecutionLocation location) {
    super(message, cause, location);
  }

  public UndefinedTypeException(String message, ExecutionLocation location) {
    super(message, location);
  }

  public UndefinedTypeException(Throwable cause, ExecutionLocation location) {
    super(cause, location);
  }

}
