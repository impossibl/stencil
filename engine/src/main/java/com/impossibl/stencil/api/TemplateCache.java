package com.impossibl.stencil.api;

import java.io.Serializable;
import java.net.URI;

import com.impossibl.stencil.engine.parsing.StencilParser.TemplateContext;

public interface TemplateCache {
  
  public class CachedTemplate implements Serializable {
    
    private static final long serialVersionUID = 8024941619938082030L;
    
    private TemplateContext templateContext;
    private String tag;
    
    public CachedTemplate(TemplateContext templateContext, String tag) {
      this.templateContext = templateContext;
      this.tag = tag;
    }

    public TemplateContext getTemplateContext() {
      return templateContext;
    }
    
    public void setTemplateContext(TemplateContext template) {
      this.templateContext = template;
    }
    
    public String getTag() {
      return tag;
    }
    
    public void setTag(String tag) {
      this.tag = tag;
    }
    
  }
  
  CachedTemplate get(URI uri);
  CachedTemplate update(URI uri, CachedTemplate cachedTemplate);
  void remove(URI uri);

}
