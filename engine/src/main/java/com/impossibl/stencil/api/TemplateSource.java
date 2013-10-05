package com.impossibl.stencil.api;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;

/**
 * Template source information. Returned from @see TemplateSourceLoader.find
 * 
 * @author kdubb
 *
 */
public interface TemplateSource extends Closeable {
  
  /**
   * Get the absolute URI of template source.
   * 
   * @return Absolute URI of template source
   * @throws IOException
   */
  public URI getURI() throws IOException;
  
  /**
   * Opens a reader for the template source text.
   * 
   * @return Reader for the template source text
   * @throws IOException
   */
  public Reader openReader() throws IOException;
  
  /**
   * Get a unique tag for the template source. Generally a hash (MD5, SHA1,
   * etc) of the source text.
   * 
   * @return Unique tag for the source text
   * @throws IOException
   */
  public String getTag() throws IOException;
  
}
