package org.vaadin.virkki.cdiutils.componentproducers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Component;

/**
 * Qualifier that can be used for declaratively defining Vaadin components at
 * CDI injection points.
 * 
 * @author Tomi Virkki / Vaadin Ltd
 */
@Qualifier
@Target({ ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Preconfigured {
	/**
	 * A key used for obtaining (localized) texts from TextBundle (assuming an
	 * implementation of it is found). The acquired text is set as the
	 * Component's caption.
	 */
	@Nonbinding
	String captionKey() default "";

	/**
	 * A key used for obtaining (localized) texts from TextBundle (assuming an
	 * implementation of it is found). The acquired text is set as the Label's
	 * value.
	 */
	@Nonbinding
	String labelValueKey() default "";

	@Nonbinding
	boolean immediate() default false;

	@Nonbinding
	boolean nullSelectionAllowed() default true;

	@Nonbinding
	String styleName() default "";

	@Nonbinding
	boolean spacing() default false;

	@Nonbinding
	boolean margin() default false;

	@Nonbinding
	boolean sizeFull() default false;

	@Nonbinding
	float height() default -1.0f;

	@Nonbinding
	int heightUnits() default Sizeable.UNITS_PIXELS;

	@Nonbinding
	float width() default -1.0f;

	@Nonbinding
	int widthUnits() default Sizeable.UNITS_PIXELS;

	@Nonbinding
	Class<? extends Component> implementation() default Component.class;

	@Nonbinding
	boolean readOnly() default false;

	@Nonbinding
	boolean enabled() default true;

	@Nonbinding
	boolean visible() default true;

	@Nonbinding
	String caption() default "";

	@Nonbinding
	String debugId() default "";

	@Nonbinding
	boolean sizeUndefined() default false;

	@Nonbinding
	String description() default "";

	@Nonbinding
	boolean required() default false;

	@Nonbinding
	String requiredError() default "";

	@Nonbinding
	boolean invalidAllowed() default true;

	@Nonbinding
	boolean invalidCommitted() default false;

	@Nonbinding
	boolean readTrough() default true;

	@Nonbinding
	boolean writeTrough() default true;

	@Nonbinding
	boolean validationVisible() default true;

	@Nonbinding
	int tabIndex() default -1;

	@Nonbinding
	boolean multiSelect() default false;

	@Nonbinding
	boolean newItemsAllowed() default false;

	@Nonbinding
	int itemCaptionMode() default -1;
}
