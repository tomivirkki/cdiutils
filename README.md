---------------------------
**THIS PROJECT IS DISCONTINUED!!!!**

I've decided to discontinue CDI Utils as such. 
The custom CDI scope part is nowadays provided by Vaadin under a complementary license 
so there's no longer need for a third party implementation.
The remaining utilities are separated as their own projects, namely:
CDI Properties (https://github.com/tomivirkki/cdi-properties.git)
and CDI MVP (https://github.com/tomivirkki/cdi-mvp.git)

---------------------------
[![Published on Vaadin  Directory](https://img.shields.io/badge/Vaadin%20Directory-published-00b4f0.svg)](https://vaadin.com/directory/component/cdi-utils)
[![Stars on Vaadin Directory](https://img.shields.io/vaadin-directory/star/cdi-utils.svg)](https://vaadin.com/directory/component/cdi-utils)

# CDI Utils

CDI Utils is an add-on for Vaadin framework aiming at simplifying
application development with Vaadin and CDI. Here are the utilities it provides:

1. @UIScoped
Custom CDI scope designed especially for vaadin applications. @UIScoped beans are
bound to a context defined by Vaadin UIs.


2. Lightweight MVP framework
    1. Extend View interface for your view
    2. Create view instance that extends AbstractView and implements your View extension

    2. Extend AbstractPresenter and annotate it with @ViewInterface(YourViewInterface.class)
       That's it.
        - The correct view instance is then automatically injected to your presenter (Your view control logic can then reside in the presenter)
        - The view should use CDI's built-in event bus to fire events that are observed by the presenter. The add-on provides a utility for this: fireViewEvent(ParameterDTO). The ParameterDTO can be used to transfer any data to the presenter (CDI Event observers only accept one parameter)
        - Call yourViewImplementationInstance.openView() each time the view is accessed (this will eventually invoke yourPresenterInstance.viewOpened())

3. Producers for declaratively defined Vaadin components (@Preconfigured -annotation). Inject preconfigured Vaadin Components to your views. For example:
```
@Preconfigured(captionKey="btnUpdate", styleName=Button.STYLE_LINK, enabled=false)
private Button button;
```
