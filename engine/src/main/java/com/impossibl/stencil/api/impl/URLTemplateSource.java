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

public class URLTemplateSource implements TemplateSource {
  
  URL url;
  URLConnection urlConnection;
  Cache<String,String> md5Cache = CacheBuilder.newBuilder().build();
  
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
