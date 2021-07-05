package br.com.anteros.remote.synch.util;

import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;

public class RemoteSynchApplicationContext {

    private static ConfigurableApplicationContext applicationContext;


    public static void setApplicationContext(ConfigurableApplicationContext applicationContext){
        RemoteSynchApplicationContext.applicationContext = applicationContext;
    }

    public static ConfigurableApplicationContext getApplicationContext(){
        return RemoteSynchApplicationContext.applicationContext;
    }

    public static void processInjectionBasedOnCurrentContext(Object target) {
        AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
        bpp.setBeanFactory(applicationContext.getAutowireCapableBeanFactory());
        bpp.processInjection(target);
    }
}
