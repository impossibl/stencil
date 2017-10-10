package com.impossibl.stencil.api;

/**
 * Extension methods marker interface.
 * 
 * Extension methods are created by implementing this interface and creating
 * static methods in the class with at lease one parameter.  The first
 * parameter of each extension method determines the class that the method
 * extends.
 * 
 * @author kdubb
 *
 */
public interface ExtensionMethodsDiscovery {

  Class<?>[] getExtensionMethodProviders();

}
