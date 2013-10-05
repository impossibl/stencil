package com.impossibl.stencil.api.impl;

import static com.google.common.net.HttpHeaders.ETAG;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.impossibl.stencil.api.TemplateSource;

/**
 * Template source based on any URL.
 * 
 * Uses either the ETAG header of the URL it builds, and caches, an MD5 for the
 * URL.
 * 
 * @author kdubb
 *
 */
public class URLTemplateSource implements TemplateSource {
  
  URL url;
  URLConnection urlConnection;
  private static Cache<String,String> md5Cache = CacheBuilder.newBuilder().build();

  /**
   * Constructs a template source for the given url
   * 
   * @param url
   */
  public URLTemplateSource(URL url) {
    this.url = url;
  }

  @Override
  public URI getURI() throws IOException {
    try {
      return url.toURI();
    }
    catch (URISyntaxException e) {
      throw new IOException(e);
    }
  }

  @Override
  public Reader openReader() throws IOException {
    return new InputStreamReader(url.openStream(), StandardCharsets.UTF_8);
  }

  /**
   * Gets the tag using either the ETAG header of the URL connection or
   * calculates it and caches it based on the URL & last modified date
   */
  @Override
  public String getTag() throws IOException {

    URLConnection urlConnection = url.openConnection();
    
    String tag = urlConnection.getHeaderField(ETAG);
    if(tag == null) {
    
      String key = url.toString() + "@" + urlConnection.getLastModified();
      
      tag = md5Cache.getIfPresent(key);

      if(tag == null) {
          
        try(InputStream urlStream = urlConnection.getInputStream()) {
      
          byte[] data = ByteStreams.toByteArray(urlConnection.getInputStream());
      
          tag = Hashing.md5().hashBytes(data).toString();
      
          md5Cache.put(key, tag);
        }
        
      }
      
    }
    
    return tag;
  }

  @Override
  public void close() throws IOException {
  }

  @Override
  public String toString() {
    return "URLTemplateSource [" + url + "]";
  }

}
