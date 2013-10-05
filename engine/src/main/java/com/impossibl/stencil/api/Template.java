package com.impossibl.stencil.api;


/**
 * Resolved & loaded template.
 * 
 * Note: This interface is returned from StencilEngine load methods and
 * is used as input back into the StencilEngine render methods.
 * 
 * @author kdubb
 * @see com.impossibl.stencil.engine.StencilEngine#load(String)
 * @see com.impossibl.stencil.engine.StencilEngine#loadInline(String)
 * @see com.impossibl.stencil.engine.StencilEngine#render(Template)
 * @see com.impossibl.stencil.engine.StencilEngine#render(Template, java.util.Map)
 * @see com.impossibl.stencil.engine.StencilEngine#render(Template, java.io.Writer)
 * @see com.impossibl.stencil.engine.StencilEngine#render(Template, java.util.Map, java.io.Writer)
 */
public interface Template {
  
  /**
   * Get the path the template was loaded from.
   * 
   * @return Path the template was loaded from 
   */
  String getPath();
  
}
