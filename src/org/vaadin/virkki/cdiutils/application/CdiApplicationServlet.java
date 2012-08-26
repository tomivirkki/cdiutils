package org.vaadin.virkki.cdiutils.application;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.vaadin.virkki.cdiutils.CdiUtilsException;
import org.vaadin.virkki.cdiutils.application.VaadinContext.VaadinScoped;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.ApplicationServlet;

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
public class CdiApplicationServlet extends ApplicationServlet {
    @Inject
    private Instance<Application> applicationInstance;

    @Override
    protected Application getNewApplication(final HttpServletRequest request)
            throws ServletException {
        try {
            return applicationInstance.select(getApplicationClass()).get();
        } catch (final ClassNotFoundException e) {
            throw new CdiUtilsException("No application class found", e);
        }
    }
}