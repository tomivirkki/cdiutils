package org.vaadin.virkki.cdiutils.application;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.vaadin.virkki.cdiutils.application.VaadinContext.BeanStoreContainer;
import org.vaadin.virkki.cdiutils.application.VaadinContext.VaadinScoped;

import com.vaadin.ui.UI;

@VaadinScoped
public class ContextHandle {

    @Inject
    private BeanStoreContainer beanStoreContainer;
    @Inject
    private BeanManager beanManager;

    /**
     * Requests VaadinContext to dereference a bean instance of the provided
     * class. The instance will not be gc'd until it's completely dereferenced
     * by the application as well.
     * 
     * @param instance
     */
    public void dereferenceBeanInstance(final Class<? extends Object> beanClass) {
        final Bean<?> bean = beanManager.getBeans(beanClass).iterator().next();
        beanStoreContainer.getBeanStore(UI.getCurrent())
                .dereferenceBeanInstance(bean);
    }
}
