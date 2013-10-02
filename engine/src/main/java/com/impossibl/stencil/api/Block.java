package com.impossibl.stencil.api;

import java.io.IOException;
import java.io.Writer;

public interface Block {
  
  void write(Writer out) throws IOException;

}
