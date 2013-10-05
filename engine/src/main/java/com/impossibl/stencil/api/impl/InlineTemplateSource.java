package com.impossibl.stencil.api.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;

import com.google.common.hash.Hashing;
import com.impossibl.stencil.api.TemplateSource;

/**
 * Template source that holds the source text passed in inside the source
 * object itself. 
 * 
 * @author kdubb
 *
 */
public class InlineTemplateSource implements TemplateSource {
	
	URI uri;
	String text;

	public InlineTemplateSource(String text) {
		this.text = text;
		this.uri = URI.create("file:" + hash(text));
	}
	
	/**
	 * Get MD5 hash of the source text
	 * 
	 * @param text Source text to hash
	 * @return MD5 hash of the source text
	 */
	static String hash(String text) {
		return Hashing.md5().hashString(text).toString();
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public URI getURI() throws IOException {
		return uri;
	}

	@Override
	public Reader openReader() throws IOException {
		return new StringReader(text);
	}

	@Override
	public String getTag() throws IOException {
		return hash(text);
	}

  @Override
  public String toString() {
    return "InlineTemplateSource [" + uri + "]";
  }

}
