package com.impossibl.stencil.engine;

import static com.impossibl.stencil.engine.internal.Contexts.value;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.impossibl.stencil.api.GlobalScope;
import com.impossibl.stencil.api.Template;
import com.impossibl.stencil.api.TemplateCache;
import com.impossibl.stencil.api.TemplateCache.CachedTemplate;
import com.impossibl.stencil.api.TemplateSource;
import com.impossibl.stencil.api.TemplateSourceLoader;
import com.impossibl.stencil.api.impl.BasicTemplateCache;
import com.impossibl.stencil.api.impl.InlineTemplateSource;
import com.impossibl.stencil.api.impl.URLTemplateSourceLoader;
import com.impossibl.stencil.engine.internal.Paths;
import com.impossibl.stencil.engine.internal.StencilInterpreter;
import com.impossibl.stencil.engine.internal.TemplateImpl;
import com.impossibl.stencil.engine.parsing.StencilErrorStrategy;
import com.impossibl.stencil.engine.parsing.StencilLexer;
import com.impossibl.stencil.engine.parsing.StencilParser;
import com.impossibl.stencil.engine.parsing.StencilParser.HeaderContext;
import com.impossibl.stencil.engine.parsing.StencilParser.HeaderImportContext;
import com.impossibl.stencil.engine.parsing.StencilParser.ImporterContext;
import com.impossibl.stencil.engine.parsing.StencilParser.TemplateContext;
import com.impossibl.stencil.engine.parsing.StencilParser.TemplateImporterContext;

/**
 * Loading and rendering templates
 * 
 * @author kdubb
 *
 */
public class StencilEngine {
	
  private static final Logger logger = LogManager.getLogger(StencilEngine.class);
  
  private static final ServiceLoader<GlobalScope> serviceGlobalScopes = ServiceLoader.load(GlobalScope.class);

  private TemplateCache cache;
  private TemplateSourceLoader sourceLoader;
  private boolean completeModificationCheck = true;
  private Iterable<GlobalScope> globalScopes = serviceGlobalScopes;

  /**
   * Constructs engine with default source loader and cache
   */
  public StencilEngine() {
    this(new URLTemplateSourceLoader());
  }
  
  /**
   * Constructs engine with given source loader and default cache
   * 
   * @param templateSourceLoader Template source loader
   */
  public StencilEngine(TemplateSourceLoader templateSourceLoader) {
    this(templateSourceLoader, new BasicTemplateCache(Integer.MAX_VALUE));
  }
  
  /**
   * Constructs engine with given source loader and given cache
   * 
   * @param templateSourceLoader Template source loader
   * @param templateCache Template cache
   */
  public StencilEngine(TemplateSourceLoader templateSourceLoader, TemplateCache templateCache) {
    this.cache = templateCache;
    this.sourceLoader = templateSourceLoader;
  }
  
  /**
   * Gets template cache
   * 
   * @return Template cache
   */
  public TemplateCache getCache() {
    return cache;
  }

  /**
   * Sets the template cache
   * 
   * @param cache New template cache
   */
  public void setCache(TemplateCache cache) {
    this.cache = cache;
  }

  /**
   * Determines if the "Complete Modification Check" is enabled
   * 
   * The "Complete Modification Check" checks imported and included templates
   * for modification when checking a template form modification.
   * 
   * @return True if check is enabled, false otherwise
   */
  public boolean isCompleteModificationCheck() {
    return completeModificationCheck;
  }

  /**
   * Enables or disables the "Complete Modification Check".
   * 
   * @param completeModificationCheck New value for check
   * @see StencilEngine#isCompleteModificationCheck()
   */
  public void setCompleteModificationCheck(boolean completeModificationCheck) {
    this.completeModificationCheck = completeModificationCheck;
  }

  /**
   * Gets the active global scopes
   * 
   * @return Active global scopes
   */
  public Iterable<GlobalScope> getGlobalScopes() {
    return globalScopes;
  }

  /**
   * Sets the active global scopes
   * 
   * @param globalScopes New active global scopes
   */
  public void setGlobalScopes(Iterable<GlobalScope> globalScopes) {
    this.globalScopes = Lists.newArrayList(Iterables.concat(globalScopes, serviceGlobalScopes));
  }

  /**
   * Renders template loaded from the given path with provided parameters to
   * the given character stream.
   * 
   * @param path Path to load template from
   * @param parameters Parameters to pass to template
   * @param out Character stream to write to
   * @param extraGlobalScopes Any extra global scopes to make available
   * @throws IOException
   * @throws ParseException
   */
  public void render(String path, Map<String, Object> parameters, Writer out, GlobalScope... extraGlobalScopes) throws IOException, ParseException {
  	render(load(path), parameters, out);
  }
  
  /**
   * Renders given text with the provided parameters to the given character
   * stream.
   * 
   * @param text Template text to render
   * @param parameters Parameters to pass to template
   * @param out Character stream to write to
   * @param extraGlobalScopes Any extra global scopes to make available
   * @throws IOException
   * @throws ParseException
   */
  public void renderInline(String text, Map<String, Object> parameters, Writer out, GlobalScope... extraGlobalScopes) throws IOException, ParseException {
    render(loadInline(text), parameters, out, extraGlobalScopes);
  }
  
  /**
   * Renders given template with provided parameters to the given character
   * stream.
   * 
   * @param template Previously loaded template to render
   * @param parameters Parameters to pass to template
   * @param out Character stream to write to
   * @param extraGlobalScopes Any extra global scopes to make available
   * @throws IOException
   */
  public void render(Template template, Map<String, Object> parameters, Writer out, GlobalScope... extraGlobalScopes) throws IOException {
    newInterpreter(extraGlobalScopes).declare(parameters).process((TemplateImpl) template, out);
  }
	  
  /**
   * Readers template loaded from the given path to the given character
   * stream.
   * 
   * @param path Path to load template from
   * @param out Character stream to write to
   * @param extraGlobalScopes Any extra global scopes to make available
   * @throws IOException
   * @throws ParseException
   */
  public void render(String path, Writer out, GlobalScope... extraGlobalScopes) throws IOException, ParseException {
    render(load(path), out, extraGlobalScopes);
  }
	  
  /**
   * Renders provided text with the provided parameters to the given character
   * stream.
   * 
   * @param text Template text to render
   * @param out Character stream to write to
   * @param extraGlobalScopes Any extra global scopes to make available
   * @throws IOException
   * @throws ParseException
   */
  public void renderInline(String text, Writer out, GlobalScope... extraGlobalScopes) throws IOException, ParseException {
    render(loadInline(text), out, extraGlobalScopes);
  }
  
  /**
   * Renders given template to the given character stream.
   * 
   * @param template Previously loaded template to render
   * @param out Character stream to write to
   * @param extraGlobalScopes Any extra global scopes to make available
   * @throws IOException
   */
  public void render(Template template, Writer out, GlobalScope... extraGlobalScopes) throws IOException {
    newInterpreter(extraGlobalScopes).process((TemplateImpl) template, out);
  }

  /**
   * Renders template loaded from path with the provided parameters and returns
   * the rendered text.
   * 
   * @param path Path to load template from
   * @param parameters Parameters to pass to template
   * @param extraGlobalScopes Any extra global scopes to make available
   * @return Rendered text
   * @throws IOException
   * @throws ParseException
   */
  public String render(String path, Map<String, Object> parameters, GlobalScope... extraGlobalScopes) throws IOException, ParseException {
    return render(load(path), parameters);
  }
  
  /**
   * Renders given text with provided parameters and returns rendered text.
   * 
   * @param text Template text to render
   * @param parameters Parameters to pass to template
   * @param extraGlobalScopes Any extra global scopes to make available
   * @return Rendered text
   * @throws IOException
   * @throws ParseException
   */
  public String renderInline(String text, Map<String, Object> parameters, GlobalScope... extraGlobalScopes) throws IOException, ParseException {
    return render(loadInline(text), parameters, extraGlobalScopes);
  }
  
  /**
   * Renders given template with the provided parameters and returns the
   * rendered text.
   * 
   * @param template Previously loaded template to render
   * @param parameters Parameters to pass to template
   * @param extraGlobalScopes Any extra global scopes to make available
   * @return Rendered text
   * @throws IOException
   */
  public String render(Template template, Map<String, Object> parameters, GlobalScope... extraGlobalScopes) throws IOException {  
    StringWriter out = new StringWriter();
    render(template, parameters, out, extraGlobalScopes);
    return out.toString();
  }
  
  /**
   * Renders template loaded from path and returns rendered text.
   * 
   * @param path Path to load template from
   * @param extraGlobalScopes Any extra global scopes to make available
   * @return Rendered text
   * @throws IOException
   * @throws ParseException
   */
  public String render(String path, GlobalScope... extraGlobalScopes) throws IOException, ParseException {
    return render(load(path), extraGlobalScopes);
  }
  
  /**
   * Renders given text and returns rendered text.
   * 
   * @param text Template text to render
   * @param extraGlobalScopes Any extra global scopes to make available
   * @return Rendered text
   * @throws IOException
   * @throws ParseException
   */
  public String renderInline(String text, GlobalScope... extraGlobalScopes) throws IOException, ParseException {
    return render(loadInline(text), extraGlobalScopes);
  }
  
  /**
   * Renders given template and returns rendered text.
   * 
   * @param template Previously loaded template to render
   * @param extraGlobalScopes Any extra global scopes to make available
   * @return Rendered text
   * @throws IOException
   */
  public String render(Template template, GlobalScope... extraGlobalScopes) throws IOException {  
    StringWriter out = new StringWriter();
    render(template, out, extraGlobalScopes);
    return out.toString();
  }
  
  /**
   * Loads the given text as a template
   * @param text Template text
   * @return Loaded template
   * @throws IOException
   * @throws ParseException
   */
  public Template loadInline(String text) throws IOException, ParseException {
    
		try(InlineTemplateSource templateSource = new InlineTemplateSource(text)) {
		
			return load("inline", templateSource);
		}
		
  }
  
  /**
   * Loads a template from the given path
   * 
   * @param path Path to load template from
   * @return Loaded template
   * @throws IOException
   * @throws ParseException
   */
  public Template load(String path) throws IOException, ParseException {
  	
    try(TemplateSource source = sourceLoader.find(path)) {

  		return load(path, source);
  	}
  }
  
  private Template load(String path, TemplateSource source) throws IOException, ParseException {
  	
    logger.info("loading '{}' from '{}'", path, source);
    
    URI uri = source.getURI();
  	
		TemplateCache.CachedTemplate cachedTemplate = cache.get(uri);
		
		if(checkModified(path, source, cachedTemplate)) {
  		cachedTemplate = reload(uri, source);
		}
		
		return new TemplateImpl(path, cachedTemplate.getTemplateContext());
  }
 
	private CachedTemplate reload(URI uri, TemplateSource templateSource) throws IOException, ParseException {

    logger.debug("reloading '{}'", templateSource);
    
		try (Reader sourceReader = templateSource.openReader()) {

		  TemplateContext context = parse(sourceReader);

			if (context.exception != null) {
				throw new ExecutionException("syntax error", context.exception);
			}

			return cache.update(uri, new CachedTemplate(context, templateSource.getTag()));
		}

	}

	private boolean checkModified(String path, TemplateSource source, CachedTemplate cachedTemplate) throws IOException {
	  
    logger.trace("checking '{}' for modification", path);

    boolean modified = false;
    
    if(cachedTemplate != null && completeModificationCheck) {
  	
      logger.trace("checking '{}' imports for modification", path);
  
      //
      //Check imports
      //
      
      TemplateContext template = cachedTemplate.getTemplateContext();
      if(template.header() != null) {
        
      	HeaderContext header = template.header();
      	Iterator<HeaderImportContext> headerImportsIter = header.headerImport().iterator();
      	
        while(headerImportsIter.hasNext() && !modified) {
        	
        	HeaderImportContext headerImport = headerImportsIter.next();
  
          Iterator<ImporterContext> importersIter = headerImport.importer().iterator();
          
          while(importersIter.hasNext() && !modified) {
  
            ImporterContext importer = importersIter.next();
  
            //Only check template imports
        	
            if(importer instanceof TemplateImporterContext) {
              
              TemplateImporterContext templateImporter = (TemplateImporterContext) importer;
  
              //Resolve import path
          	
              String relImportPath = value(templateImporter.stringLiteral());
              String importPath = Paths.resolvePath(path, relImportPath);
          	
              //Find import source
          	
              try(TemplateSource importSource = sourceLoader.find(importPath)) {
            
                if(importSource == null) {
                  //No source... consider it modified
                  modified = true;
                }
                else {
                  //Coalesce further... 
                  CachedTemplate cachedImport = cache.get(importSource.getURI());
                  modified |= checkModified(importPath, importSource, cachedImport);
                }
              }
              
            }
          	
          }
  
        }
        
      }
      
      modified |= cachedTemplate.getTag().equals(source.getTag()) == false;
      
    }
    else {
      modified = cachedTemplate == null;
    }
    
    if(logger.isTraceEnabled()) {
      logger.trace("'{}' {} been modified", path, modified ? "has" : "has not");
    }
    
    return modified;
  }
  
  private StencilInterpreter newInterpreter(GlobalScope... extraGlobalScopes) {
    return new StencilInterpreter(this, Iterables.concat(Arrays.asList(extraGlobalScopes), globalScopes));
  }

  private TemplateContext parse(Reader reader) throws IOException, ParseException {
    
    ANTLRInputStream inputStream = new ANTLRInputStream(reader);
    
    StencilLexer lexer = new StencilLexer(inputStream);
    
    CommonTokenStream tokenStream = new CommonTokenStream(lexer);
    
    StencilParser parser = new StencilParser(tokenStream);
    parser.setErrorHandler(new StencilErrorStrategy());

    try {
      return parser.template();
    }
    catch(ParseCancellationException e) {
      RecognitionException re = (RecognitionException) e.getCause();
      throw new ParseException("syntax error at " + re.getOffendingToken().getText(), re.getOffendingToken().getLine(), re.getOffendingToken().getCharPositionInLine()+1);
    }
  }

}
