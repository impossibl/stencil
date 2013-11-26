package com.impossibl.stencil.engine.parsing;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;

public class StencilErrorStrategy extends BailErrorStrategy {

  @Override
  public void reportError(Parser recognizer, RecognitionException e) {
    if (e instanceof InvalidSignatureException) {
      recognizer.notifyErrorListeners(e.getOffendingToken(), e.getMessage(), e);
      return;
    }
    super.reportError(recognizer, e);
  }

}
