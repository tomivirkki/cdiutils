package org.vaadin.virkki.cdiutils;

/**
 * Exception class for CDI Utils related exceptions
 * 
 * @author Tomi Virkki / Vaadin Ltd
 */
@SuppressWarnings("serial")
public class CdiUtilsException extends RuntimeException {
    public CdiUtilsException(final String cause, final Throwable throwable) {
        super(cause, throwable);
    }

    public CdiUtilsException(final String cause) {
        super(cause);
    }
}
