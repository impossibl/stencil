package com.impossibl.stencil.engine.internal;

import java.util.List;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.impossibl.stencil.engine.parsing.ParamOutputBlockMode;
import com.impossibl.stencil.engine.parsing.StencilParser;
import com.impossibl.stencil.engine.parsing.StencilParser.BlockDeclContext;
import com.impossibl.stencil.engine.parsing.StencilParser.BooleanLiteralContext;
import com.impossibl.stencil.engine.parsing.StencilParser.CallableSignatureContext;
import com.impossibl.stencil.engine.parsing.StencilParser.FloatingLiteralContext;
import com.impossibl.stencil.engine.parsing.StencilParser.FunctionDefinitionContext;
import com.impossibl.stencil.engine.parsing.StencilParser.IntegerLiteralContext;
import com.impossibl.stencil.engine.parsing.StencilParser.LValueRefContext;
import com.impossibl.stencil.engine.parsing.StencilParser.MacroDefinitionContext;
import com.impossibl.stencil.engine.parsing.StencilParser.MemberIndexSelectorContext;
import com.impossibl.stencil.engine.parsing.StencilParser.MemberSelectorContext;
import com.impossibl.stencil.engine.parsing.StencilParser.MethodCallSelectorContext;
import com.impossibl.stencil.engine.parsing.StencilParser.NamedOutputBlockContext;
import com.impossibl.stencil.engine.parsing.StencilParser.NamedValueContext;
import com.impossibl.stencil.engine.parsing.StencilParser.ParamOutputBlockModeContext;
import com.impossibl.stencil.engine.parsing.StencilParser.ParameterDeclContext;
import com.impossibl.stencil.engine.parsing.StencilParser.SafeMemberSelectorContext;
import com.impossibl.stencil.engine.parsing.StencilParser.SimpleNameContext;
import com.impossibl.stencil.engine.parsing.StencilParser.StringLiteralContext;
import com.impossibl.stencil.engine.parsing.StencilParser.UnnamedOutputBlockContext;
import com.impossibl.stencil.engine.parsing.StencilParser.VariableDeclContext;
import com.impossibl.stencil.engine.parsing.StencilParser.VariableRefContext;

public class Contexts {

  public static Boolean value(BooleanLiteralContext ctx) {
    return Boolean.parseBoolean(ctx.getText());
  }
  
  public static Number value(IntegerLiteralContext ctx) {
    String val = ctx.getText();
    switch(val.charAt(val.length()-1)) {
    case 's':
      return Short.decode(val.substring(0, val.length()-1));
    case 'l':
      return Long.decode(val.substring(0, val.length()-1));
    case 'i':
      val = val.substring(0, val.length()-1);
    default:
      return Integer.decode(val);
    }
  }
  
  public static Number value(FloatingLiteralContext ctx) {
    String val = ctx.getText();
    switch(val.charAt(val.length()-1)) {
    case 'f':
      return Float.parseFloat(val.substring(0, val.length()-1));
    case 'd':
      val = val.substring(0, val.length()-1);
    default:
      return Double.parseDouble(val);
    }
  }
  
  public static String value(StringLiteralContext ctx) {
    String text = ctx.getText();
    return text.substring(1, text.length()-1);
  }

  public static String value(SimpleNameContext ctx) {
    if(ctx.stringLit != null)
      return value(ctx.stringLit);
    if(ctx.id != null)
      return value(ctx.id);
    return null;
  }

  public static String value(ParserRuleContext ctx) {
    if(ctx == null)
      return null;
    return ctx.getText();
  }

  public static String value(TerminalNode node) {
    if(node == null)
      return null;
    return node.getText();
  }
  
  public static String value(Token token) {
    if(token == null)
      return null;
    return token.getText();
  }
  
  public static ParamOutputBlockMode value(ParamOutputBlockModeContext ctx) {
    if(ctx == null)
      return null;
    for(ParamOutputBlockMode mode : ParamOutputBlockMode.values()) {
      if(mode.name().toLowerCase().equals(ctx.getText()))
        return mode;
    }
    return null;
  }

  public static ParamOutputBlockMode mode(ParserRuleContext paramBlock) {
    if(paramBlock instanceof UnnamedOutputBlockContext)
      return value(((UnnamedOutputBlockContext) paramBlock).blockMode);
    else if(paramBlock instanceof NamedOutputBlockContext)
      return value(((NamedOutputBlockContext) paramBlock).blockMode);
    return null;
  }

  public static String name(LValueRefContext ref) {
    if(ref == null)
      return null;
    return value(ref.id);
  }

  public static String name(NamedValueContext val) {
    if(val == null)
      return null;
    return value(val.name); 
  }

  public static String name(VariableRefContext ref) {
    if(ref == null)
      return null;
    return value(ref.id); 
  }

  public static String name(VariableDeclContext decl) {
    if(decl == null)
      return null;
    return value(decl.id); 
  }

  public static String name(ParameterDeclContext decl) {
    return value(decl.id); 
  }

  public static String name(BlockDeclContext decl) {
    if(decl == null)
      return null;
    return value(decl.id);
  }
  
  public static String name(NamedOutputBlockContext block) {
    if(block == null)
      return null;
    return value(block.id);
  }
  
  public static String name(MethodCallSelectorContext ctx) {
    if(ctx == null)
      return null;
    return value(ctx.id);
  }
  
  public static String name(MemberSelectorContext ctx) {
    if(ctx == null)
      return null;
    return value(ctx.id);
  }
  
  public static String name(SafeMemberSelectorContext ctx) {
    if(ctx == null)
      return null;
    return value(ctx.id);
  }
  
  public static String name(MemberIndexSelectorContext ctx) {
    if(ctx == null)
      return null;
    return value(ctx.id);
  }
  
  public static String name(MacroDefinitionContext ctx) {
    if(ctx == null)
      return null;
    return value(ctx.id);
  }
  
  public static String name(FunctionDefinitionContext ctx) {
    if(ctx == null)
      return null;
    return value(ctx.id);
  }
  
  public static CallableSignatureContext createCallableSignatureContext(List<ParameterDeclContext> paramDecls) {
    
    CallableSignatureContext sig = new CallableSignatureContext(null, -1);
    
    sig.paramDecls = paramDecls;
    
    return sig;
  }
  
  public static ParameterDeclContext createParameterDecl(ParserRuleContext parent, String name, boolean all) {

    ParameterDeclContext paramDecl = new ParameterDeclContext(parent,-1);
    
    if(all) {
      paramDecl.flag = createToken(StencilParser.MUL, "*");
    }
    
    paramDecl.id = createToken(StencilParser.ID, name);
    
    return paramDecl;
  }
  
  public static BlockDeclContext createBlockDecl(ParserRuleContext parent, String name, boolean all, boolean unnamed) {

    BlockDeclContext blockDecl = new BlockDeclContext(parent,-1);
    
    if(all) {
      blockDecl.flag = createToken(StencilParser.MUL, "*");
    }
    if(unnamed) {
      blockDecl.flag = createToken(StencilParser.ADD, "+");
    }
    
    blockDecl.id = createToken(StencilParser.ID, name);
    
    return blockDecl;
  }
  
  public static Token createToken(int ttype, String text) {
    return new CommonToken(ttype, text);
  }

}
