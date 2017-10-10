package com.impossibl.stencil.ext.std;

import com.impossibl.stencil.api.ExtensionMethodsDiscovery;



/**
 * Created by kdubb on 1/9/17.
 */
public class StandardExtensionMethodsDiscovery implements ExtensionMethodsDiscovery {

  @Override
  public Class<?>[] getExtensionMethodProviders() {
    return new Class<?>[] {
        CollectionExtensionMethods.class,
        HtmlExtensionMethods.class,
    };
  }
}
