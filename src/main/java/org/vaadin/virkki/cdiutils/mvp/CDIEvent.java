package org.vaadin.virkki.cdiutils.mvp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;

/**
 * Qualifier for view events observed by presenters.
 * 
 * @author Tomi Virkki / Vaadin Ltd
 */
@Qualifier
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface CDIEvent {
    String value();

    @SuppressWarnings("serial")
    public static class CDIEventImpl extends AnnotationLiteral<CDIEvent>
            implements CDIEvent {
        private final String value;

        public CDIEventImpl(final String value) {
            super();
            this.value = value;
        }

        @Override
        public final String value() {
            return value;
        }
    }
}