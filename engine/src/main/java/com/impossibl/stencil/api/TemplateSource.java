package com.impossibl.stencil.api;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;

public interface TemplateSource extends Closeable {
  
  public URI getURI() throws IOException;
  public Reader openReader() throws IOException;
  public String getTag() throws IOException;
  
}
