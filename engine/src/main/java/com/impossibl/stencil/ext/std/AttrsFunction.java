package com.impossibl.stencil.ext.std;

import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.impossibl.stencil.api.Named;
import com.impossibl.stencil.api.impl.AnnotatedCallableBase;

@Named("attrs")
public class AttrsFunction extends AnnotatedCallableBase {
  
  public Object doCall(@Named("*") Map<String,Object> attrs, @Named("nulls") Boolean includeNulls) {

    includeNulls = includeNulls != null ? includeNulls : false;
    
    if(!includeNulls) {
      attrs = Maps.filterValues(attrs, new Predicate<Object>() {
  
        @Override
        public boolean apply(Object input) {
          return input != null;
        }
        
      });
    }
        
    attrs = Maps.transformValues(attrs, new Function<Object,Object>() {

      @Override
      public String apply(Object value) {
        return "\"" + value.toString() + "\"";
      }
      
    });

    return Joiner.on(" ").withKeyValueSeparator("=").join(attrs);
  }
  
}
