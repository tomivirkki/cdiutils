package org.vaadin.virkki.cdiutils.componentproducers;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.inject.Inject;

import org.vaadin.virkki.cdiutils.TextBundle;
import org.vaadin.virkki.cdiutils.application.VaadinContext.VaadinScoped;
import org.vaadin.virkki.cdiutils.mvp.CDIEvent;
import org.vaadin.virkki.cdiutils.mvp.ParameterDTO;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

/**
 * Collection of components whose captions or value should be localized
 * 
 * @author Tomi Virkki / Vaadin Ltd
 */
@SuppressWarnings("serial")
@VaadinScoped
public class Localizer implements Serializable {
    public static final String UPDATE_LOCALIZED_VALUES = "update_localized_values";

    @Inject
    private Instance<TextBundle> textBundle;

    private final Map<Component, String> localizedCaptions = new HashMap<Component, String>();
    private final Map<Label, String> localizedLabelValues = new HashMap<Label, String>();

    void updateCaption(
            @Observes @CDIEvent(UPDATE_LOCALIZED_VALUES) final ParameterDTO parameters) {
        for (final Entry<Component, String> entry : localizedCaptions
                .entrySet()) {
            try {
                entry.getKey().setCaption(
                        textBundle.get().getText(entry.getValue()));
            } catch (final UnsatisfiedResolutionException e) {
                entry.getKey()
                        .setCaption("No TextBundle implementation found!");
            }
        }

        for (final Entry<Label, String> entry : localizedLabelValues.entrySet()) {
            try {
                entry.getKey().setValue(
                        textBundle.get().getText(entry.getValue()));
            } catch (final UnsatisfiedResolutionException e) {
                entry.getKey()
                        .setCaption("No TextBundle implementation found!");
            }
        }
    }

    public void addLocalizedCaption(final Component component,
            final String captionKey) {
        localizedCaptions.put(component, captionKey);
    }

    public void addLocalizedLabelValue(final Label label,
            final String labelValueKey) {
        localizedLabelValues.put(label, labelValueKey);
    }
}
