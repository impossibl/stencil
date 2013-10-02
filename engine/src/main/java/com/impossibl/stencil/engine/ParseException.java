package com.impossibl.stencil.engine;

import static java.lang.String.format;

public class ParseException extends Exception {

  private static final long serialVersionUID = 7226525741369257902L;
  
  int line;
  int column;

  public ParseException() {
    super();
  }

  public ParseException(String message, int line, int column) {
    super(message);
    this.line = line;
    this.column = column;
  }

  @Override
  public String toString() {
    return format("(%d,%d): %s", line, column, getMessage());
  }

}
