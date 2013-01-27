package org.vaadin.virkki.cdiutils.mvp;

import java.util.logging.Logger;

import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.inject.Inject;

import org.vaadin.virkki.cdiutils.TextBundle;
import org.vaadin.virkki.cdiutils.componentproducers.Localizer;
import org.vaadin.virkki.cdiutils.componentproducers.Preconfigured;
import org.vaadin.virkki.cdiutils.mvp.CDIEvent.CDIEventImpl;

import com.vaadin.ui.CustomComponent;

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

    void observeLocalize(
            @Observes(notifyObserver = Reception.IF_EXISTS) @CDIEvent(Localizer.UPDATE_LOCALIZED_VALUES) final ParameterDTO object) {
        localize();
    }

    /**
     * Override to localize the view. Firing a
     * 
     * @CDIEvent(Localizer.UPDATE_LOCALIZED_VALUES) event will eventually invoke
     *                                              this method
     */
    protected void localize() {
    }
}
