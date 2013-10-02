package com.impossibl.stencil.engine;

public class UndefinedTypeException extends ExecutionException {

  private static final long serialVersionUID = 7955978535416096683L;

  public UndefinedTypeException() {
  }

  public UndefinedTypeException(String message, Throwable cause) {
    super(message, cause);
  }

  public UndefinedTypeException(String message) {
    super(message);
  }

  public UndefinedTypeException(Throwable cause) {
    super(cause);
  }

}
