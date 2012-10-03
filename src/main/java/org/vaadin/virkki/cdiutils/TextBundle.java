package org.vaadin.virkki.cdiutils;

/**
 * Interface for a bundle implementation used for obtaining (localized) texts.
 * If implemented, can be used by CDI Utils.
 * 
 * @author Tomi Virkki / Vaadin Ltd
 */
public interface TextBundle {
    String getText(String key, Object... params);
}
