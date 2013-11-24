package com.impossibl.stencil.ext.std;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import com.impossibl.stencil.api.Block;
import com.impossibl.stencil.api.Callable;
import com.impossibl.stencil.api.Named;
import com.impossibl.stencil.api.Preparable;

@Named("raw")
public class RawFunction implements Callable, Preparable {
  
  @Override
  public String[] getParameterNames() {
    return ALL_PARAMS;
  }

  @Override
  public Object call(Map<String,?> params) {
    return this;
  }

  @Override
  public String[] getBlockNames() {
    return ALL_BLOCKS;
  }
  
  @Override
  public Object prepare(Map<String,?> params) throws IOException {
    
    StringWriter out = new StringWriter();
    
    for(Map.Entry<String, ?> paramEntry : params.entrySet()) {
      
      Object param = paramEntry.getValue();
      
      if (param instanceof Block) {
        
        ((Block) param).write(out);
        
      }
      else if (param instanceof Map) {
        
        @SuppressWarnings("unchecked")
        Map<String, Block> otherBlocks = (Map<String, Block>) param;
        
        for (Block block : otherBlocks.values()) {
          block.write(out);
        }
        
      }
      
    }
    
    return out.toString();
  }

}
