package com.impossibl.stencil.engine.parsing;

import org.antlr.v4.runtime.IntStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

public class InvalidSignatureException extends RecognitionException {

  public InvalidSignatureException(String message, Recognizer<?, ?> recognizer, IntStream input, ParserRuleContext ctx, Token flagToken) {
    super(message, recognizer, input, ctx);
    setOffendingToken(flagToken);
  }

  private static final long serialVersionUID = 3764903354281993004L;

}
