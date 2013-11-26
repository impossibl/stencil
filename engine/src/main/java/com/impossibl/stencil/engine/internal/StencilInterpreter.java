package com.impossibl.stencil.engine.internal;

import static com.google.common.base.Strings.nullToEmpty;
import static com.impossibl.stencil.api.Callable.ALL_PARAM_NAME;
import static com.impossibl.stencil.api.Preparable.ALL_BLOCK_NAME;
import static com.impossibl.stencil.api.Preparable.UNNAMED_BLOCK_NAME;
import static com.impossibl.stencil.engine.internal.Contexts.mode;
import static com.impossibl.stencil.engine.internal.Contexts.name;
import static com.impossibl.stencil.engine.internal.Contexts.value;
import static com.impossibl.stencil.engine.internal.ExtensionMethodManager.getExtensionMethod;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.RuleNode;
import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import com.impossibl.stencil.api.Block;
import com.impossibl.stencil.api.Callable;
import com.impossibl.stencil.api.GlobalScope;
import com.impossibl.stencil.api.Preparable;
import com.impossibl.stencil.engine.ExecutionException;
import com.impossibl.stencil.engine.ExecutionLocation;
import com.impossibl.stencil.engine.InvocationException;
import com.impossibl.stencil.engine.ParseException;
import com.impossibl.stencil.engine.StencilEngine;
import com.impossibl.stencil.engine.UndefinedTypeException;
import com.impossibl.stencil.engine.UndefinedVariableException;
import com.impossibl.stencil.engine.parsing.ParamOutputBlockMode;
import com.impossibl.stencil.engine.parsing.StencilParser.AssignOutputContext;
import com.impossibl.stencil.engine.parsing.StencilParser.AssignmentContext;
import com.impossibl.stencil.engine.parsing.StencilParser.AssignmentStatementContext;
import com.impossibl.stencil.engine.parsing.StencilParser.BinaryExpressionContext;
import com.impossibl.stencil.engine.parsing.StencilParser.BlockDeclContext;
import com.impossibl.stencil.engine.parsing.StencilParser.BlockStatementContext;
import com.impossibl.stencil.engine.parsing.StencilParser.BooleanLiteralContext;
import com.impossibl.stencil.engine.parsing.StencilParser.BreakOutputContext;
import com.impossibl.stencil.engine.parsing.StencilParser.BreakStatementContext;
import com.impossibl.stencil.engine.parsing.StencilParser.CallSelectorContext;
import com.impossibl.stencil.engine.parsing.StencilParser.CallableInvocationContext;
import com.impossibl.stencil.engine.parsing.StencilParser.CallableSignatureContext;
import com.impossibl.stencil.engine.parsing.StencilParser.ContinueOutputContext;
import com.impossibl.stencil.engine.parsing.StencilParser.ContinueStatementContext;
import com.impossibl.stencil.engine.parsing.StencilParser.DeclarationOutputContext;
import com.impossibl.stencil.engine.parsing.StencilParser.DeclarationStatementContext;
import com.impossibl.stencil.engine.parsing.StencilParser.DynamicOutputContext;
import com.impossibl.stencil.engine.parsing.StencilParser.ExportDefinitionContext;
import com.impossibl.stencil.engine.parsing.StencilParser.ExpressionContext;
import com.impossibl.stencil.engine.parsing.StencilParser.ExpressionOutputContext;
import com.impossibl.stencil.engine.parsing.StencilParser.ExpressionStatementContext;
import com.impossibl.stencil.engine.parsing.StencilParser.FloatingLiteralContext;
import com.impossibl.stencil.engine.parsing.StencilParser.ForeachOutputContext;
import com.impossibl.stencil.engine.parsing.StencilParser.ForeachStatementContext;
import com.impossibl.stencil.engine.parsing.StencilParser.FunctionDefinitionContext;
import com.impossibl.stencil.engine.parsing.StencilParser.HeaderContext;
import com.impossibl.stencil.engine.parsing.StencilParser.IfOutputContext;
import com.impossibl.stencil.engine.parsing.StencilParser.IfStatementContext;
import com.impossibl.stencil.engine.parsing.StencilParser.IncludeOutputContext;
import com.impossibl.stencil.engine.parsing.StencilParser.IndexSelectorContext;
import com.impossibl.stencil.engine.parsing.StencilParser.InstanceExpressionContext;
import com.impossibl.stencil.engine.parsing.StencilParser.IntegerLiteralContext;
import com.impossibl.stencil.engine.parsing.StencilParser.LValueRefContext;
import com.impossibl.stencil.engine.parsing.StencilParser.ListLiteralContext;
import com.impossibl.stencil.engine.parsing.StencilParser.LiteralContext;
import com.impossibl.stencil.engine.parsing.StencilParser.LiteralExpressionContext;
import com.impossibl.stencil.engine.parsing.StencilParser.MacroDefinitionContext;
import com.impossibl.stencil.engine.parsing.StencilParser.MapLiteralContext;
import com.impossibl.stencil.engine.parsing.StencilParser.MemberIndexSelectorContext;
import com.impossibl.stencil.engine.parsing.StencilParser.MemberSelectorContext;
import com.impossibl.stencil.engine.parsing.StencilParser.MethodCallSelectorContext;
import com.impossibl.stencil.engine.parsing.StencilParser.NamedOutputBlockContext;
import com.impossibl.stencil.engine.parsing.StencilParser.NamedValueContext;
import com.impossibl.stencil.engine.parsing.StencilParser.NullLiteralContext;
import com.impossibl.stencil.engine.parsing.StencilParser.NumberLiteralContext;
import com.impossibl.stencil.engine.parsing.StencilParser.OutputBlockContext;
import com.impossibl.stencil.engine.parsing.StencilParser.OutputContext;
import com.impossibl.stencil.engine.parsing.StencilParser.ParameterDeclContext;
import com.impossibl.stencil.engine.parsing.StencilParser.ParenExpressionContext;
import com.impossibl.stencil.engine.parsing.StencilParser.PrepareInvocationContext;
import com.impossibl.stencil.engine.parsing.StencilParser.PrepareSignatureContext;
import com.impossibl.stencil.engine.parsing.StencilParser.RangeLiteralContext;
import com.impossibl.stencil.engine.parsing.StencilParser.RefSelectorContext;
import com.impossibl.stencil.engine.parsing.StencilParser.ReturnStatementContext;
import com.impossibl.stencil.engine.parsing.StencilParser.SafeMemberSelectorContext;
import com.impossibl.stencil.engine.parsing.StencilParser.SelectorContext;
import com.impossibl.stencil.engine.parsing.StencilParser.SelectorExpressionContext;
import com.impossibl.stencil.engine.parsing.StencilParser.SimpleNameContext;
import com.impossibl.stencil.engine.parsing.StencilParser.StatementContext;
import com.impossibl.stencil.engine.parsing.StencilParser.StringLiteralContext;
import com.impossibl.stencil.engine.parsing.StencilParser.SwitchOutputCaseContext;
import com.impossibl.stencil.engine.parsing.StencilParser.SwitchOutputContext;
import com.impossibl.stencil.engine.parsing.StencilParser.SwitchOutputDefaultCaseContext;
import com.impossibl.stencil.engine.parsing.StencilParser.SwitchOutputValueCaseContext;
import com.impossibl.stencil.engine.parsing.StencilParser.SwitchStatementCaseContext;
import com.impossibl.stencil.engine.parsing.StencilParser.SwitchStatementContext;
import com.impossibl.stencil.engine.parsing.StencilParser.SwitchStatementDefaultCaseContext;
import com.impossibl.stencil.engine.parsing.StencilParser.SwitchStatementValueCaseContext;
import com.impossibl.stencil.engine.parsing.StencilParser.TemplateImporterContext;
import com.impossibl.stencil.engine.parsing.StencilParser.TernaryExpressionContext;
import com.impossibl.stencil.engine.parsing.StencilParser.TextOutputContext;
import com.impossibl.stencil.engine.parsing.StencilParser.TypeImporterContext;
import com.impossibl.stencil.engine.parsing.StencilParser.UnaryExpressionContext;
import com.impossibl.stencil.engine.parsing.StencilParser.UnnamedOutputBlockContext;
import com.impossibl.stencil.engine.parsing.StencilParser.ValueSelectorContext;
import com.impossibl.stencil.engine.parsing.StencilParser.VariableDeclContext;
import com.impossibl.stencil.engine.parsing.StencilParser.VariableRefContext;
import com.impossibl.stencil.engine.parsing.StencilParser.VariableRefExpressionContext;
import com.impossibl.stencil.engine.parsing.StencilParser.WhileOutputContext;
import com.impossibl.stencil.engine.parsing.StencilParser.WhileStatementContext;
import com.impossibl.stencil.engine.parsing.StencilParser.WithOutputContext;
import com.impossibl.stencil.engine.parsing.StencilParser.WithStatementContext;
import com.impossibl.stencil.engine.parsing.StencilParserBaseVisitor;


public class StencilInterpreter {
  
  
  private static final Logger logger = LogManager.getLogger(StencilInterpreter.class);
  

  /**
   * Environment
   */
  private class Environment {

    private String path;
    private Writer out;
    
    public Environment(String path, Writer out) {
      this.path = path;
      this.out = out;
    }
    
  }

  /**
   * Scope holds values for current scope and has
   * a parent for managing a stack of scopes.
   * 
   * @author kdubb
   *
   */
  private static class Scope {
    
    private static class Null {
      @Override
      public String toString() { return "NULL"; }
    }
    private static final Null NULL = new Null();

    
    protected Scope parent;
    protected Map<String, Object> values = new HashMap<String, Object>();
    

    public Scope(Scope parent) {
      this.parent = parent;
    }
    
    protected Object unwrap(Object val) {
      return val != NULL ? val : null;
    }
    
    protected Object wrap(Object value) {
      return value != null ? value : NULL;
    }

    /**
     * Retrieves a variable from the given scope or
     * one of its parents
     * 
     * @param name Name of variable to retrieve
     * @return Value of named variable
     * @throws UndefinedVariableException when the variable is undefined
     */
    Object ref(String name) {

      Object val = values.get(name);
      if(val == null) {
        
        if (parent == null)
          throw new UndefinedVariableException(name);

        return parent.ref(name);
      }

      return unwrap(val);
    }
    
    /**
     * Retrieves a control variable from the current scope
     * or one of its parents
     * 
     * @param name Name of control variable to retrieve
     * @return Value of named variable
     */
    Object refControl(String name) {
      
      Object val = values.get(name);
      if(val == null) {
        
        if(parent == null)
          return null;
        
        return parent.refControl(name);
      }
      
      return unwrap(val);
    }

    /**
     * Declares a variable in this scope
     * 
     * @param name Name of variable to declare
     * @param value Initial value of variable
     */
    void declare(String name, Object value) {
      values.put(name, wrap(value));
    }

    /**
     * Declares variables in this scope
     * @param map Name=>Value map of variables to declare
     */
    void declare(Map<String, ?> map) {
      for(Map.Entry<String, ?> entry : map.entrySet())
        values.put(entry.getKey(), wrap(entry.getValue()));
    }

    /**
     * Assigns a value to a variable in the current scope
     * @param name Name of variable to assign value to
     * @param value Value to assign to variable
     * @throws UndefinedVariableException when the variable is undefined
     */
    void assign(String name, Object value) {
      if (values.containsKey(name))
        values.put(name, wrap(value));
      else if (parent != null)
        parent.assign(name, value);
      else
        throw new UndefinedVariableException(name);
    }

    @Override
    public String toString() {
      
      StringBuilder sb = new StringBuilder()
      .append(values.toString())
      .append(" => ");
      
      if(parent != null)
        sb.append(parent);
      
      return sb.toString();
    }
    
  }
  
  /**
   * Scope for callables that does not pass on
   * control variables
   * 
   * @author kdubb
   *
   */
  private static class CallableScope extends Scope {

    public CallableScope(Scope parent) {
      super(parent);
    }
    
    @Override
    public Object refControl(String name) {
      return unwrap(values.get(name));
    }
    
  }
  
  /**
   * Root scope that searches all provided
   * globals scopes as a last resort
   * 
   * @author kdubb
   *
   */
  private static class RootScope extends Scope {
    
    Iterable<GlobalScope> globalScopes;

    public RootScope(Iterable<GlobalScope> globalScopes) {
      super(null);
      this.globalScopes = globalScopes;
    }

    @Override
    Object ref(String name) {

      Object val = values.get(name);
      if (val == null) {

        return refGlobal(name);
      }

      return unwrap(values.get(name));
    }

    private Object refGlobal(String name) {
      
      for(GlobalScope globalScope : globalScopes) {
        
        Object val = globalScope.get(name);
        if(val != GlobalScope.NOT_FOUND)
          return val;
        
      }
      
      return null;
    }
    
  }
  
  private static class WithScope extends Scope {
    
    Iterable<Object> objects;

    public WithScope(Iterable<Object> objects) {
      super(null);
      this.objects = objects;
    }

    @Override
    Object ref(String name) {

      //Look for the variable in the local objects
      for(Object object : objects) {
        
        try {
          return PropertyUtils.getProperty(object, name);
        }
        catch (IllegalAccessException e) {
          throw new ExecutionException(e);
        }
        catch (InvocationTargetException e) {
          throw new ExecutionException(e);
        }
        catch (NoSuchMethodException e) {
          //Ignore...
        }
        
      }
      
      return super.ref(name);
    }
    
  }

  /**
   * FunctionDefinition bound to the scope in which it was defined
   * 
   * @author kdubb
   *
   */
  private class BoundFunction {

    Scope owningScope;
    FunctionDefinitionContext source;

    /**
     * Constructs a bound function
     * @param source FunctionDefinition to bind
     * @param owningScope Scope to bind to
     */
    public BoundFunction(FunctionDefinitionContext source, Scope owningScope) {
      this.owningScope = owningScope;
      this.source = source;
    }

    /**
     * Executes the referenced function in the bound scope
     * 
     * @param parameters Name=>Value map of parameters for call
     * @return Result of function execution
     */
    public Object call(Map<String, Object> parameters) {

      Scope prevScope = switchScope(owningScope);

      try {

        pushScope(new CallableScope(currentScope));

        try {
          
          //Declare return variable
          currentScope.declare(FUNC_RETURN_VAR, null);

          //Declare all parameters in the function's execution scope
          currentScope.declare(parameters);

          //Execute the function statements
          exec(source.blockStmt);
          
          //Return value (if any)
          return currentScope.refControl(FUNC_RETURN_VAR);

        }
        finally {
          popScope();
        }

      }
      finally {
        switchScope(prevScope);
      }

    }

    @Override
    public String toString() {
      return "func " + name(source);
    }
    
  }

  /**
   * MacroDefinition bound to the scope in which it was defined
   * 
   * @author kdubb
   *
   */
  private class BoundMacro {

    Scope owningScope;
    MacroDefinitionContext source;

    /**
     * Constructs a bound macro
     * @param def MacroDefinition to bind
     * @param owningScope Scope to bind to
     */
    public BoundMacro(MacroDefinitionContext def, Scope owningScope) {
      this.owningScope = owningScope;
      this.source = def;
    }

    @Override
    public String toString() {
      return "macro " + name(source);
    }
    
  }

  /**
   * Block bound to the macro and parameters/blocks passed to it
   * 
   * @author kdubb
   *
   */
  private class BoundBlock implements Block {

    BoundMacro source;
    Map<String, Object> parameters = new HashMap<String, Object>();

    /**
     * Constructs a bound block
     * @param source
     */
    public BoundBlock(BoundMacro source) {
      this.source = source;
    }

    /**
     * Binds more parameters to the block
     * @param parameters Parameters to bind to the block
     */
    public void declareParams(Map<String, ?> params) {
      
      parameters.putAll(params);
        
    }
    
    /**
     * Binds more parameters to the block
     * @param parameters Parameters to bind to the block
     */
    public void declareBlocks(Map<String, Object> blocks) {
    
      parameters.putAll(blocks);

    }

    /**
     * Evaluates the block in the scope in which its source macro was bound
     */
    public void exec() {

      Scope prevScope = switchScope(source.owningScope);

      try {

        pushScope();

        try {

          //Declare the parameters in the block's execution scope
          currentScope.declare(parameters);

          //Evaluate the block and return it's related text
          source.source.block.accept(visitor);

        }
        finally {
          popScope();
        }

      }
      finally {
        switchScope(prevScope);
      }

    }

    @Override
    public boolean getHasOutput() {
      return true;
    }

    @Override
    public void write(Writer out) throws IOException {
      
      Writer prevOut = currentEnvironment.out;
      currentEnvironment.out = out;
      
      try {
        
        exec();
        
      }
      finally {
        currentEnvironment.out = prevOut;
      }
      
    }

    @Override
    public String toString() {
      StringWriter out = new StringWriter();
      try {
        write(out);
      }
      catch (IOException e) {
        throw new ExecutionException(e);
      }
      return out.toString();
    }

  }

  /**
   * ParamOutputBlock bound to the scope in which it was defined and the
   * related replacement mode.
   * 
   * @author kdubb
   *
   */
  private class BoundParamOutputBlock implements Block {

    ParamOutputBlockMode mode;
    ParserRuleContext source;
    Scope owningScope;

    /**
     * Constructs a bound parameter block. The mode is taken from the source block
     * @param source ParamOutputBlock to bind
     * @param owningScope Scope to bind to
     */
    public BoundParamOutputBlock(ParserRuleContext source, ParamOutputBlockMode mode, Scope owningScope) {
      this.source = source;
      this.mode = mode != null ? mode : ParamOutputBlockMode.Replace;
      this.owningScope = owningScope;
    }

    /**
     * Constructs an empty bound parameter block
     * @param mode OutputBlockMode to assign
     * @param owningScope Scope to bind to
     */
    public BoundParamOutputBlock(ParamOutputBlockMode mode, Scope owningScope) {
      this.mode = mode != null ? mode : ParamOutputBlockMode.Replace;
      this.owningScope = owningScope;
    }

    /**
     * Evaluates block in the scope in which it was defined
     */
    public void exec() {
      
      if (source == null)
        return;

      Scope prevScope = switchScope(owningScope);

      try {

        //Evaluate block
        source.accept(visitor);

      }
      finally {
        switchScope(prevScope);
      }

    }
    
    @Override
    public boolean getHasOutput() {
      return source != null;
    }

    @Override
    public void write(Writer out) throws IOException {
      
      Writer prevOut = currentEnvironment.out;
      currentEnvironment.out = out;
      
      try {
        
        exec();
        
      }
      finally {
        currentEnvironment.out = prevOut;
      }
      
    }
    
    @Override
    public String toString() {
      StringWriter out = new StringWriter();
      try {
        write(out);
      }
      catch (IOException e) {
        throw new ExecutionException(e);
      }
      return out.toString();
    }

  }
  
  
  abstract class LValue {
    
    abstract Object get();
    abstract void set(Object val);
    
    LValue select(RefSelectorContext selector) {
    	if(selector instanceof SafeMemberSelectorContext) {
    		return new SafeSelectorLValue(get(), selector);
    	}
      return new SelectorLValue(get(), selector);
    }
    
  }
  
  class ScopeLValue extends LValue {

    Scope scope;
    String name;
        
    public ScopeLValue(Scope scope, String name) {
      this.scope = scope;
      this.name = name;
    }

    @Override
    Object get() {
      return scope.ref(name);
    }

    @Override
    void set(Object val) {
      scope.assign(name, val);
    }
    
  }
    
    
  class SelectorLValue extends LValue {
    
    Object source;    
    RefSelectorContext selector;
    
    SelectorLValue(Object source, RefSelectorContext selector) {
      this.source = source;
      this.selector = selector;
    }

    @Override
    Object get() {
      return visitor.select(source, selector);
    }
    
    void nullAssignment() {
      logger.error("{}: attempt to assign to null lvalue", getLocation(selector));
    }

    void set(Object val) {
      
      if(source == null) {
      	nullAssignment();
        return;
      }
      
      if(selector instanceof IndexSelectorContext) {
        IndexSelectorContext sel = (IndexSelectorContext)selector;
        setIndex(source, eval(sel.expr), val);
      }
      else if(selector instanceof MemberSelectorContext) {
        MemberSelectorContext sel = (MemberSelectorContext)selector;
        setMember(source, name(sel), false, val);
      }
      else if(selector instanceof SafeMemberSelectorContext) {
        SafeMemberSelectorContext sel = (SafeMemberSelectorContext)selector;
        setMember(source, name(sel), true, val);
      }
      else if(selector instanceof MemberIndexSelectorContext) {
        MemberIndexSelectorContext sel = (MemberIndexSelectorContext)selector;
        setMemberIndex(source, name(sel), eval(sel.expr), val);
      }
      
    }
    
    void setMemberIndex(Object source, String member, Object index, Object val) {
    
      if(index instanceof Number) {
        
        try {
          PropertyUtils.setIndexedProperty(source, member, ((Number) index).intValue(), val);
        }
        catch (IllegalAccessException | InvocationTargetException e) {
          throw new ExecutionException("invalid indexed member access", getLocation(selector));
        }
        catch (NoSuchMethodException e) {
          //Ignore and keep trying...
        }
        
      }
      
      //Try as separate member -> index accesses
      
      Object memberSource = visitor.selectMember(source, member, false, selector);
      if(memberSource == null) {
        logger.error("{}: attempt to assign to null", getLocation(selector));
        return;
      }
      
      setIndex(memberSource, index, val);
    }
    
    void setIndex(Object source, Object index, Object val) {
      
      if(index instanceof Number) {
        
        setIndex(source, ((Number) index).intValue(), val);
      }
      else {
        setMember(source, index.toString(), false, val);
      }
      
    }

    void setIndex(Object source, int index, Object val) {
      
      if(source.getClass().isArray()) {
        
        Array.set(source, index, val);
        
      }
      else if(source instanceof List<?>) {
        
        @SuppressWarnings("unchecked")
        List<Object> list = ((List<Object>)source);
        
        list.set(index, val);
        
      }
      else {
        throw new ExecutionException("Invalid index expression", getLocation(selector));
      }
      
    }
    
    void setMember(Object source, String property, boolean safe, Object val) {
      
      try {
        PropertyUtils.setSimpleProperty(source, property, val);
      }
      catch (IllegalAccessException e) {
        throw new ExecutionException(e, getLocation(selector));
      }
      catch (InvocationTargetException e) {
        throw new ExecutionException(e, getLocation(selector));
      }
      catch (NoSuchMethodException e) {
        if(!safe) {
          logger.warn("{}: property '{}' does not exist", getLocation(selector), property);
        }
      }
      
    }
    
  }
  
  class SafeSelectorLValue extends SelectorLValue {

		SafeSelectorLValue(Object source, RefSelectorContext selector) {
			super(source, selector);
		}

		@Override
		void nullAssignment() {
		}
  	
  }

  /**
   * Provides foreach iteration values
   * 
   * @author kdubb
   *
   */
  static public class ForeachIterator {

    Iterator<?> iter;
    Integer index;
    Object value;
    
    public ForeachIterator(Collection<?> source) {
      this.iter = source.iterator();
      this.index = -1;
    }
    
    boolean next() {
      if(!iter.hasNext()) {
        index = null;
        value = null;
        return false;
      }
      
      value = iter.next();
      index+=1;
      return true;
    }
    
    public boolean getHasNext() {
      return iter.hasNext();
    }
    
    public boolean getOdd() {
      Integer count = getCount();
      if(count == null)
        return false;
      return count % 2 == 1;
    }
    
    public boolean getEven() {
      Integer count = getCount();
      if(count == null)
        return false;
      return count % 2 == 0;
    }
    
    public Integer getCount() {
      if(index == null)
        return null;
      return index+1;
    }
    
    public Integer getIndex() {
      return index;
    }
    
    public Object getValue() {
      return value;
    }
      
  }
  
  
  private static final String OUTPUT_LOOP_CONTROL_VAR = "##loop-control";
  private static final String FUNC_RETURN_VAR = "##return";
  
  private enum ControlStatements {
    Return,
    Break,
    Continue
  }
  
  
  private class Visitor extends StencilParserBaseVisitor<Object> {
    
    @Override
    public Object visitChildren(RuleNode node) {
      Object res = null;
      int n = node.getChildCount();
      for (int i=0; i<n; i++) {
        res = node.getChild(i).accept(this);
      }
      return res;
    }

    @Override
    public Object visitTemplateImporter(TemplateImporterContext object) {
  
      TemplateImpl imported = load(value(object.stringLit));
      
      Environment prevEnv = switchEnvironment(new Environment(imported.getPath(), new NullWriter()));
      pushScope();

      Scope importedScope;
      try {
        
        imported.getContext().accept(this);
        
      }
      finally {
        importedScope = popScope();        
        switchEnvironment(prevEnv);
      }
    
      //Import defintions
      if(object.id == null) {
    	  currentScope.values.putAll(importedScope.values);
      }
      else {
    	  currentScope.declare(object.id.getText(), importedScope.values);
      }

      return null;
    }
    
    @Override
    public Object visitTypeImporter(TypeImporterContext object) {
      
      String typeFullName = value(object.qName);
    
      String localName = value(object.id);
      if(localName == null) {
        localName = Iterables.getLast(Splitter.on('.').split(typeFullName), "");
      }
    
      Class<?> type = loadClass(typeFullName);
      if(type == null) {
        throw new UndefinedTypeException(typeFullName);
      }
    
      currentScope.declare(localName, type);
      
      return null;
    }
    
    
    @Override
    public Object visitMacroDefinition(MacroDefinitionContext object) {  

      BoundMacro boundMacro = new BoundMacro(object, currentScope);

      currentScope.declare(value(object.id), boundMacro);

      return null;
    }
    
    @Override
    public Object visitFunctionDefinition(FunctionDefinitionContext object) {

      BoundFunction boundFunction = new BoundFunction(object, currentScope);

      currentScope.declare(value(object.id), boundFunction);

      return null;
    }

    @Override
    public Object visitExportDefinition(ExportDefinitionContext object) {
                  
      for(VariableDeclContext varDecl : object.vars) {
      
        currentScope.declare(value(varDecl.id), eval(varDecl.expr));
      }
      
      return null;
    }
  
    @Override
    public Object visitOutputBlock(OutputBlockContext object) {
      
      for (OutputContext content : object.outputs) {
        
        content.accept(this);
        
        Object outputLoopControl = currentScope.refControl(OUTPUT_LOOP_CONTROL_VAR);
        if(outputLoopControl == ControlStatements.Break || outputLoopControl == ControlStatements.Continue) {
          break;
        }
        
      }
      
      return null;
    }
    
    @Override
    public Object visitOutput(OutputContext ctx) {
      return ctx.getChild(0).accept(visitor);
    }

    @Override
    public Object visitDynamicOutput(DynamicOutputContext ctx) {
      return ctx.getChild(1).accept(visitor);
    }

    @Override
    public Object visitTextOutput(TextOutputContext object) {
  
      output(object.getText());
      
      return null;
    }
  
    @Override
    public Object visitIncludeOutput(IncludeOutputContext object) {
        
      TemplateImpl included = load(value(object.stringLit));
  
      Map<String, Object> params;
      Map<String, Object> blocks;
      
      HeaderContext hdr = included.getContext().hdr;
      if(hdr.hdrSig != null) {
        params = bindParams(hdr.hdrSig.callSig, object.call);
        blocks = bindBlocks(hdr.hdrSig.prepSig, object.prep);
      }
      else {
        params = Collections.emptyMap();
        blocks = Collections.emptyMap();
      }
  
      Environment prevEnv = switchEnvironment(new Environment(included.getPath(), currentEnvironment.out));
      
      pushScope();
  
      try {
  
        currentScope.declare(params);
        currentScope.declare(blocks);
  
        included.getContext().accept(visitor);
        
      }
      finally {
        popScope();
        switchEnvironment(prevEnv);
      }
  
      return null;
    }
  
    @Override
    public Object visitExpressionOutput(ExpressionOutputContext object) {
      
      if(logger.isTraceEnabled()) {
        logger.trace("Evaluating expression: {}", object.getText());
      }
  
      Object value = eval(object.expr);
  
      if (value == null) {
        logger.info("Expression evaluates to null: {}", value(object));
      }
      else if (value instanceof BoundMacro) {
        
        //
        //Output a bound macro definition
        //
  
        BoundBlock boundBlock = new BoundBlock((BoundMacro) value);
  
        Map<String, Object> boundBlocks = bindBlocks(boundBlock.source.source.prepSig, object.prep);

        boundBlock.declareBlocks(boundBlocks);
  
        boundBlock.exec();
  
      }
      else if (value instanceof BoundBlock) {
  
        //
        //Output a bound block
        //
        
        BoundBlock boundBlock = (BoundBlock) value;
  
        Map<String, Object> boundBlocks = bindBlocks(boundBlock.source.source.prepSig, object.prep);
  
        boundBlock.declareBlocks(boundBlocks);
  
        boundBlock.exec();
        
      }
      else if (value instanceof BoundParamOutputBlock) {
        
        //
        //Output a bound parameter block
        //
  
        BoundParamOutputBlock boundParamBlock = (BoundParamOutputBlock) value;
        UnnamedOutputBlockContext defaultBlock = object.prep != null ? object.prep.unnamedOutputBlock() : null;
  
        switch (boundParamBlock.mode) {
  
        case Before:
          boundParamBlock.exec();
          if (defaultBlock != null)
            defaultBlock.accept(this);
          break;
  
        case After:
          if (defaultBlock != null)
            defaultBlock.accept(this);
          boundParamBlock.exec();
          break;
  
        case Replace:
          boundParamBlock.exec();
          break;
  
        }
  
      }
      else if(value instanceof Preparable) {
                
        Preparable function = (Preparable) value;
        
        PrepareSignatureContext sig;
        try {
          sig = extensionPreparableSignatureCache.get(function);
        }
        catch (java.util.concurrent.ExecutionException e) {
          throw new RuntimeException(e);
        }
        
        Map<String, Object> params = bindBlocks(sig, object.prep);
        
        try {
          output(function.prepare(params));
        }
        catch (Throwable e) {
          throw new InvocationException("error invoking extension function", getLocation(object), e);
        }
        
      }
      else {
  
        //Ensure a preparation isn't incorrectly attempted
        
        if (object.prep != null &&
            (object.prep.namedBlocks.isEmpty() == false ||
            object.prep.unnamedBlock != null)) {
          throw new ExecutionException("Illegal block call", getLocation(object));
        }
  
        output(value);
      }
  
      return null;
    }
    
    @Override
    public Object visitDeclarationOutput(DeclarationOutputContext object) {
  
      for (VariableDeclContext decl : object.vars) {
  
        String name = name(decl);
        Object value = eval(decl.expr);
  
        currentScope.declare(name, value);
      }
  
      return null;
    }
  
    @Override
    public Object visitAssignOutput(AssignOutputContext object) {
      
      for(AssignmentContext ass : object.assignments) {
        
        LValue lvalue = eval(ass.lValRef);
        Object val = eval(ass.expr);
        assign(lvalue, value(ass.assignmntOper), val, ass);
        
      }
      
      return null;
    }
  
    @Override
    public Object visitIfOutput(IfOutputContext object) {
  
      boolean cond = evalBoolean(object.expr);
  
      OutputBlockContext out = cond ? object.thenBlock : object.elseBlock;
  
      if (out != null)
        out.accept(this);
  
      return null;
    }
  
    @Override
    public Object visitForeachOutput(ForeachOutputContext object) {
  
      Collection<?> collection = evalCollection(object.expr);
  
      pushScope();
  
      currentScope.declare(OUTPUT_LOOP_CONTROL_VAR, null);
      
      try {
        
        if (collection != null && collection.isEmpty() == false) {
  
          ForeachIterator iter = new ForeachIterator(collection);
  
          if(object.iterator != null) {
            currentScope.declare(name(object.iterator), iter);
          }
  
          while(iter.next() && currentScope.refControl(OUTPUT_LOOP_CONTROL_VAR) != ControlStatements.Break) {
  
            currentScope.assign(OUTPUT_LOOP_CONTROL_VAR, null);
            
            currentScope.declare(name(object.value), iter.getValue());
  
            object.iterBlock.accept(this);
          
          }
          
        }
        else {

          if (object.elseBlock != null)
            object.elseBlock.accept(this);
        }
  
      }
      finally {
        popScope();
      }

      return null;
    }
  
    @Override
    public Object visitWhileOutput(WhileOutputContext object) {
  
      ExpressionContext condExpr = object.expr;
  
      pushScope();
      
      currentScope.declare(OUTPUT_LOOP_CONTROL_VAR, null);
  
      try {
  
        while (evalBoolean(condExpr) && currentScope.refControl(OUTPUT_LOOP_CONTROL_VAR) != ControlStatements.Break) {
  
          currentScope.assign(OUTPUT_LOOP_CONTROL_VAR, null);

          object.block.accept(this);
  
        }
  
      }
      finally {
        popScope();
      }

      return null;
    }
    
    @Override
    public Object visitBreakOutput(BreakOutputContext object) {
      currentScope.assign(OUTPUT_LOOP_CONTROL_VAR, ControlStatements.Break);
      return null;
    }
  
    @Override
    public Object visitContinueOutput(ContinueOutputContext object) {
      currentScope.assign(OUTPUT_LOOP_CONTROL_VAR, ControlStatements.Continue);
      return null;
    }
  
    @Override
    public Object visitSwitchOutput(SwitchOutputContext object) {
  
      Object switchValue = eval(object.expr);
  
      boolean found = false;
      SwitchOutputDefaultCaseContext defaultCase=null;
  
      for (SwitchOutputCaseContext switchCase : object.cases) {
  
        if(switchCase instanceof SwitchOutputValueCaseContext) {
          
          SwitchOutputValueCaseContext valueCase = (SwitchOutputValueCaseContext) switchCase;
          
          Object caseValue = eval(valueCase.expr);
  
          if (switchValue.equals(caseValue)) {
  
            valueCase.block.accept(this);
  
            found = true;
          }
  
        }
        else if(switchCase instanceof SwitchOutputDefaultCaseContext){
          defaultCase = (SwitchOutputDefaultCaseContext) switchCase;
        }
        
      }
  
      if (!found && defaultCase != null) {
  
        defaultCase.block.accept(this);
  
      }
  
      return null;
    }
    
    @Override
    public Object visitWithOutput(WithOutputContext object) {
          
      //Declare all variables
      List<Object> vars = eval(object.expr);
      
      pushScope(new WithScope(vars));
      
      try {
        
        object.block.accept(this);
        
      }
      finally {
        popScope();
      }
      
      return null;
    }
  
    @Override
    public Object visitBlockStatement(BlockStatementContext object) {
  
      pushScope();
  
      try {
  
        for (StatementContext statement : object.stmts) {
  
          Object res = statement.accept(visitor);
          if (res != null)
            return res;
  
        }
  
        return null;
      }
      finally {
        popScope();
      }
  
    }
  
    @Override
    public Object visitExpressionStatement(ExpressionStatementContext object) {
      eval(object.expr);
      return null;
    }
  
    @Override
    public Object visitDeclarationStatement(DeclarationStatementContext object) {
  
      for (VariableDeclContext decl : object.vars) {
  
        String name = name(decl);
        Object value = eval(decl.expr);
  
        currentScope.declare(name, value);
      }
  
      return null;
    }
  
    @Override
    public Object visitAssignmentStatement(AssignmentStatementContext object) {
  
      LValue lvalue = eval(object.assignmnt.lValRef);
      Object val = eval(object.assignmnt.expr);
      assign(lvalue, value(object.assignmnt.assignmntOper), val, object);
  
      return null;
    }
  
    @Override
    public Object visitReturnStatement(ReturnStatementContext object) {
      Object val = eval(object.expr);
      currentScope.assign(FUNC_RETURN_VAR, val);
      return ControlStatements.Return;
    }
  
    @Override
    public Object visitBreakStatement(BreakStatementContext ctx) {
      return ControlStatements.Break;
    }

    @Override
    public Object visitContinueStatement(ContinueStatementContext ctx) {
      return ControlStatements.Continue;
    }

    @Override
    public Object visitIfStatement(IfStatementContext object) {
  
      boolean condition = evalBoolean(object.expr);
  
      if (condition) {
        return exec(object.thenStmt);
      }
      else {
        return exec(object.elseStmt);
      }
  
    }
  
    @Override
    public Object visitForeachStatement(ForeachStatementContext object) {
  
      Collection<?> collection = evalCollection(object.expr);
  
      pushScope();
  
      try {
        
        Object res = null;
        
        if (collection != null && collection.isEmpty() == false) {
          
          ForeachIterator iter = new ForeachIterator(collection);
    
          if(object.iterator != null) {
            
            currentScope.declare(name(object.iterator), iter);
            
          }
    
          while(iter.next()) {
    
            currentScope.declare(name(object.value), iter.getValue());
    
            Object sres = exec(object.iterStmt);
            if(sres == ControlStatements.Break) {
              break;
            }
            else if(sres == ControlStatements.Continue) {
              continue;
            }
            else if(sres == ControlStatements.Return) {
              res = sres;
              break;
            }
          
          }
          
        }
        else {

          res = exec(object.elseStmt);
          
        }
  
        return res;
        
      }
      finally {
        popScope();
      }
  
    }
  
    @Override
    public Object visitWhileStatement(WhileStatementContext object) {
  
      ExpressionContext condExpr = object.expr;
  
      while (evalBoolean(condExpr)) {
  
        Object res = exec(object.stmt);
        if(res == ControlStatements.Break) {
          break;
        }
        else if(res == ControlStatements.Continue) {
          continue;
        }
        else if (res == ControlStatements.Return) {
          return res;
        }
  
      }
  
      return null;
    }
  
    @Override
    public Object visitSwitchStatement(SwitchStatementContext object) {
  
      Object switchValue = eval(object.expr);
  
      boolean found = false;
      SwitchStatementDefaultCaseContext defaultCase = null;
  
      for(SwitchStatementCaseContext switchCase : object.cases) {
  
        if(switchCase instanceof SwitchStatementValueCaseContext) {
          
          SwitchStatementValueCaseContext valueCase = (SwitchStatementValueCaseContext) switchCase;

          Object caseValue = eval(valueCase.expr);
  
          if(switchValue.equals(caseValue)) {
  
            exec(valueCase.stmt);
            
            found = true;
          }
  
        }
        else if(switchCase instanceof SwitchStatementDefaultCaseContext) {
          
          defaultCase = (SwitchStatementDefaultCaseContext) switchCase;
          
        }
        
      }
  
      if (!found && defaultCase != null) {
  
        exec(defaultCase.stmt);
  
      }
  
      return null;
    }
  
    @Override
    public Object visitWithStatement(WithStatementContext object) {
  
      //Declare all variables
      List<Object> vars = eval(object.expr);
      
      pushScope(new WithScope(vars));
      
      try {
        
        exec(object.stmt);
        
      }
      finally {
        popScope();
      }
      
      return null;
    }
    
    @Override
    public LValue visitLValueRef(LValueRefContext object) {
  
      LValue val = new ScopeLValue(currentScope, name(object));
      
      for(RefSelectorContext sel : object.refSel) {
        val = val.select(sel);
      }
  
      return val;
    }
  
    @Override
    public Object visitVariableRef(VariableRefContext object) {
      return currentScope.ref(name(object));
    }
    
    @Override
    public Object visitSimpleName(SimpleNameContext ctx) {
      return ctx.getChild(0);
    }

    @Override
    public Object visitLiteral(LiteralContext ctx) {
      return ctx.getChild(0).accept(visitor);
    }
    
    @Override
    public Object visitNullLiteral(NullLiteralContext object) {
      return null;
    }
  
    @Override
    public Object visitBooleanLiteral(BooleanLiteralContext object) {
      return value(object);
    }
  
    @Override
    public Object visitNumberLiteral(NumberLiteralContext ctx) {
      return ctx.getChild(0).accept(visitor);
    }

    @Override
    public Object visitIntegerLiteral(IntegerLiteralContext object) {
      return value(object);
    }
  
    @Override
    public Object visitFloatingLiteral(FloatingLiteralContext object) {
      return value(object);
    }
  
    @Override
    public Object visitStringLiteral(StringLiteralContext object) {
      return value(object);
    }
  
    @Override
    public Object visitListLiteral(ListLiteralContext object) {
  
      List<Object> list = new ArrayList<Object>();
  
      for (ExpressionContext itemExpr : object.expr) {
  
        list.add(eval(itemExpr));
  
      }
  
      return list;
    }
  
    @Override
    public Object visitMapLiteral(MapLiteralContext object) {
  
      Map<String, Object> map = new HashMap<String, Object>();
  
      for (NamedValueContext namedValue : object.namedValues) {
  
        Object value = eval(namedValue.expr);
  
        map.put(name(namedValue), value);
  
      }
  
      return map;
    }
  
    @Override
    public Object visitRangeLiteral(RangeLiteralContext object) {
      int low = evalInteger(object.from);
      int hi = evalInteger(object.to);
      Set<?> range = ContiguousSet.create(Range.closedOpen(low, hi), DiscreteDomain.integers());
      return range;
    }
  
    @Override
    public Object visitTernaryExpression(TernaryExpressionContext object) {
  
      Object test = eval(object.test);
      
      if(operands.toBoolean(test)) {
        if(object.trueExpr == null)
          return test;
        else
          return eval(object.trueExpr);
      }
      else {
        return eval(object.falseExpr);
      }
    }
  
    @Override
    public Object visitInstanceExpression(InstanceExpressionContext ctx) {
      String typeFullName = value(ctx.qName);
      Class<?> type = loadClass(typeFullName);
      if(type == null) {
        Object val = currentScope.ref(typeFullName);        
        if(val instanceof Class<?>) {
          type = (Class<?>) val;
        }
        else {
          throw new UndefinedTypeException(typeFullName);
        }
      }
      Object value = eval(ctx.expr);
      return type.isInstance(value);
    }

    @Override
    public Object visitParenExpression(ParenExpressionContext ctx) {
      return eval(ctx.expr);
    }

    @Override
    public Object visitBinaryExpression(BinaryExpressionContext object) {
  
      ExpressionContext left = object.leftExpr, right = object.rightExpr;
      String oper = value(object.operator);
  
      if (oper.equals("==")) {
        return operands.equals(eval(left), eval(right));
      }
      else if (oper.equals("===")) {
        return eval(left) == eval(right);
      }
      else if (oper.equals("!==")) {
        return !(eval(left) == eval(right));
      }
      else if (oper.equals("!=")) {
        return !operands.equals(eval(left), eval(right));
      }
      else if (oper.equals(">")) {
        return operands.greaterThan(eval(left), eval(right));
      }
      else if (oper.equals(">=")) {
        return operands.greaterThanOrEqual(eval(left), eval(right));
      }
      else if (oper.equals("<")) {
        return operands.lessThan(eval(left), eval(right));
      }
      else if (oper.equals("<=")) {
        return operands.lessThanOrEqual(eval(left), eval(right));
      }
      else if (oper.equals("+")) {
        return operands.add(eval(left), eval(right));
      }
      else if (oper.equals("-")) {
        return operands.subtract(eval(left), eval(right));
      }
      else if (oper.equals("*")) {
        return operands.multiply(eval(left), eval(right));
      }
      else if (oper.equals("/")) {
        return operands.divide(eval(left), eval(right));
      }
      else if (oper.equals("%")) {
        return operands.mod(eval(left), eval(right));
      }
      else if (oper.equals(">>")) {
        return operands.rightShift(eval(left), eval(right));
      }
      else if (oper.equals("<<")) {
        return operands.leftShift(eval(left), eval(right));
      }
      else if (oper.equals("&&")) {
        return evalBoolean(left) && evalBoolean(right);
      }
      else if (oper.equals("||")) {
        return evalBoolean(left) || evalBoolean(right);
      }
      else if (oper.equals("&")) {
        return operands.bitwiseAnd(eval(left), eval(right));
      }
      else if (oper.equals("|")) {
        return operands.bitwiseOr(eval(left), eval(right));
      }
      else if (oper.equals("^")) {
        return operands.bitwiseXor(eval(left), eval(right));
      }
  
      throw new ExecutionException("invalid expression operator", getLocation(object));
    }
  
    @Override
    public Object visitUnaryExpression(UnaryExpressionContext object) {
  
      Object val = eval(object.expr);
  
      String oper = value(object.operator);
  
      if (oper.equals("++")) {
        return operands.add(val, 1);
      }
      else if (oper.equals("--")) {
        return operands.subtract(val, 1);
      }
      else if (oper.equals("~")) {
        return operands.bitwiseComplement(val);
      }
      else if (oper.equals("!")) {
        return !operands.toBoolean(val);
      }
      else if (oper.equals("+")) {
        return val;
      }
      else if (oper.equals("-")) {
        return operands.negate(val);
      }
  
      throw new ExecutionException("invalid prefix expression",getLocation(object));
    }
  
    @Override
    public Object visitVariableRefExpression(VariableRefExpressionContext ctx) {
      return eval(ctx.varRef);
    }

    @Override
    public Object visitLiteralExpression(LiteralExpressionContext ctx) {
      return ctx.lit.accept(visitor);
    }

    @Override
    public Object visitSelectorExpression(SelectorExpressionContext object) {
  
      Object res = eval(object.expr);
      
      for(SelectorContext sel : object.sels) {
        res = select(res, sel);
        if(res == null)
          break;
      }
      
      return res;
    }
  
    Object select(Object source, SelectorContext sel) {
      
      if(sel.valSel != null) {
        return select(source, sel.valSel);
      }
      else if(sel.refSel != null) {
        return select(source, sel.refSel);
      }
 
      return source;
    }
    
    Object select(Object source, ValueSelectorContext sel) {

      if(sel instanceof CallSelectorContext) {
        return select(source, (CallSelectorContext)sel);
      }
      
      if(sel instanceof MethodCallSelectorContext) {
        return select(source, (MethodCallSelectorContext)sel);
      }
      
      throw new ExecutionException("unknown selector", getLocation(sel));
    }
    
    Object select(Object source, RefSelectorContext sel) {        

      if(sel instanceof MemberIndexSelectorContext) {
        return select(source, (MemberIndexSelectorContext)sel);
      }
      
      if(sel instanceof IndexSelectorContext) {
        return select(source, (IndexSelectorContext)sel);
      }
      
      if(sel instanceof MemberSelectorContext) {
        return select(source, (MemberSelectorContext)sel);
      }
        
      if(sel instanceof SafeMemberSelectorContext) {
        return select(source, (SafeMemberSelectorContext)sel);
      }
        
      throw new ExecutionException("unknown selector", getLocation(sel));
    }
    
    Object select(Object source, CallSelectorContext sel) {
      
      if(logger.isTraceEnabled()) {
        logger.trace("Evaluating call selector: {}", sel.getText());
      }

      return selectCall(source, sel.call, sel);
    }

    Object select(Object source, MethodCallSelectorContext sel) {
  
      if(logger.isTraceEnabled()) {
        logger.trace("Evaluating method call selector: {}", sel.getText());
      }
      
      if (source == null) {
        logger.error("{}: attempt to call method on null", getLocation(sel));
        return null;
      }
      else {
  
        String name = name(sel);
        
        List<Object> params = sel.call.posParams != null ? eval(sel.call.posParams.exprs) : Collections.emptyList();
  
        try {
        
          if(source instanceof Class<?>) {
            
            return MethodUtils.invokeStaticMethod((Class<?>) source, name, params.toArray());
          
          }
          else {
            
            return MethodUtils.invokeMethod(source, name, params.toArray());
          
          }
        
        }
        catch (IllegalAccessException | InvocationTargetException e) {
          
          throw new ExecutionException("error executing call", getLocation(sel), e);
        
        }
        catch (NoSuchMethodException e) {
        
          Method extensionMethod = getExtensionMethod(source.getClass(), name);
          if(extensionMethod != null) {
            
            params.add(0, source);
            
            try {
              
              return extensionMethod.invoke(null, params.toArray());
              
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
              throw new ExecutionException("error executing call", getLocation(sel));
            }
            
          }
          else {
            
            source = selectMember(source, name, true, sel);
            if(source != null) {
            	return selectCall(source, sel.call, sel);
            }
            
            return null;
          }
          
        }
  
      }
  
    }
    
    Object select(Object source, MemberIndexSelectorContext sel) {
      
      if(logger.isTraceEnabled()) {
        logger.trace("Evaluating member index selector: {}", sel.getText());
      }
      
      String member = name(sel);
      Object index = eval(sel.expr);      
  
      return selectMemberIndex(source, member, index, sel);
    }
    
    Object select(Object source, IndexSelectorContext sel) {
      
      if(logger.isTraceEnabled()) {
        logger.trace("Evaluating index selector: {}", sel.getText());
      }
      
      Object selectorValue = eval(sel.expr);

      return selectIndex(source, selectorValue, sel);
    }
    
    Object select(Object source, MemberSelectorContext sel) {
      
      if(logger.isTraceEnabled()) {
        logger.trace("Evaluating member selector: {}", sel.getText());
      }
      
      if(source == null) {
        logger.warn("{}: unsafe null navigation", getLocation(sel));
        return null;
      }
      return selectMember(source, name(sel), false, sel);
    }
    
    Object select(Object source, SafeMemberSelectorContext sel) {
      
      if(logger.isTraceEnabled()) {
        logger.trace("Evaluating safe member selector: {}", sel.getText());
      }
      
      if(source == null) {
        return null;
      }
      
      return selectMember(source, name(sel), true, sel);
    }
    
    Object selectCall(Object source, CallableInvocationContext inv, ParserRuleContext loc) {
      
      if (source == null) {
        logger.error("Attempt to call null " + getLocation(loc));
        return null;
      }
      else if (source instanceof BoundMacro) {
  
        BoundMacro boundMacro = (BoundMacro) source;
        BoundBlock boundBlock = new BoundBlock(boundMacro);
  
        Map<String, Object> params = bindParams(boundMacro.source.callSig, inv);
  
        boundBlock.declareParams(params);
  
        return boundBlock;
  
      }
      else if (source instanceof BoundFunction) {
  
        BoundFunction boundFunction = (BoundFunction) source;
  
        Map<String, Object> params = bindParams(boundFunction.source.callSig, inv);
  
        return boundFunction.call(params);
      }
      else if(source instanceof Callable) {
        
        Callable function = (Callable) source;
        
        CallableSignatureContext sig;
        try {
          sig = extensionCallableSignatureCache.get(function);
        }
        catch (java.util.concurrent.ExecutionException e) {
          throw new RuntimeException(e);
        }
        
        Map<String,Object> params = bindParams(sig, inv);
        
        try {
          return function.call(params);
        }
        catch (Throwable e) {
          throw new InvocationException("error invoking extension function", getLocation(loc), e);
        }
      }
      else if(source instanceof Class<?>) {
        
        Class<?> type = (Class<?>) source;
        
        if(inv.namedParams != null) {
          throw new InvocationException("error invoking constructor: named parameters not allowed for constructors", getLocation(inv));
        }
  
        List<Object> paramValues;
        if(inv.posParams != null) {
          paramValues = eval(inv.posParams.exprs);
        }
        else {
          paramValues = Collections.emptyList();
        }
        
        try {
          return ConstructorUtils.invokeConstructor(type, paramValues.toArray());
        }
        catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
          throw new ExecutionException("error invoking constructor: ", getLocation(inv));
        }
        catch (NoSuchMethodException e) {
          return null;
        }
        
      }
  
      throw new ExecutionException("invalid call expression", getLocation(inv));
    }

    Object selectMemberIndex(Object source, String member, Object index, ParserRuleContext loc) {
      
      if(source == null) {
        logger.error("{}: attempt to index null", getLocation(loc));
        return null;
      }

      if(index instanceof Number) {
        
        try {
          return PropertyUtils.getIndexedProperty(source, member, ((Number) index).intValue());
        }
        catch (IllegalAccessException | InvocationTargetException e) {
          throw new ExecutionException("invalid indexed member access", getLocation(loc));
        }
        catch (NoSuchMethodException e) {
          //Ignore and keep trying...
        }
        
      }
      
      //Try as separate member -> index accesses
      
      Object memberSource = selectMember(source, member, false, loc);
      if(memberSource == null) {
        logger.error("{}: unsafe null navigation", getLocation(loc));
        return null;
      }
      
      return selectIndex(memberSource, index, loc);
    }
    
    Object selectIndex(Object source, Object index, ParserRuleContext loc) {
      
      if(source == null) {
        logger.error("{}: attempt to index null", getLocation(loc));
        return null;
      }

      if(index instanceof Number) {
        
        return selectIndex(source, ((Number) index).intValue(), loc);
      }
      else {
        return selectMember(source, index.toString(), false, loc);
      }
      
    }

    Object selectIndex(Object source, int index, ParserRuleContext loc) {
      
      if(source == null) {
        logger.error("{}: attempt to index null", getLocation(loc));
        return null;
      }

      if(source.getClass().isArray()) {
        
        return Array.get(source, index);
        
      }
      else if(source instanceof List<?>) {
        
        List<?> list = ((List<?>)source);
        
        return list.get(index);
        
      }
      else if(source instanceof Map<?,?>) {
        
        Map<?, ?> map = ((Map<?, ?>)source);

        return map.get(index);
      }
      else {
        throw new ExecutionException("invalid index expression", getLocation(loc));
      }
      
    }
    
    Object selectMember(Object source, String property, boolean safe, ParserRuleContext loc) {
      
      if(source == null) {
        logger.error("{}: attempt to access null", getLocation(loc));
        return null;
      }

      try {
        return PropertyUtils.getProperty(source, property);
      }
      catch (IllegalAccessException | InvocationTargetException e) {
        throw new ExecutionException(e, getLocation(loc));
      }
      catch (NoSuchMethodException e) {
        if(!safe) {
          logger.error("{}: property '{}' does not exist", getLocation(loc), property);
        }
      }
      
      return null;
    }

    void assign(LValue lvalue, String oper, Object right, ParserRuleContext loc) {
      
      if (oper.equals("=")) {
        lvalue.set(right);
      }
      else if (oper.equals("+=")) {
        lvalue.set(operands.add(lvalue.get(), right));
      }
      else if (oper.equals("-=")) {
        lvalue.set(operands.subtract(lvalue.get(), right));
      }
      else if (oper.equals("*=")) {
        lvalue.set(operands.multiply(lvalue.get(), right));
      }
      else if (oper.equals("/=")) {
        lvalue.set(operands.divide(lvalue.get(), right));
      }
      else if (oper.equals("%=")) {
        lvalue.set(operands.mod(lvalue.get(), right));
      }
      else if (oper.equals("&=")) {
        lvalue.set(operands.bitwiseAnd(lvalue.get(), right));
      }
      else if (oper.equals("|=")) {
        lvalue.set(operands.bitwiseOr(lvalue.get(), right));
      }
      else if (oper.equals("^=")) {
        lvalue.set(operands.bitwiseXor(lvalue.get(), right));
      }
      else if (oper.equals("<<=")) {
        lvalue.set(operands.leftShift(lvalue.get(), right));
      }
      else if (oper.equals(">>=")) {
        lvalue.set(operands.rightShift(lvalue.get(), right));
      }
      else {
        throw new ExecutionException("invalid assignment operator", getLocation(loc));
      }
      
    }

  }
    
  private StencilEngine engine;
  private Visitor visitor = new Visitor();
  private Environment currentEnvironment;
  private Scope currentScope;
  private StencilOperands operands = new StencilOperands();
  private LoadingCache<Callable,CallableSignatureContext> extensionCallableSignatureCache = CacheBuilder.newBuilder().build(new CacheLoader<Callable,CallableSignatureContext>() {

    @Override
    public CallableSignatureContext load(Callable key) throws Exception {

      String[] paramNames = key.getParameterNames();
      
      CallableSignatureContext sig = new CallableSignatureContext(null, -1);

      sig.paramDecls = new ArrayList<>();
      
      for(String paramName : paramNames) {
        
        if (paramName.equals(ALL_PARAM_NAME)) {
          sig.paramDecls.add(Contexts.createAllParameterDecl(sig, ALL_PARAM_NAME));
        }
        else {
          sig.paramDecls.add(Contexts.createParameterDecl(sig, paramName));
        }
        
      }
      
      return sig;
    }
    
  });
  private LoadingCache<Preparable,PrepareSignatureContext> extensionPreparableSignatureCache = CacheBuilder.newBuilder().build(new CacheLoader<Preparable,PrepareSignatureContext>() {

    @Override
    public PrepareSignatureContext load(Preparable key) throws Exception {

      String[] blockNames = key.getBlockNames();
      
      PrepareSignatureContext sig = new PrepareSignatureContext(null, -1);
      
      sig.blockDecls = new ArrayList<>();
      
      for(String blockName : blockNames) {
        
        if (blockName.equals(ALL_BLOCK_NAME)) {
          sig.blockDecls.add(Contexts.createAllBlockDecl(sig, blockName));
        }
        else if (blockName.equals(UNNAMED_BLOCK_NAME)) {
          sig.blockDecls.add(Contexts.createUnnamedBlockDecl(sig, blockName));
        }
        else {
          sig.blockDecls.add(Contexts.createBlockDecl(sig, blockName));
        }
      }
      
      return sig;
    }
    
  });
  
  
  
  public StencilInterpreter(StencilEngine engine, Iterable<GlobalScope> globalScopes) {
    this.engine = engine;    
    this.currentScope = new RootScope(globalScopes);
  }

  public StencilInterpreter declare(Map<String,Object> parameters) {
    currentScope.declare(parameters);
    return this;
  }
  
  /**
   * Processes a template into its text representation
   */
  public void process(TemplateImpl template, Writer out) throws IOException {
    
  	Environment env = switchEnvironment(new Environment(template.getPath(), out));
    
		try {
			template.getContext().accept(visitor);
		}
		finally {
		  switchEnvironment(env);
		}
  }
  
  /**
   * Loads a template from the supplied template loader.
   * 
   */
  private TemplateImpl load(String path) {

  	path = Paths.resolvePath(currentEnvironment.path, path);
  	
    try {
      return (TemplateImpl) engine.load(path);
    }
    catch (IOException | ParseException e) {
      throw new ExecutionException(e);
    }
  }

  /**
   * 
   */
  private Class<?> loadClass(String className) {
    try {
      return StencilEngine.class.getClassLoader().loadClass(className);
    }
    catch (ClassNotFoundException e) {
      return null;
    }
  }
  
  /**
   * Switch to a new environment
   * 
   * @param env Environment to switch to
   * @returns Previous environment 
   */
  private Environment switchEnvironment(Environment env) {
    Environment prev = currentEnvironment;
    currentEnvironment = env;
    return prev;
  }
  
  /**
   * Switch to a completely new scope stack
   * 
   * @param scope Top of scope stack to switch to
   * @return Top of previous scope stack
   */
  private Scope switchScope(Scope scope) {
    Scope prev = currentScope;
    currentScope = scope;
    return prev;
  }
  
  /**
   * Pushes a new scope onto the scope stack
   * 
   */
  private void pushScope() {
    pushScope(new Scope(currentScope));
  }

  /**
   * Pushes a scope onto the scope stack
   * 
   * @param scope Scope to be pushed onto the stack
   */
  private void pushScope(Scope scope) {
    scope.parent = currentScope;
    currentScope = scope;
  }

  /**
   * Pops scope from the scope stack returning the current previously current
   * scope
   * 
   * @return Scope prior to popping
   */
  private Scope popScope() {
    Scope prevScope = currentScope;
    currentScope = currentScope.parent;
    return prevScope;
  }

  /**
   * Bind parameter values to names based on how the call was invoked (named or
   * positional) and parameters names from the call signature.
   * 
   * @param sig Signature of call
   * @param inv Invocation of call
   * @return Map of String => Object representing call parameters
   */
  private Map<String, Object> bindParams(CallableSignatureContext sig, CallableInvocationContext inv) throws ExecutionException {
    
    if(inv == null) {
      return Collections.emptyMap();
    }

    Map<String, Object> vals = new HashMap<>();
    
    if (inv.posParams != null) {

      //
      // Ensure legality of call
      //

      // Can only use positional or named; not both
      if (inv.namedParams != null) {
        throw new InvocationException("only positional or named parameters are allowed", getLocation(inv));
      }

      //
      // Match up parameters
      //

      ListIterator<ParameterDeclContext> paramDeclIter = sig.paramDecls.listIterator();
      ListIterator<ExpressionContext> paramExprIter = inv.posParams.exprs.listIterator();
      
      ParameterDeclContext allDecl = null;

      while (paramDeclIter.hasNext()) {

        ParameterDeclContext paramDecl = paramDeclIter.next();

        if (paramDecl.flag != null && paramDecl.flag.getText().equals("*")) {
          if (allDecl != null) {
            throw new ExecutionException("only a single parameter can be marked with '*'");
          }
          allDecl = paramDecl;
          continue;
        }
        
        String paramName = name(paramDecl);
        
        // Find expression (or use default)
        ExpressionContext paramExpr = paramExprIter.hasNext() ? paramExprIter.next() : paramDecl.expr;

        // Evaluate expression -> value
        Object paramVal = eval(paramExpr);

        vals.put(paramName, paramVal);
        
      }
      
      //
      // Add rest of parameters (if requested)
      //
      
      if (allDecl != null) {
        
        Map<String, Object> otherParams = new HashMap<>();
        
        while (paramExprIter.hasNext()) {
          
          int paramIdx = paramExprIter.nextIndex();
          
          ExpressionContext paramExpr = paramExprIter.next();
          
          Object paramVal = eval(paramExpr);
          
          otherParams.put(Integer.toString(paramIdx), paramVal);
        }
        
        vals.put(allDecl.id.getText(), otherParams);
      }
      
    }
    else if (inv.namedParams != null) {

      //
      // Ensure legality of call
      //

      // Can only use positional or named; not both
      if (inv.posParams != null) {
        throw new InvocationException("only positional or named parameters are allowed", getLocation(inv));
      }

      //
      // Load parameters
      //

      List<NamedValueContext> namedParameters = new ArrayList<>(inv.namedParams.namedValues);
      Iterator<ParameterDeclContext> paramDeclIter = sig.paramDecls.iterator();
      
      ParameterDeclContext allDecl = null;

      while (paramDeclIter.hasNext()) {

        ParameterDeclContext paramDecl = paramDeclIter.next();
        
        if (paramDecl.flag != null && paramDecl.flag.getText().equals("*")) {
          allDecl = paramDecl;
          continue;
        }
        
        String paramName = name(paramDecl);

        // Find expression
        ExpressionContext paramExpr = findAndRemoveValue(namedParameters, paramName);

        // Assign default (is needed)
        paramExpr = paramExpr != null ? paramExpr : paramDecl.expr;

        // Evaluate expression -> value
        Object paramVal = eval(paramExpr);

        vals.put(paramName, paramVal);
        
      }
      
      //
      // Add rest of parameters (if requested)
      //
      
      if (allDecl != null) {
        
        Map<String, Object> otherParams = new HashMap<>();
        
        for (NamedValueContext namedValue : namedParameters) {
          
          String paramName = namedValue.name.getText();
          
          Object paramVal = eval(namedValue.expr);
          
          otherParams.put(paramName, paramVal);
          
        }

        vals.put(allDecl.id.getText(), otherParams);
      }
      

    }

    return vals;
  }

  /**
   * Bind block values to names based on prepare signature
   * 
   * @param sig Signature of output block preparation
   * @param inv Invocation of output block
   * @return Map of Name => BoundParamBlocks representing Macro blocks
   */
  private Map<String, Object> bindBlocks(PrepareSignatureContext sig, PrepareInvocationContext inv) {

    if(inv == null) {
      return Collections.emptyMap();
    }
    
    BlockDeclContext allDecl = null;
    BlockDeclContext unnamedDecl = null;
    
    List<NamedOutputBlockContext> namedBlocks = new ArrayList<>(inv.namedBlocks);
    Map<String, Object> blocks = new HashMap<>();
    
    for (BlockDeclContext blockDecl : sig.blockDecls) {
      
      if (blockDecl.flag != null) {
        if (blockDecl.flag.getText().equals("*")) {
          if (allDecl != null) {
            throw new ExecutionException("only a single parameter can be marked with '*'");
          }
          allDecl = blockDecl;
        }
        else if (blockDecl.flag.getText().equals("+")) {
          if (unnamedDecl != null) {
            throw new ExecutionException("only a single parameter can be marked with '+'");
          }
          unnamedDecl = blockDecl;
        }
        else {
          throw new ExecutionException("unknown block declaration flag");
        }
        continue;
      }
      
      //Find the block
      
      ParserRuleContext paramBlock = findAndRemoveBlock(namedBlocks, name(blockDecl));
      
      //Bind the block
      
      BoundParamOutputBlock boundBlock = bindBlock(paramBlock);

      blocks.put(name(blockDecl), boundBlock);
      
    }
    
    //
    // Bind unnamed block (if requested)
    //
    
    if (unnamedDecl != null) {
      
      UnnamedOutputBlockContext unnamedBlock = inv.unnamedBlock;
      
      BoundParamOutputBlock boundUnnamedBlock = bindBlock(unnamedBlock);
      
      blocks.put(unnamedDecl.id.getText(), boundUnnamedBlock);
      
    }
    
    //
    // Bind rest of blocks (if requested)
    //
    
    if (allDecl != null) {
      
      Map<String, Block> otherBlocks = new HashMap<>();
      
      // Add unnamed block if it wasn't bound explicitly
      if (inv.unnamedBlock != null && unnamedDecl == null) {
        
        UnnamedOutputBlockContext unnamedBlock = inv.unnamedBlock;
        
        BoundParamOutputBlock boundUnnamedBlock = new BoundParamOutputBlock(unnamedBlock, mode(unnamedBlock), currentScope);
        
        otherBlocks.put("", boundUnnamedBlock);
      }
      
      // Add all other unbound blocks
      for (NamedOutputBlockContext namedBlock : namedBlocks) {
        
        String blockName = nullToEmpty(name(namedBlock));
        
        BoundParamOutputBlock boundNamedBlock = new BoundParamOutputBlock(namedBlock, mode(namedBlock), currentScope);
        
        otherBlocks.put(blockName, boundNamedBlock);
        
      }

      blocks.put(allDecl.id.getText(), otherBlocks);
      
    }

    return blocks;
  }

  private BoundParamOutputBlock bindBlock(ParserRuleContext paramBlock) {
    
    BoundParamOutputBlock boundBlock;
    
    if (paramBlock != null) {
      
      //Simply bind with found block
      boundBlock = new BoundParamOutputBlock(paramBlock, mode(paramBlock), currentScope);
    }
    else {
      
      //Couldn't find a block with the declared name so we bind a 
      //"null" block with mode "AFTER" to ensure default is used
      boundBlock = new BoundParamOutputBlock(ParamOutputBlockMode.After, currentScope);
    }
    
    return boundBlock;
  }

  /**
   * Find and remove block with specific name
   * 
   * @param blocks List of ParamOutputBlocks to search
   * @param name Name of block to find
   * @return ParamOutputBlock with specified name
   */
  private NamedOutputBlockContext findAndRemoveBlock(List<NamedOutputBlockContext> blocks, String name) {

    if(blocks == null) {
      return null;
    }

    Iterator<NamedOutputBlockContext> blockIter = blocks.iterator();
    while (blockIter.hasNext()) {
      NamedOutputBlockContext block = blockIter.next();
      String blockName = name(block);
      if(name.equals(blockName)) {
        blockIter.remove();
        return block;
      }
    }

    return null;
  }
  
  /**
   * Find and remove named value with specific name
   * 
   * @param namedValues List of NamedValues to search
   * @param name Name of value to find
   * @return Expression for value with specified name
   */
  private ExpressionContext findAndRemoveValue(List<NamedValueContext> namedValues, String name) {
    
    Iterator<NamedValueContext> namedValueIter = namedValues.iterator();
    while (namedValueIter.hasNext()) {
      NamedValueContext namedValue = namedValueIter.next();
      if (name.equals(value(namedValue.name))) {
        namedValueIter.remove();
        return namedValue.expr;
      }
    }

    return null;
  }
  
  /**
   * Evaluate an expression into a value
   * 
   * @param expr Expression to evaluate
   * @return Result of expression evaluate
   */
  private Object eval(ExpressionContext expr) {
    Object res = expr;
    while(res instanceof ExpressionContext)
      res = ((ExpressionContext)res).accept(visitor);
    if(res instanceof LValue)
      res = ((LValue) res).get();
    return res;
  }
  
  private Object eval(VariableRefContext ref) {
    return currentScope.ref(name(ref));
  }
  
  private LValue eval(LValueRefContext ref) {
    return (LValue) ref.accept(visitor);
  }
  
  /**
   * Evaluates a list of expressions into their values
   * 
   * @param expressions List of expressions to evaluate
   * @return List of related expression values
   */
  private List<Object> eval(Iterable<ExpressionContext> expressions) {

    List<Object> results = new ArrayList<Object>();

    for (ExpressionContext expression : expressions) {
      results.add(eval(expression));
    }

    return results;
  }

  /**
   * Execute a statement block until the first
   * return statement is reached.
   * 
   * @param block StatementBlock to evaluate
   * @return Result of expression evaluate
   */
  private Object exec(StatementContext statement) {
    if (statement == null)
      return null;
    return statement.accept(visitor);
  }

  private Object exec(BlockStatementContext statement) {
    if (statement == null)
      return null;
    return statement.accept(visitor);
  }

  /**
   * Evaluate an expression into a boolean value.
   * 
   * Conversion Rules: Boolean => Boolean String => !val.isEmpty() Collection =>
   * !val.isEmpty() <Else> => val != null
   * 
   * @param expr
   *          Expression to evaluate
   * @return Result of expression as boolean using conversion rules
   */
  private boolean evalBoolean(ExpressionContext expr) {
    return operands.toBoolean(eval(expr));
  }

  /**
   * Evaluate expression into an Integer value
   * 
   * @param expr
   *          Expression to evaluate
   * @return Result of expression as integer
   * @throws ExecutionException
   *           when expression doesn't evaluate to an integer
   */
  private Integer evalInteger(ExpressionContext expr) {
    return operands.toInteger(eval(expr));
  }
  
  /**
   * Evaluate expression into a Collection value
   * 
   * @param expr
   *          Expression to evaluate
   * @return Result of expression as collection
   * @throws ExecutionException
   *           when expression doesn't evaluate to a collection
   */
  private Collection<?> evalCollection(ExpressionContext expr) {
    return operands.toCollection(eval(expr));
  }

  /**
   * Retrieves location information for a model object
   *  
   * @param object Model object to locate
   * @return Location for the provided model object
   */
  private ExecutionLocation getLocation(ParserRuleContext object) {
    Token start = object.getStart();
    return new ExecutionLocation(currentEnvironment.path, start.getLine(), start.getCharPositionInLine());
  }
  
  void output(Object val) {
    if(val == null)
      return;
    try {
      currentEnvironment.out.append(val.toString());
    }
    catch (IOException e) {
      throw new ExecutionException(e);
    }
  }
  
}

class NullWriter extends Writer {

  public void close() {
    // blank
  }

  public void flush() {
    // blank
  }

  public void write(char[] cbuf, int off, int len) {
    // blank
  }
}
