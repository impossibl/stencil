package com.impossibl.stencil.ext.std;

import org.apache.commons.lang3.StringEscapeUtils;

import com.impossibl.stencil.api.ExtensionMethods;

public class HtmlExtensionMethods implements ExtensionMethods {
  
  public static String html(Object obj) {
    String str = obj.toString();
    return StringEscapeUtils.escapeHtml4(str);
  }

}
