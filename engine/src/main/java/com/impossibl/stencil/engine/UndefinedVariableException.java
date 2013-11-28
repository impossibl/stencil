package com.impossibl.stencil.engine;

public class UndefinedVariableException extends ExecutionException {

  private static final long serialVersionUID = 7955978535416096683L;

  public UndefinedVariableException() {
  }

  public UndefinedVariableException(String message, Throwable cause, ExecutionLocation location) {
    super(message, cause, location);
  }

  public UndefinedVariableException(String message, ExecutionLocation location) {
    super(message, location);
  }

  public UndefinedVariableException(Throwable cause, ExecutionLocation location) {
    super(cause, location);
  }

}
