package org.vaadin.virkki.cdiutils.mvp;

import java.util.logging.Logger;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.inject.Inject;

import org.vaadin.virkki.cdiutils.TextBundle;
import org.vaadin.virkki.cdiutils.application.AbstractCdiApplication;
import org.vaadin.virkki.cdiutils.application.RequestData;
import org.vaadin.virkki.cdiutils.componentproducers.Preconfigured;
import org.vaadin.virkki.cdiutils.mvp.CDIEvent.CDIEventImpl;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Window;

/**
 * Superclass for views and their subcomponents.
 * 
 * @author Tomi Virkki / Vaadin Ltd
 */
@SuppressWarnings("serial")
public abstract class ViewComponent extends CustomComponent {
    @Inject
    private javax.enterprise.event.Event<ParameterDTO> viewEvent;
    @Inject
    private Instance<TextBundle> textBundle;
    @Inject
    private Instance<RequestData> requestData;
    @Inject
    @Preconfigured
    protected transient Logger logger;

    protected String getText(final String key, final Object... params) {
        try {
            return textBundle.get().getText(key, params);
        } catch (final UnsatisfiedResolutionException e) {
            return "No TextBundle implementation found!";
        }
    }

    protected void fireViewEvent(final String methodIdentifier,
            final Object primaryParameter, final Object... secondaryParameters) {
        viewEvent.select(new CDIEventImpl(methodIdentifier)).fire(
                new ParameterDTO(primaryParameter, secondaryParameters));
    }

    /**
     * Returns the {@link Window} bound to the current request.
     * 
     * @return
     */
    protected Window getContextWindow() {
        Window window = requestData.get().getWindow();
        if (window == null) {
            window = getContextApplication().getMainWindow();
        }
        return window;
    }

    /**
     * Returns the {@link AbstractCdiApplication} bound to the current request.
     * 
     * @return
     */
    protected AbstractCdiApplication getContextApplication() {
        return requestData.get().getApplication();
    }
}
