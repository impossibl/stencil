package com.impossibl.stencil.api;


/**
 * Resolved & loaded template.
 * 
 * Note: This interface is returned from @see StencilEngine load methods and
 * is used as input back into the @see StencilEngine render methods.
 * 
 * @author kdubb
 *
 */
public interface Template {
  
  /**
   * Get the path the template was loaded from.
   * 
   * @return Path the template was loaded from 
   */
  String getPath();
  
}
