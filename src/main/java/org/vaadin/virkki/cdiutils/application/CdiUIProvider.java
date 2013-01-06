package org.vaadin.virkki.cdiutils.application;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.vaadin.virkki.cdiutils.application.UIContext.BeanStoreContainer;

import com.vaadin.server.ClientConnector.DetachEvent;
import com.vaadin.server.ClientConnector.DetachListener;
import com.vaadin.server.DefaultUIProvider;
import com.vaadin.server.UICreateEvent;
import com.vaadin.ui.UI;
import com.vaadin.util.CurrentInstance;

@SuppressWarnings("serial")
public class CdiUIProvider extends DefaultUIProvider {

    private BeanManager beanManager;

    public CdiUIProvider() {
        final String name = "java:comp/" + BeanManager.class.getSimpleName();
        try {
            final InitialContext ic = new InitialContext();
            beanManager = (BeanManager) ic.lookup(name);
        } catch (final NamingException e) {
            throw new RuntimeException("Error in BeanManager lookup", e);
        }
    }

    @Override
    public UI createInstance(final UICreateEvent event) {
        CurrentInstance.set(UICreateEvent.class, event);

        final Bean<?> uiBean = beanManager.getBeans(event.getUIClass())
                .iterator().next();
        final UI ui = (UI) beanManager.getReference(uiBean,
                uiBean.getBeanClass(),
                beanManager.createCreationalContext(uiBean));

        ui.addDetachListener(new DetachListener() {
            @Override
            public void detach(final DetachEvent event) {
                final Bean<?> containerBean = beanManager
                        .getBeans(BeanStoreContainer.class).iterator().next();
                final BeanStoreContainer beanStoreContainer = (BeanStoreContainer) beanManager
                        .getReference(containerBean, containerBean
                                .getBeanClass(), beanManager
                                .createCreationalContext(containerBean));
                beanStoreContainer.uiDetached(ui.getUIId());
            }
        });

        CurrentInstance.set(UICreateEvent.class, null);

        return ui;
    }
}
