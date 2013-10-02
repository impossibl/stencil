package com.impossibl.stencil.engine.internal;

import java.io.Serializable;

import com.impossibl.stencil.api.Template;
import com.impossibl.stencil.engine.parsing.StencilParser.TemplateContext;

public class TemplateImpl implements Template, Serializable {

  private static final long serialVersionUID = -4977880299063133293L;
  
  private String path;
  private TemplateContext context;
  
  public TemplateImpl(String path, TemplateContext context) {
    this.path = path;
    this.context = context;
  }

  public String getPath() {
    return path;
  }
  
  public void setPath(String path) {
    this.path = path;
  }
  
  public TemplateContext getContext() {
    return context;
  }

  public void setContext(TemplateContext context) {
    this.context = context;
  }

}
