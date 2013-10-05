package com.impossibl.stencil.engine;

import static com.impossibl.stencil.engine.internal.Contexts.value;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
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
import com.impossibl.stencil.engine.parsing.StencilLexer;
import com.impossibl.stencil.engine.parsing.StencilParser;
import com.impossibl.stencil.engine.parsing.StencilParser.HeaderContext;
import com.impossibl.stencil.engine.parsing.StencilParser.HeaderImportContext;
import com.impossibl.stencil.engine.parsing.StencilParser.ImporterContext;
import com.impossibl.stencil.engine.parsing.StencilParser.TemplateContext;
import com.impossibl.stencil.engine.parsing.StencilParser.TemplateImporterContext;

public class StencilEngine {
	
  private static final Logger logger = LogManager.getLogger(StencilEngine.class);
  
  private static final ServiceLoader<GlobalScope> serviceGlobalScopes = ServiceLoader.load(GlobalScope.class);

  private TemplateCache cache;
  private TemplateSourceLoader sourceLoader;
  private boolean completeModificationCheck = true;
  private Iterable<GlobalScope> globalScopes = serviceGlobalScopes;

  public StencilEngine() {
    this(new URLTemplateSourceLoader());
  }
  
  public StencilEngine(TemplateSourceLoader templateSourceLoader) {
    this(templateSourceLoader, new BasicTemplateCache(Integer.MAX_VALUE));
  }
  
  public StencilEngine(TemplateSourceLoader templateSourceLoader, TemplateCache templateCache) {
    this.cache = templateCache;
    this.sourceLoader = templateSourceLoader;
  }
  
  
  public TemplateCache getCache() {
    return cache;
  }

  public void setCache(TemplateCache cache) {
    this.cache = cache;
  }

  public boolean isCompleteModificationCheck() {
    return completeModificationCheck;
  }

  public void setCompleteModificationCheck(boolean completeModificationCheck) {
    this.completeModificationCheck = completeModificationCheck;
  }

  public Iterable<GlobalScope> getGlobalScopes() {
    return globalScopes;
  }

  public void setGlobalScopes(Iterable<GlobalScope> globalScopes) {
    this.globalScopes = Lists.newArrayList(Iterables.concat(globalScopes, serviceGlobalScopes));
  }

  public void render(String path, Map<String, Object> parameters, Writer out) throws IOException, ParseException {
  	render(load(path), parameters, out);
  }
  
  public void renderInline(String text, Map<String, Object> parameters, Writer out) throws IOException, ParseException {
    render(loadInline(text), parameters, out);
  }
  
  public void render(Template template, Map<String, Object> parameters, Writer out) throws IOException {
    newInterpreter().declare(parameters).process((TemplateImpl) template, out);
  }
	  
  public void render(String path, Writer out) throws IOException, ParseException {
    render(load(path), out);
  }
	  
  public void renderInline(String text, Writer out) throws IOException, ParseException {
    render(loadInline(text), out);
  }
  
  public void render(Template template, Writer out) throws IOException {
    newInterpreter().process((TemplateImpl) template, out);
  }

  public String render(String path, Map<String, Object> parameters) throws IOException, ParseException {
    return render(load(path), parameters);
  }
  
  public String renderInline(String text, Map<String, Object> parameters) throws IOException, ParseException {
    return render(loadInline(text), parameters);
  }
  
  public String render(Template template, Map<String, Object> parameters) throws IOException {  
    StringWriter out = new StringWriter();
    render(template, parameters, out);
    return out.toString();
  }
  
  public String render(String path) throws IOException, ParseException {
    return render(load(path));
  }
  
  public String renderInline(String text) throws IOException, ParseException {
    return render(loadInline(text));
  }
  
  public String render(Template template) throws IOException {  
    StringWriter out = new StringWriter();
    render(template, out);
    return out.toString();
  }
  
  public Template loadInline(String text) throws IOException, ParseException {
    
		try(InlineTemplateSource templateSource = new InlineTemplateSource(text)) {
		
			return load("inline", templateSource);
		}
		
  }
  
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
  
  private StencilInterpreter newInterpreter() {
    return new StencilInterpreter(this, globalScopes);
  }

  private TemplateContext parse(Reader reader) throws IOException, ParseException {
    
    ANTLRInputStream inputStream = new ANTLRInputStream(reader);
    
    StencilLexer lexer = new StencilLexer(inputStream);
    
    CommonTokenStream tokenStream = new CommonTokenStream(lexer);
    
    StencilParser parser = new StencilParser(tokenStream);
    parser.setErrorHandler(new BailErrorStrategy());

    try {
      return parser.template();
    }
    catch(ParseCancellationException e) {
      RecognitionException re = (RecognitionException) e.getCause();
      throw new ParseException("syntax error at " + re.getOffendingToken().getText(), re.getOffendingToken().getLine(), re.getOffendingToken().getCharPositionInLine()+1);
    }
  }

}
