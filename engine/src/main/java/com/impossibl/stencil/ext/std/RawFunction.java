package com.impossibl.stencil.ext.std;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import com.impossibl.stencil.api.Block;
import com.impossibl.stencil.api.Named;
import com.impossibl.stencil.api.impl.AnnotatedCallablePreparableBase;

@Named("raw")
public class RawFunction extends AnnotatedCallablePreparableBase {
  
  public Object doCall(@Named("*") Object obj) {
    return this;
  }
  
  public Object doPrepare(@Named("*") Map<String,Block> blocks) throws IOException {
    
    StringWriter out = new StringWriter();
    
    for(Block block : blocks.values()) {
      block.write(out);
    }
    
    return out.toString();
  }

}
