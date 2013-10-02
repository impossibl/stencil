package com.impossibl.stencil.engine;

public class ExecutionLocation {
  
  public String file;
  public int line;
  public int column;
  
  public ExecutionLocation(String file, int line, int column) {
    this.file = file;
    this.line = line;
    this.column = column;
  }

  @Override
  public String toString() {
    return String.format("%s (%d,%d)", file, line, column);
  }
  
}
