package org.vaadin.virkki.cdiutils.application;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.vaadin.virkki.cdiutils.application.VaadinContext.RootBeanStore;
import org.vaadin.virkki.cdiutils.application.VaadinContext.VaadinScoped;

import com.vaadin.ui.Root;

@SuppressWarnings("serial")
@VaadinScoped
public abstract class AbstractCdiRoot extends Root {

    @Inject
    private BeanManager beanManager;

    // TODO: Move to servlet, no need for this class anymore?
    private final RootBeanStore beanStore = new RootBeanStore();

    RootBeanStore getBeanStore() {
        return beanStore;
    }

    public AbstractCdiRoot() {
        Root.setCurrent(this);
    }

    /**
     * Requests VaadinContext to dereference a bean instance of the provided
     * class.
     * 
     * @param instance
     */
    public void dereferenceBeanInstance(final Class<? extends Object> beanClass) {
        final Bean<?> bean = beanManager.getBeans(beanClass).iterator().next();
        beanStore.dereferenceBeanInstance(bean);
    }

}
