package com.impossibl.stencil.api;

import java.io.Serializable;
import java.net.URI;

import com.impossibl.stencil.engine.parsing.StencilParser.TemplateContext;

/**
 * Template caching interface
 * 
 * @author kdubb
 *
 */
public interface TemplateCache {
  
  
  /**
   * Template cache entry
   * 
   * @author kdubb
   *
   */
  public class CachedTemplate implements Serializable {
    
    private static final long serialVersionUID = 8024941619938082030L;
    
    private TemplateContext templateContext;
    private String tag;
    
    public CachedTemplate(TemplateContext templateContext, String tag) {
      this.templateContext = templateContext;
      this.tag = tag;
    }

    /**
     * Get parsed template context for the entry
     * 
     * @return Parsed template context for the entry
     */
    public TemplateContext getTemplateContext() {
      return templateContext;
    }
    
    /**
     * Set parsed template context for the entry
     * 
     * @param template Parsed template context for the entry
     */
    public void setTemplateContext(TemplateContext template) {
      this.templateContext = template;
    }
    
    /**
     * Get template source tag for the entry
     * 
     * @return Template source tag for the entry 
     */
    public String getTag() {
      return tag;
    }
    
    /**
     * Set template source tag for the entry
     * 
     * @param tag Template source tag for the entry
     */
    public void setTag(String tag) {
      this.tag = tag;
    }
    
  }
  
  /**
   * Get a cached version of the template for the given URI
   * 
   * @param uri Absolute URI of cached template to retrieve
   * @return Cached version of the template associated with the given URI
   */
  CachedTemplate get(URI uri);
  
  /**
   * Updates the cache at the given URI key with a new cached template
   * 
   * @param uri Absolute URI as cache key
   * @param cachedTemplate New template information to use as cache entry
   * @return Old cache entry
   */
  CachedTemplate update(URI uri, CachedTemplate cachedTemplate);
  
  /**
   * Removes the cached template for the given cache URI key
   * 
   * @param uri Absolute URI as cache key
   */
  void remove(URI uri);

}
