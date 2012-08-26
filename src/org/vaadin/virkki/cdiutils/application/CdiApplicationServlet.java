package org.vaadin.virkki.cdiutils.application;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.vaadin.virkki.cdiutils.application.VaadinContext.BeanStoreContainer;

import com.vaadin.Application;
import com.vaadin.shared.ApplicationConstants;
import com.vaadin.terminal.CombinedRequest;
import com.vaadin.terminal.DefaultRootProvider;
import com.vaadin.terminal.WrappedRequest;
import com.vaadin.terminal.gwt.server.ApplicationServlet;
import com.vaadin.ui.Root;

@SuppressWarnings("serial")
public class CdiApplicationServlet extends ApplicationServlet {

    @Inject
    private Instance<Root> rootInstance;
    @Inject
    private BeanStoreContainer beanStoreContainer;

    @Override
    protected Application getNewApplication(final HttpServletRequest request)
            throws ServletException {
        final Application app = super.getNewApplication(request);
        app.addRootProvider(new DefaultRootProvider() {

            @Override
            public Root instantiateRoot(final Application application,
                    final Class<? extends Root> type,
                    final WrappedRequest request) {
                Root root = null;
                final Integer rootId = getRootId(request);
                if (rootId != null) {
                    root = application.getRootById(rootId);
                }
                if (root == null) {
                    Root.setCurrent(null);
                    root = rootInstance.select(type.asSubclass(Root.class))
                            .get();
                    root.setApplication(application);
                    beanStoreContainer.rootInitialized(root);
                }
                return root;
            }
        });
        return app;
    }

    private static Integer getRootId(WrappedRequest request) {
        if (request instanceof CombinedRequest) {
            // Combined requests has the rootid parameter in the second request
            final CombinedRequest combinedRequest = (CombinedRequest) request;
            request = combinedRequest.getSecondRequest();
        }
        final String rootIdString = request
                .getParameter(ApplicationConstants.ROOT_ID_PARAMETER);
        final Integer rootId = rootIdString == null ? null : new Integer(
                rootIdString);
        return rootId;
    }
}
