package org.vaadin.virkki.cdiutils.mvp;

import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;

import org.vaadin.virkki.cdiutils.application.VaadinContext.VaadinScoped;
import org.vaadin.virkki.cdiutils.componentproducers.Localizer;

/**
 * Abstract implementation of CDI Utils MVP-pattern view.
 * 
 * @author Tomi Virkki / Vaadin Ltd
 */
@SuppressWarnings("serial")
@VaadinScoped
public abstract class AbstractView extends ViewComponent implements View {
    private boolean initialized;

    protected Class<? extends View> viewInterface;

    @SuppressWarnings("unchecked")
    @Override
    public void openView() {
        if (viewInterface == null) {
            // Determine the view interface
            for (final Class<?> clazz : AbstractView.this.getClass()
                    .getInterfaces()) {
                if (!clazz.equals(View.class)
                        && View.class.isAssignableFrom(clazz)) {
                    viewInterface = (Class<? extends View>) clazz;
                }
            }
        }
        if (!initialized) {
            initView();
            initialized = true;
            logger.info("View initialized: " + viewInterface);
        }

        fireViewEvent(viewInterface.getName() + AbstractPresenter.VIEW_OPEN,
                this);
        logger.info("View accessed: " + viewInterface);
    }

    void observeLocalize(
            @Observes(notifyObserver = Reception.IF_EXISTS) @CDIEvent(Localizer.UPDATE_LOCALIZED_VALUES) final ParameterDTO object) {
        localize();
    }

    /**
     * Override to localize the view.
     * 
     * @CDIEvent(Localizer.UPDATE_LOCALIZED_VALUES) event will eventually invoke
     *                                              this method
     */
    protected void localize() {
    }

    /**
     * Initialize the view
     */
    protected abstract void initView();
}
