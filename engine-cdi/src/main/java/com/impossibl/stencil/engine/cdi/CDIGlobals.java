package com.impossibl.stencil.engine.cdi;

import com.impossibl.stencil.api.GlobalScope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import java.util.Set;

public class CDIGlobals implements GlobalScope {
  
  private static final Logger logger = LogManager.getLogger(CDIGlobals.class);

  private BeanManager beanManager = CDI.current().getBeanManager();
  private CreationalContext<?> globalContext = beanManager.createCreationalContext(null);

  public CDIGlobals() {
    logger.info("Initializing CDI Globals");
  }

  public Object get(String name) {
    
    Set<Bean<?>> beans = beanManager.getBeans(name);
    if(beans.isEmpty()) {
      return NOT_FOUND;
    }
    
    Bean<?> bean = beanManager.resolve(beans);
    
    return beanManager.getReference(bean, Object.class, globalContext);
  }

}
