package org.vaadin.virkki.cdiutils.mvp;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.vaadin.virkki.cdiutils.application.VaadinContext.VaadinScoped;
import org.vaadin.virkki.cdiutils.componentproducers.Preconfigured;

/**
 * Abstract implementation of CDI Utils MVP-pattern presenter. Associated
 * {@link org.vaadin.virkki.cdiutils.mvp.View View} interface extension is
 * declared for each extended AbstractPresenter using
 * {@link org.vaadin.virkki.cdiutils.mvp.AbstractPresenter.ViewInterface
 * ViewInterface} annotation.
 * 
 * @author Tomi Virkki / Vaadin Ltd
 */
@SuppressWarnings("serial")
@VaadinScoped
public abstract class AbstractPresenter<T extends View> implements Serializable {
    @Inject
    @Preconfigured
    protected transient Logger logger;
    @Inject
    private BeanManager beanManager;

    protected T view;

    public static final String VIEW_OPEN = "AbstractPresenter_vo";

    @SuppressWarnings("unchecked")
    @PostConstruct
    protected void postConstruct() {
        // ViewInterface must be defined
        Class<? extends View> viewInterface = getClass().getAnnotation(
                ViewInterface.class).value();
        // Just one bean implementing the view interface should be found
        Bean<?> bean = beanManager.getBeans(viewInterface).iterator().next();
        // Get the contextual instance
        view = (T) beanManager.getReference(bean, bean.getBeanClass(),
                beanManager.createCreationalContext(bean));

        initPresenter();
        logger.info("Presenter initialized: " + getClass());
    }

    /**
     * Initializes the presenter.
     */
    protected abstract void initPresenter();

    /**
     * Performs view actions called each time the view is opened.
     */
    public abstract void viewOpened();

    /**
     * Declares a view interface for CDI Utils MVP-pattern presenter
     * implementation.
     * 
     * @author Tomi Virkki / Vaadin Ltd
     */
    @Target({ ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface ViewInterface {
        Class<? extends View> value();
    }
}
