package org.vaadin.virkki.cdiutils.application;

import javax.enterprise.context.RequestScoped;

import com.vaadin.ui.Window;

/**
 * Class which wraps request specific information for CDI Utils.
 * 
 * @author Tomi Virkki / Vaadin Ltd
 */
@RequestScoped
public class RequestData {

    private AbstractCdiApplication application;
    private Window window;

    public AbstractCdiApplication getApplication() {
        return application;
    }

    public void setApplication(final AbstractCdiApplication application) {
        this.application = application;
    }

    public Window getWindow() {
        return window;
    }

    public void setWindow(final Window window) {
        this.window = window;
    }

}
