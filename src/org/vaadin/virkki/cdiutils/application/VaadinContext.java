package org.vaadin.virkki.cdiutils.application;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Scope;

import org.vaadin.virkki.cdiutils.application.VaadinContext.VaadinScoped.VaadinScope;
import org.vaadin.virkki.cdiutils.mvp.AbstractPresenter;
import org.vaadin.virkki.cdiutils.mvp.AbstractPresenter.ViewInterface;
import org.vaadin.virkki.cdiutils.mvp.AbstractView;

import com.vaadin.ui.Window;

/**
 * CDI Extension which registers VaadinContextImpl context.
 * 
 * @author Tomi Virkki / Vaadin Ltd
 */
public class VaadinContext implements Extension {

    void afterBeanDiscovery(
            @Observes final AfterBeanDiscovery afterBeanDiscovery,
            final BeanManager beanManager) {
        afterBeanDiscovery.addContext(new VaadinContextImpl(beanManager));
    }

    /**
     * Custom CDI context for Vaadin applications. Stores references to bean
     * instances in the scope of Vaadin Application or Vaadin application-level
     * Window.
     * 
     * @author Tomi Virkki / Vaadin Ltd
     */
    public static class VaadinContextImpl implements Context {
        // TODO: logging
        private final BeanManager beanManager;

        public VaadinContextImpl(final BeanManager beanManager) {
            this.beanManager = beanManager;
        }

        public RequestData getRequestData() {
            final Bean<?> bean = beanManager.getBeans(RequestData.class)
                    .iterator().next();
            return (RequestData) beanManager.getReference(bean,
                    bean.getBeanClass(),
                    beanManager.createCreationalContext(bean));
        }

        @Override
        public <T> T get(final Contextual<T> contextual) {
            return doGet((Bean<T>) contextual, null);
        }

        @Override
        public <T> T get(final Contextual<T> contextual,
                final CreationalContext<T> creationalContext) {
            return doGet((Bean<T>) contextual, creationalContext);
        }

        private <T> T doGet(final Bean<T> bean,
                final CreationalContext<T> creationalContext) {

            if (creationalContext != null
                    && getRequestData().getApplication() == null
                    && AbstractCdiApplication.class.isAssignableFrom(bean
                            .getBeanClass())) {
                // Application doesn't exist yet, create one
                final AbstractCdiApplication application = (AbstractCdiApplication) bean
                        .create(creationalContext);
                final ApplicationBeanStore beanStore = application
                        .getBeanStore();
                beanStore.addBeanInstance(bean,
                        beanStore.new ContextualInstance<T>((T) application,
                                creationalContext));
                getRequestData().setApplication(application);
            }

            final AbstractCdiApplication application = getRequestData()
                    .getApplication();
            ApplicationBeanStore beanStore = null;
            if (application != null) {
                beanStore = application.getBeanStore();
            }

            T instance = null;
            if (beanStore != null) {
                instance = beanStore.getBeanInstance(bean, creationalContext);
            }
            return instance;
        }

        @Override
        public Class<? extends Annotation> getScope() {
            return VaadinScoped.class;
        }

        @Override
        public boolean isActive() {
            return true;
        }
    }

    /**
     * Datastructure for storing bean instances and child {@link BeanStore}s in
     * {@link VaadinScope}.APPLICATION context.
     * 
     * @author Tomi Virkki / Vaadin Ltd
     */
    static class ApplicationBeanStore extends BeanStore {
        private final Map<Window, BeanStore> windowBeanStores = new HashMap<Window, BeanStore>();

        public ApplicationBeanStore(final BeanManager beanManager) {
            super(beanManager);
        }

        @Override
        protected <T> T getBeanInstance(final Bean<T> bean,
                final CreationalContext<T> creationalContext) {
            final VaadinScope scope = bean.getBeanClass()
                    .getAnnotation(VaadinScoped.class).value();
            T instance = null;
            if (scope == VaadinScope.WINDOW) {
                final Window window = getRequestData().getWindow();
                final BeanStore windowBeanStore = getWindowBeanStore(window,
                        creationalContext != null);
                if (windowBeanStore != null) {
                    instance = windowBeanStore.getBeanInstance(bean,
                            creationalContext);
                }
            } else {
                instance = super.getBeanInstance(bean, creationalContext);
            }
            return instance;
        }

        public BeanStore getWindowBeanStore(final Window window,
                final boolean create) {
            Window key = window;
            if (key != null && key.getApplication().getMainWindow() == key) {
                // We're using null as the key value for main Window.
                key = null;
            }
            BeanStore beanStore = windowBeanStores.get(key);
            if (beanStore == null && create) {
                beanStore = new BeanStore(beanManager);
                windowBeanStores.put(key, beanStore);
            }
            return beanStore;
        }

        @Override
        public void dereferenceAllBeanInstances() {
            for (final Window window : new HashSet<Window>(
                    windowBeanStores.keySet())) {
                dereferenceContext(window);
            }
            super.dereferenceAllBeanInstances();
        }

        public void dereferenceContext(final Window window) {
            final BeanStore beanStore = getWindowBeanStore(window, false);
            if (beanStore != null) {
                beanStore.dereferenceAllBeanInstances();
            }

            if (window != null
                    && window.getApplication().getMainWindow() == window) {
                windowBeanStores.remove(null);
            } else {
                windowBeanStores.remove(window);
            }
        }

        @Override
        public <T> void dereferenceBeanInstance(final Bean<T> bean) {
            final VaadinScope scope = bean.getBeanClass()
                    .getAnnotation(VaadinScoped.class).value();
            if (scope == VaadinScope.WINDOW) {
                final Window window = getRequestData().getWindow();
                final BeanStore windowBeanStore = getWindowBeanStore(window,
                        false);
                if (windowBeanStore != null) {
                    windowBeanStore.dereferenceBeanInstance(bean);
                }
            } else {
                super.dereferenceBeanInstance(bean);
            }
        }
    }

    /**
     * Datastructure for storing bean instances in {@link VaadinScope}.WINDOW
     * context.
     * 
     * @author Tomi Virkki / Vaadin Ltd
     */
    static class BeanStore {
        private final Map<Bean<?>, ContextualInstance<?>> instances = new HashMap<Bean<?>, ContextualInstance<?>>();
        protected final BeanManager beanManager;

        public BeanStore(final BeanManager beanManager) {
            super();
            this.beanManager = beanManager;
        }

        protected RequestData getRequestData() {
            final Bean<?> bean = beanManager.getBeans(RequestData.class)
                    .iterator().next();
            return (RequestData) beanManager.getReference(bean,
                    bean.getBeanClass(),
                    beanManager.createCreationalContext(bean));
        }

        @SuppressWarnings("unchecked")
        protected <T> T getBeanInstance(final Bean<T> bean,
                final CreationalContext<T> creationalContext) {
            ContextualInstance<T> contextualInstance = (ContextualInstance<T>) instances
                    .get(bean);
            if (contextualInstance == null && creationalContext != null) {
                contextualInstance = new ContextualInstance<T>(
                        bean.create(creationalContext), creationalContext);
                addBeanInstance(bean, contextualInstance);
            }
            return contextualInstance != null ? contextualInstance
                    .getInstance() : null;
        }

        public void addBeanInstance(final Bean<?> bean,
                final ContextualInstance<?> instance) {
            instances.put(bean, instance);
        }

        public void dereferenceAllBeanInstances() {
            for (final Bean<?> bean : new HashSet<Bean<?>>(instances.keySet())) {
                dereferenceBeanInstance(bean);
            }
        }

        public <T> void dereferenceBeanInstance(final Bean<T> bean) {
            @SuppressWarnings("unchecked")
            final ContextualInstance<T> contextualInstance = (ContextualInstance<T>) instances
                    .get(bean);
            if (contextualInstance != null) {
                bean.destroy(contextualInstance.getInstance(),
                        contextualInstance.getCreationalContext());
                instances.remove(bean);
            }

            if (AbstractView.class.isAssignableFrom(bean.getBeanClass())) {
                // An AbstractView was dereferenced. The presenter should be
                // dereferenced as well
                Bean<?> removablePresenterBean = null;
                for (final Bean<?> presenterBean : instances.keySet()) {
                    if (AbstractPresenter.class.isAssignableFrom(presenterBean
                            .getBeanClass())) {
                        final ViewInterface viewInterface = presenterBean
                                .getBeanClass().getAnnotation(
                                        ViewInterface.class);
                        if (viewInterface.value().isAssignableFrom(
                                bean.getBeanClass())) {
                            removablePresenterBean = presenterBean;
                            break;
                        }
                    }
                }
                if (removablePresenterBean != null) {
                    dereferenceBeanInstance(removablePresenterBean);
                }
            }
        }

        class ContextualInstance<T> {
            private final T instance;
            private final CreationalContext<T> creationalContext;

            public ContextualInstance(final T instance,
                    final CreationalContext<T> creationalContext) {
                super();
                this.instance = instance;
                this.creationalContext = creationalContext;
            }

            public T getInstance() {
                return instance;
            }

            public CreationalContext<T> getCreationalContext() {
                return creationalContext;
            }

        }
    }

    /**
     * Annotation used for declaring bean class scope for VaadinScoped beans
     * 
     * @author Tomi Virkki / Vaadin Ltd
     */
    @Scope
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    public @interface VaadinScoped {
        VaadinScope value() default VaadinScope.WINDOW;

        public enum VaadinScope {
            APPLICATION, WINDOW
        }
    }

}
