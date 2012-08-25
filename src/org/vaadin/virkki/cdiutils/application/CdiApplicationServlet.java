package org.vaadin.virkki.cdiutils.application;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.vaadin.Application;
import com.vaadin.terminal.DefaultRootProvider;
import com.vaadin.terminal.WrappedRequest;
import com.vaadin.terminal.gwt.server.ApplicationServlet;
import com.vaadin.ui.Root;

@SuppressWarnings("serial")
public class CdiApplicationServlet extends ApplicationServlet {

    @Inject
    private Instance<AbstractCdiRoot> rootInstance;

    //
    // private Map<Root, RootBeanStore> beanStores = new HashMap<Root,
    // VaadinContext.RootBeanStore>();

    @Override
    protected Application getNewApplication(final HttpServletRequest request)
            throws ServletException {
        final Application app = super.getNewApplication(request);
        app.addRootProvider(new DefaultRootProvider() {

            private final Map<Application, AbstractCdiRoot> roots = new HashMap<Application, AbstractCdiRoot>();

            @Override
            public Root instantiateRoot(final Application application,
                    final Class<? extends Root> type,
                    final WrappedRequest request) {
                // TODO: Exceptions
                AbstractCdiRoot root = roots.get(application);
                if (root == null
                        || !root.getApplication().getRoots().contains(root)) {

                    root = rootInstance.select(
                            type.asSubclass(AbstractCdiRoot.class)).get();
                    root.setApplication(app);
                }
                return root;
            }
        });
        return app;
    }
}
