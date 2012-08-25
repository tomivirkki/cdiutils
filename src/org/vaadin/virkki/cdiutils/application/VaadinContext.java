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

import org.vaadin.virkki.cdiutils.mvp.AbstractPresenter;
import org.vaadin.virkki.cdiutils.mvp.AbstractPresenter.ViewInterface;
import org.vaadin.virkki.cdiutils.mvp.AbstractView;

import com.vaadin.ui.Root;

/**
 * CDI Extension which registers VaadinContextImpl context.
 * 
 * @author Tomi Virkki / Vaadin Ltd
 */
public class VaadinContext implements Extension {

    void afterBeanDiscovery(
            @Observes final AfterBeanDiscovery afterBeanDiscovery,
            final BeanManager beanManager) {
        afterBeanDiscovery.addContext(new VaadinContextImpl());
    }

    /**
     * Custom CDI context for Vaadin applications. Stores references to bean
     * instances in the scope of Vaadin Root.
     * 
     * @author Tomi Virkki / Vaadin Ltd
     */
    private static class VaadinContextImpl implements Context {

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

            if (Root.getCurrent() == null && creationalContext != null) {
                // Instantiating the root
                final AbstractCdiRoot root = (AbstractCdiRoot) bean
                        .create(creationalContext);
                final RootBeanStore beanStore = root.getBeanStore();
                beanStore.addBeanInstance(bean,
                        beanStore.new ContextualInstance<T>((T) root,
                                creationalContext));
                Root.setCurrent(root);
            }

            T instance = null;
            if (Root.getCurrent() != null) {
                final RootBeanStore beanStore = ((AbstractCdiRoot) Root
                        .getCurrent()).getBeanStore();
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
     * Datastructure for storing bean instances in {@link VaadinScope} context.
     * 
     * @author Tomi Virkki / Vaadin Ltd
     */
    static class RootBeanStore {
        private final Map<Bean<?>, ContextualInstance<?>> instances = new HashMap<Bean<?>, ContextualInstance<?>>();

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
            T instance = null;
            if (contextualInstance != null) {
                instance = contextualInstance.getInstance();
            }
            return instance;
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
    // TODO: NormalScope
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    public @interface VaadinScoped {
    }

}
