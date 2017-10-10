package com.impossibl.stencil.ext.std;

import org.apache.commons.lang3.StringEscapeUtils;

public class HtmlExtensionMethods {
  
  public static String html(Object obj) {
    String str = obj.toString();
    return StringEscapeUtils.escapeHtml4(str);
  }

}
