package org.vaadin.virkki.cdiutils.mvp;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ObserverMethod;

import org.vaadin.virkki.cdiutils.mvp.AbstractPresenter.ViewInterface;
import org.vaadin.virkki.cdiutils.mvp.CDIEvent.CDIEventImpl;

/**
 * CDI extension needed by CDI Utils MVP-pattern.
 * 
 * @author Tomi Virkki / Vaadin Ltd
 */
@SuppressWarnings("serial")
public class MvpExtension implements Extension, Serializable {
    /**
     * Adds a View open observer method for each bean extending
     * AbstractPresenter.
     * 
     * @param afterBeanDiscovery
     * @param beanManager
     */
    void afterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscovery,
            final BeanManager beanManager) {

        Iterator<Bean<?>> beanIterator = beanManager.getBeans(
                AbstractPresenter.class).iterator();
        while (beanIterator.hasNext()) {
            final Bean<?> bean = beanIterator.next();
            afterBeanDiscovery
                    .addObserverMethod(new ObserverMethod<ParameterDTO>() {
                        @Override
                        public Class<?> getBeanClass() {
                            return bean.getBeanClass();
                        }

                        @Override
                        public Set<Annotation> getObservedQualifiers() {
                            Set<Annotation> qualifiers = new HashSet<Annotation>();
                            Class<? extends View> viewInterface = getBeanClass()
                                    .getAnnotation(ViewInterface.class).value();
                            qualifiers.add(new CDIEventImpl(viewInterface
                                    .getName() + AbstractPresenter.VIEW_OPEN));
                            return qualifiers;
                        }

                        @Override
                        public Type getObservedType() {
                            return ParameterDTO.class;
                        }

                        @Override
                        public Reception getReception() {
                            return Reception.ALWAYS;
                        }

                        @Override
                        public TransactionPhase getTransactionPhase() {
                            return TransactionPhase.IN_PROGRESS;
                        }

                        @SuppressWarnings("rawtypes")
                        @Override
                        public void notify(final ParameterDTO event) {
                            Object presenter = beanManager.getReference(bean,
                                    getBeanClass(),
                                    beanManager.createCreationalContext(bean));
                            ((AbstractPresenter) presenter).viewOpened();
                        }
                    });
        }
    }
}
