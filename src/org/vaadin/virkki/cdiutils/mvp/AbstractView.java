package org.vaadin.virkki.cdiutils.mvp;

import org.vaadin.virkki.cdiutils.application.VaadinContext.VaadinScoped;

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
            for (Class<?> clazz : AbstractView.this.getClass().getInterfaces()) {
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

    /**
     * Initialize the view
     */
    protected abstract void initView();
}
