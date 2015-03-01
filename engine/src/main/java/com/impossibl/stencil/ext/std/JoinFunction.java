package com.impossibl.stencil.ext.std;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Strings.nullToEmpty;

import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.impossibl.stencil.api.Named;
import com.impossibl.stencil.api.impl.AnnotatedCallableBase;

@Named("join")
public class JoinFunction extends AnnotatedCallableBase {
  
  public Object doCall(@Named("objects") Object obj, @Named("deliminator") String delim, @Named("separator") String sep, @Named("nulls") Boolean includeNulls) {

    delim = nullToEmpty(delim);
    sep = nullToEmpty(sep);
    includeNulls = firstNonNull(includeNulls,false);

    Joiner joiner = Joiner.on(delim);
    
    if (obj instanceof Map<?, ?>) {
      
      Map<?,?> map = (Map<?, ?>) obj;

      if(!includeNulls) {
        
        map = Maps.filterValues(map, new Predicate<Object>() {
  
          @Override
          public boolean apply(Object value) {
            return value != null;
          }
          
        });
        
      }
      
      return joiner.withKeyValueSeparator(sep).join(map);
    }
    else if (obj instanceof Iterable<?>) {
      
      if(!includeNulls) {
        joiner = joiner.skipNulls();
      }
      
      return joiner.join((Iterable<?>) obj);
    }

    return null;
  }
  
}
