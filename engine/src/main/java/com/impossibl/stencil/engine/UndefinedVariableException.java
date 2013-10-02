package com.impossibl.stencil.engine;

public class UndefinedVariableException extends ExecutionException {

  private static final long serialVersionUID = 7955978535416096683L;

  public UndefinedVariableException() {
  }

  public UndefinedVariableException(String message, Throwable cause) {
    super(message, cause);
  }

  public UndefinedVariableException(String message) {
    super(message);
  }

  public UndefinedVariableException(Throwable cause) {
    super(cause);
  }

}
