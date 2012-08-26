package org.vaadin.virkki.cdiutils.application;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.vaadin.virkki.cdiutils.application.VaadinContext.BeanStoreContainer;

import com.vaadin.Application;
import com.vaadin.shared.ui.ui.UIConstants;
import com.vaadin.terminal.CombinedRequest;
import com.vaadin.terminal.DefaultUIProvider;
import com.vaadin.terminal.WrappedRequest;
import com.vaadin.terminal.gwt.server.ApplicationServlet;
import com.vaadin.ui.UI;

@SuppressWarnings("serial")
public class CdiApplicationServlet extends ApplicationServlet {

    @Inject
    private Instance<UI> uiInstance;
    @Inject
    private BeanStoreContainer beanStoreContainer;

    @Override
    protected Application getNewApplication(final HttpServletRequest request)
            throws ServletException {
        final Application app = super.getNewApplication(request);
        app.addUIProvider(new DefaultUIProvider() {
            @Override
            public UI instantiateUI(final Application application,
                    final Class<? extends UI> type, final WrappedRequest request) {
                UI ui = null;
                final Integer uiId = getUIId(request);
                if (uiId != null) {
                    ui = application.getUIById(uiId);
                }
                if (ui == null) {

                    UI.setCurrent(null);
                    ui = uiInstance.select(type.asSubclass(UI.class)).get();
                    ui.setApplication(application);
                    beanStoreContainer.uiInitialized(ui);
                }
                return ui;
            }
        });

        return app;
    }

    private static Integer getUIId(WrappedRequest request) {
        if (request instanceof CombinedRequest) {
            // Combined requests has the uiId parameter in the second request
            final CombinedRequest combinedRequest = (CombinedRequest) request;
            request = combinedRequest.getSecondRequest();
        }
        final String uiIdString = request
                .getParameter(UIConstants.UI_ID_PARAMETER);
        final Integer uiId = uiIdString == null ? null
                : new Integer(uiIdString);
        return uiId;
    }
}
