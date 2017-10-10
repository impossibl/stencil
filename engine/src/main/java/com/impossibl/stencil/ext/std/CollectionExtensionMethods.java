package com.impossibl.stencil.ext.std;

import java.util.Collection;



/**
 * Created by kdubb on 1/9/17.
 */
public class CollectionExtensionMethods {

  public static Object first(Collection<?> values) {
    if(values.isEmpty()) {
      return null;
    }
    return values.iterator().next();
  }

}
