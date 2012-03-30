package org.vaadin.virkki.cdiutils.application;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.vaadin.virkki.cdiutils.CdiUtilsException;
import org.vaadin.virkki.cdiutils.application.VaadinContext.VaadinScoped;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.AbstractApplicationServlet;

/**
 * {@link com.vaadin.terminal.gwt.server.AbstractApplicationServlet
 * AbstractApplicationServlet} extension which instantiates new
 * {@link org.vaadin.virkki.cdiutils.application.AbstractCdiApplication
 * AbstractCdiApplications} in {@link VaadinScoped}. Extensions must declare
 * Application class with {@link ApplicationClass} annotation.
 * 
 * @author Tomi Virkki / Vaadin Ltd
 */
@SuppressWarnings("serial")
public abstract class AbstractCdiApplicationServlet extends
        AbstractApplicationServlet {

    @Inject
    private BeanManager beanManager;

    @Override
    protected AbstractCdiApplication getNewApplication(
            final HttpServletRequest request) throws ServletException {
        try {
            final Bean<?> bean = beanManager.getBeans(getApplicationClass())
                    .iterator().next();
            final AbstractCdiApplication application = (AbstractCdiApplication) beanManager
                    .getReference(bean, bean.getBeanClass(),
                            beanManager.createCreationalContext(bean));
            return application;
        } catch (final ClassNotFoundException e) {
            throw new CdiUtilsException("No application class found", e);
        }
    }

    @Override
    protected Class<? extends Application> getApplicationClass()
            throws ClassNotFoundException {
        final ApplicationClass applicationClass = getClass().getAnnotation(
                ApplicationClass.class);
        if (applicationClass == null) {
            throw new CdiUtilsException("No application class defined");
        }
        return applicationClass.value();
    }

    /**
     * Provides {@link AbstractCdiApplicationServlet} information about the
     * application class.
     * 
     * @author Tomi Virkki / Vaadin Ltd
     */
    @Target({ ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ApplicationClass {
        Class<? extends AbstractCdiApplication> value();
    }
}