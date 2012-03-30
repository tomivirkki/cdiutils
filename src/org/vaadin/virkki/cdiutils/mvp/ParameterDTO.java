package org.vaadin.virkki.cdiutils.mvp;

/**
 * Parameter DTO for CDI Events (Observer methods only accept one event
 * parameter).
 * 
 * @author Tomi Virkki / Vaadin Ltd
 */
public class ParameterDTO {
    private final Object primaryParameter;

    private final Object[] secondaryParameters;

    public ParameterDTO(final Object primaryParameter,
            final Object... secondaryParameters) {
        super();
        this.primaryParameter = primaryParameter;
        this.secondaryParameters = secondaryParameters;
    }

    public Object getPrimaryParameter() {
        return primaryParameter;
    }

    public Object[] getSecondaryParameters() {
        return secondaryParameters;
    }

    @SuppressWarnings("unchecked")
    public <T extends Object> T getPrimaryParameter(final Class<T> clazz) {
        return (T) getPrimaryParameter();
    }

    @SuppressWarnings("unchecked")
    public <T extends Object> T getSecondaryParameter(final int index,
            final Class<T> clazz) {
        return (T) getSecondaryParameters()[index];
    }
}
