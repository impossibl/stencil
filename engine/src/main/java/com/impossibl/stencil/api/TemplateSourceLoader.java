package com.impossibl.stencil.api;

import java.io.IOException;

public interface TemplateSourceLoader {
  
  TemplateSource find(String path) throws IOException;

}
