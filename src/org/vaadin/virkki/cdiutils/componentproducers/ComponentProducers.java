package org.vaadin.virkki.cdiutils.componentproducers;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.logging.Logger;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.vaadin.virkki.cdiutils.TextBundle;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.InlineDateField;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.LoginForm;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.Slider;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.Upload;
import com.vaadin.ui.UriFragmentUtility;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;

/**
 * Producer methods for
 * {@link org.vaadin.virkki.cdiutils.componentproducers.Preconfigured
 * Preconfigured} Vaadin components.
 * 
 * @author Tomi Virkki / Vaadin Ltd
 */
@SuppressWarnings("serial")
@SessionScoped
public class ComponentProducers implements Serializable {

    /**
     * Produces a logger
     * 
     * @param injectionPoint
     * @return
     */
    @Produces
    @Preconfigured
    public Logger createLogger(final InjectionPoint injectionPoint) {
        return Logger.getLogger(injectionPoint.getMember().getDeclaringClass()
                .getName());
    }

    @Inject
    private Instance<TextBundle> textBundle;

    /**
     * Applies @Preconfigured attributes to Vaadin Components.
     * 
     * @param c
     * @param injectionPoint
     * @return
     */
    @SuppressWarnings("unchecked")
    private <T extends Component> T configureComponent(final T c,
            final InjectionPoint injectionPoint) {
        T component = c;
        for (Annotation annotation : injectionPoint.getQualifiers()) {
            if (annotation instanceof Preconfigured) {
                final Preconfigured preconfigured = (Preconfigured) annotation;

                if (!preconfigured.implementation().equals(Component.class)) {
                    if (component.getClass().isAssignableFrom(
                            preconfigured.implementation())) {
                        try {
                            component = (T) preconfigured.implementation()
                                    .newInstance();
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
                configureComponentApi(component, preconfigured);

                if (component instanceof Field) {
                    configureFieldApi((Field) component, preconfigured);
                }
                if (component instanceof AbstractField) {
                    configureAbstractFieldApi((AbstractField) component,
                            preconfigured);
                }
                if (component instanceof AbstractComponent) {
                    ((AbstractComponent) component).setImmediate(preconfigured
                            .immediate());
                }
                if (component instanceof AbstractSelect) {
                    configureAbstractSelectApi((AbstractSelect) component,
                            preconfigured);
                }
                if (component instanceof AbstractLayout) {
                    ((AbstractLayout) component).setMargin(preconfigured
                            .margin());
                }
                if (component instanceof AbstractOrderedLayout) {
                    ((AbstractOrderedLayout) component)
                            .setSpacing(preconfigured.spacing());
                }
            }
        }
        return component;
    }

    private void configureAbstractSelectApi(
            final AbstractSelect abstractSelect,
            final Preconfigured preconfigured) {
        abstractSelect.setNullSelectionAllowed(preconfigured
                .nullSelectionAllowed());
        abstractSelect.setMultiSelect(preconfigured.multiSelect());
        abstractSelect.setNewItemsAllowed(preconfigured.newItemsAllowed());
        if (preconfigured.itemCaptionMode() > -1) {
            abstractSelect.setItemCaptionMode(preconfigured.itemCaptionMode());
        }

    }

    private void configureAbstractFieldApi(final AbstractField abstractField,
            final Preconfigured preconfigured) {
        if (!(abstractField instanceof Form)) {
            abstractField.setInvalidAllowed(preconfigured.invalidAllowed());
        }
        abstractField.setInvalidCommitted(preconfigured.invalidCommitted());
        abstractField.setReadThrough(preconfigured.readTrough());
        abstractField.setReadThrough(preconfigured.writeTrough());
        abstractField.setValidationVisible(preconfigured.validationVisible());
        if (preconfigured.tabIndex() > -1) {
            abstractField.setTabIndex(preconfigured.tabIndex());
        }
    }

    private void configureFieldApi(final Field field,
            final Preconfigured preconfigured) {
        final String description = preconfigured.description();
        if (!description.isEmpty()) {
            field.setDescription(description);
        }

        final String requiredError = preconfigured.requiredError();
        if (!requiredError.isEmpty()) {
            field.setRequiredError(requiredError);
        }

        field.setRequired(preconfigured.required());
    }

    private void configureComponentApi(final Component component,
            final Preconfigured preconfigured) {
        component.setEnabled(preconfigured.enabled());
        component.setVisible(preconfigured.visible());
        component.setReadOnly(preconfigured.readOnly());

        final String styleName = preconfigured.styleName();
        if (!styleName.isEmpty()) {
            component.addStyleName(styleName);
        }

        final String caption = preconfigured.caption();
        if (caption.isEmpty()) {
            final String captionKey = preconfigured.captionKey();
            if (!captionKey.isEmpty()) {
                try {
                    component.setCaption(textBundle.get().getText(captionKey));
                } catch (UnsatisfiedResolutionException e) {
                    component.setCaption("No TextBundle implementation found!");
                }
            }
        } else {
            component.setCaption(caption);
        }

        if (component instanceof Label) {
            String labelValueKey = preconfigured.labelValueKey();
            if (!labelValueKey.isEmpty()) {
                try {
                    ((Label) component).setValue(textBundle.get().getText(
                            labelValueKey));
                } catch (UnsatisfiedResolutionException e) {
                    component.setCaption("No TextBundle implementation found!");
                }
            }
        }

        String debugId = preconfigured.debugId();
        if (!debugId.isEmpty()) {
            component.setDebugId(debugId);
        }

        if (preconfigured.sizeFull()) {
            component.setSizeFull();
        } else if (preconfigured.sizeUndefined()) {
            component.setSizeUndefined();
        } else {
            float width = preconfigured.width();
            if (width > -1.0f) {
                int widthUnits = preconfigured.widthUnits();
                component.setWidth(width, widthUnits);
            }
            float height = preconfigured.height();
            if (height > -1.0f) {
                int heightUnits = preconfigured.heightUnits();
                component.setHeight(height, heightUnits);
            }
        }

    }

    // Generated producer methods for Vaadin components
    @Produces
    @Preconfigured
    public AbsoluteLayout createAbsoluteLayout(final InjectionPoint ip) {
        AbsoluteLayout component = new AbsoluteLayout();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public Button createButton(final InjectionPoint ip) {
        Button component = new Button();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public ComboBox createComboBox(final InjectionPoint ip) {
        ComboBox component = new ComboBox();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public CssLayout createCssLayout(final InjectionPoint ip) {
        CssLayout component = new CssLayout();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public Embedded createEmbedded(final InjectionPoint ip) {
        Embedded component = new Embedded();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public Form createForm(final InjectionPoint ip) {
        Form component = new Form();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public FormLayout createFormLayout(final InjectionPoint ip) {
        FormLayout component = new FormLayout();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public GridLayout createGridLayout(final InjectionPoint ip) {
        GridLayout component = new GridLayout();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public HorizontalLayout createHorizontalLayout(final InjectionPoint ip) {
        HorizontalLayout component = new HorizontalLayout();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public HorizontalSplitPanel createHorizontalSplitPanel(
            final InjectionPoint ip) {
        HorizontalSplitPanel component = new HorizontalSplitPanel();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public InlineDateField createInlineDateField(final InjectionPoint ip) {
        InlineDateField component = new InlineDateField();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public Label createLabel(final InjectionPoint ip) {
        Label component = new Label();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public Link createLink(final InjectionPoint ip) {
        Link component = new Link();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public ListSelect createListSelect(final InjectionPoint ip) {
        ListSelect component = new ListSelect();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public LoginForm createLoginForm(final InjectionPoint ip) {
        LoginForm component = new LoginForm();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public MenuBar createMenuBar(final InjectionPoint ip) {
        MenuBar component = new MenuBar();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public NativeSelect createNativeSelect(final InjectionPoint ip) {
        NativeSelect component = new NativeSelect();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public OptionGroup createOptionGroup(final InjectionPoint ip) {
        OptionGroup component = new OptionGroup();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public Panel createPanel(final InjectionPoint ip) {
        Panel component = new Panel();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public PasswordField createPasswordField(final InjectionPoint ip) {
        PasswordField component = new PasswordField();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public PopupDateField createPopupDateField(final InjectionPoint ip) {
        PopupDateField component = new PopupDateField();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public ProgressIndicator createProgressIndicator(final InjectionPoint ip) {
        ProgressIndicator component = new ProgressIndicator();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public RichTextArea createRichTextArea(final InjectionPoint ip) {
        RichTextArea component = new RichTextArea();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public Slider createSlider(final InjectionPoint ip) {
        Slider component = new Slider();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public Table createTable(final InjectionPoint ip) {
        Table component = new Table();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public TabSheet createTabSheet(final InjectionPoint ip) {
        TabSheet component = new TabSheet();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public TextArea createTextArea(final InjectionPoint ip) {
        TextArea component = new TextArea();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public TextField createTextField(final InjectionPoint ip) {
        TextField component = new TextField();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public Tree createTree(final InjectionPoint ip) {
        Tree component = new Tree();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public TwinColSelect createTwinColSelect(final InjectionPoint ip) {
        TwinColSelect component = new TwinColSelect();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public Upload createUpload(final InjectionPoint ip) {
        Upload component = new Upload();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public UriFragmentUtility createUriFragmentUtility(final InjectionPoint ip) {
        UriFragmentUtility component = new UriFragmentUtility();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public VerticalLayout createVerticalLayout(final InjectionPoint ip) {
        VerticalLayout component = new VerticalLayout();
        return configureComponent(component, ip);
    }

    @Produces
    @Preconfigured
    public VerticalSplitPanel createVerticalSplitPanel(final InjectionPoint ip) {
        VerticalSplitPanel component = new VerticalSplitPanel();
        return configureComponent(component, ip);
    }

}
