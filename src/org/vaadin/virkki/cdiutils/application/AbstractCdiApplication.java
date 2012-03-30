package org.vaadin.virkki.cdiutils.application;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.vaadin.virkki.cdiutils.CdiUtilsException;
import org.vaadin.virkki.cdiutils.application.VaadinContext.ApplicationBeanStore;
import org.vaadin.virkki.cdiutils.application.VaadinContext.VaadinScoped;
import org.vaadin.virkki.cdiutils.application.VaadinContext.VaadinScoped.VaadinScope;

import com.vaadin.Application;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.Window;

/**
 * Vaadin {@link com.vaadin.Application Application} class extension for CDI
 * Utils applications.
 * 
 * @author Tomi Virkki / Vaadin Ltd
 */
@SuppressWarnings("serial")
@VaadinScoped(VaadinScope.APPLICATION)
public abstract class AbstractCdiApplication extends Application implements
        HttpServletRequestListener {

    @Inject
    private BeanManager beanManager;
    @Inject
    private Instance<RequestData> requestData;

    private ApplicationBeanStore beanStore;

    @Override
    public void close() {
        super.close();
        beanStore.dereferenceAllBeanInstances();
    }

    @Override
    public void onRequestStart(final HttpServletRequest request,
            final HttpServletResponse response) {
        requestData.get().setApplication(this);
    }

    ApplicationBeanStore getBeanStore() {
        if (beanStore == null) {
            beanStore = new ApplicationBeanStore(beanManager);
        }
        return beanStore;
    }

    @Override
    public void onRequestEnd(final HttpServletRequest request,
            final HttpServletResponse response) {
        // NOP
    }

    /**
     * Do not invoke!
     */
    @Override
    public final Window getWindow(final String name) {
        Window window = getExistingWindow(name);
        if (window == null) {
            window = instantiateNewWindowIfNeeded(name);
            if (window != null) {
                if (window.getContent().getComponentIterator().hasNext()) {
                    throw new CdiUtilsException(
                            "instantiateNewWindowIfNeeded() should only be used "
                                    + "for instantiating new Windows. Populate the Window"
                                    + "in buildNewWindow(Window)");
                }
                window.setName(name);
                addWindow(window);
                requestData.get().setWindow(window);
                buildNewWindow(window);
                window.open(new ExternalResource(window.getURL()));
            }
        }
        requestData.get().setWindow(window);
        return window;
    }

    protected Window getExistingWindow(final String name) {
        return super.getWindow(name);
    }

    /**
     * If multi-window support required, this method should be overridden to
     * return a new Window instance. The window should not have any content at
     * this point and most importantly, no VaadinScoped references should be
     * requested during this method. The created Window should be populated in
     * buildNewWindow(Window)
     * 
     * @param name
     * @return
     */
    protected Window instantiateNewWindowIfNeeded(final String name) {
        return null;
    }

    /**
     * If multi-window support required, this method should be overridden to
     * populate the content of the window instantiated in
     * instantiateNewWindowIfNeeded(String).
     * 
     * @param newWindow
     */
    protected void buildNewWindow(final Window newWindow) {
        // NOP
    }

    /**
     * Requests VaadinContext to dereference a bean instance of the provided
     * class. The instance will not be gc'd until it's completely dereferenced
     * by the application as well.
     * 
     * @param instance
     */
    public void dereferenceBeanInstance(final Class<? extends Object> beanClass) {
        final Bean<?> bean = beanManager.getBeans(beanClass).iterator().next();
        beanStore.dereferenceBeanInstance(bean);
    }

    /**
     * Requests VaadinContext to dereference every
     * VaadinScoped(VaadinScope.WINDOW) bean instance of a context defined by
     * the window.
     * 
     * @param window
     */
    public void dereferenceContext(final Window window) {
        beanStore.dereferenceContext(window);
    }

}
