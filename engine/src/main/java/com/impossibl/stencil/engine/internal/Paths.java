package com.impossibl.stencil.engine.internal;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

public class Paths {
  
  public static String resolvePath(String baseUri, String uri) {
    
    String absUri;
    
    if(uri.startsWith("/") == false) {
      
      Splitter splitter = Splitter.on('/').omitEmptyStrings();
      
      List<String> currentParts = newArrayList(splitter.split(baseUri + "/.."));            
      List<String> newParts = newArrayList(splitter.split(uri));
      
      Iterable<String> allParts = Iterables.concat(currentParts, newParts);

      List<String> res = new ArrayList<>();
      
      for(String part : allParts) {
        switch(part) {
        case "..":
          if(!res.isEmpty())
            res.remove(res.size()-1);
          break;
        case ".":
          break;
        default:
          res.add(part);
        }
      }
      
      absUri = Joiner.on("/").join(res);
    }
    else {
      
      absUri = uri;
    }
    
    return absUri;
  }
  
}
