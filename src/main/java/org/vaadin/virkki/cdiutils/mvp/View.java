package org.vaadin.virkki.cdiutils.mvp;

/**
 * Superinterface of each CDI Utils MVP-pattern View interface.
 * 
 * @author Tomi Virkki / Vaadin Ltd
 */
public interface View {
    /**
     * Called (by the application logic) whenever the view is opened/accessed.
     */
    void openView();
}
