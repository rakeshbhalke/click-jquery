The Click-jQuery project provides tight integration between Click and the [jQuery](http://jquery.com/) JavaScript library.

Click-JQuery support centers around the jQuery [Taconite plugin](http://malsup.com/jquery/taconite/). Taconite is a plugin that allows you to make multiple DOM updates with a single Ajax request. With Taconite you specify a list of commands to be executed as the result of an Ajax request.

Taconite commands are built as a single XML document, which is returned to the browser where the commands are executed one by one.

The jQuery-Click subproject provides the [Taconite](http://clickclick.googlecode.com/svn/trunk/site/javadoc/jquery-api/net/sf/clickclick/jquery/util/Taconite.html) Java class to wrap the Taconite plugin so you don't have to write any XML code yourself.

To see this in action here is an example:

```
public class MyPage extends Page {

    // Create a new jQuery ActionLink control called "link"
    private ActionLink link = new ActionLink("link");

    public void onInit() {
        addControl(link);

        link.addBehavior(new MyBehavior());
    }

    class ClickBehavior extends DefaultAjaxBehavior {

        // When the link is clicked, this method is invoked that returns a Taconite
        // instance
        public ActionResult onAction(Control source) {

            // Create a new Taconite instance
            Taconite result = new Taconite();

            // Insert a Table after the link
            result.after(link, new Table("table"));

            return result;
        }
    }
}
```

The Taconite instance above will render the following XML document:
```
<taconite>
    <after select="#link">
        <table name="table" id="table">
        </table>
    </after> 
</taconite> 
```

Here is another example showing that multiple commands can be executed:

```
public class MyPage extends Page {

    // Create a new ActionLink control called "link"
    private ActionLink link = new ActionLink("link");

    public void onInit() {
        addControl(link);

        link.addBehavior(new MyBehavior());
    }

    class ClickBehavior extends DefaultAjaxBehavior {

        // When the link is clicked, this method is invoked that returns a Taconite
        // instance
        public ActionResult onAction(Control source) {

            // Create a new Taconite partial instance
            Taconite result = new Taconite();

            Table table = new Table("table");

            // Insert a Table after the link
            result.after(link, table);

            Span span = new Span();
            span.setText("Hello World!");

            // Insert a Span element before the table
            result.before(table, span);

            return result;
        }
    }
}
```

The Taconite instance above will render the following XML document:
```
<taconite>
    <after select="#link">
        <table name="table" id="table">
        </table>
    </after>

    <before select="#table">
        <span>Hello World!</span>
    </before>
</taconite>
```

If you are familiar with jQuery you might have realized that Taconite XML commands map directly to [jQuery methods](http://docs.jquery.com/Manipulation). This gives you alot of control over code that is executed on the client-side. Please see the [Taconite Command](http://malsup.com/jquery/taconite/#commands) section for details.

### Features ###

Here are some of the features provided by Click-jQuery:

  * Ajax actions can return any Click control to the browser. For example, one can submit a Form using Ajax that returns a new Form instance
  * When Ajax actions return a Click Control, the Control's CSS and JavaScript resources are automatically added to the HTML page HEAD element.
  * If CSS and JavaScript resources are already available to the browser, they will be ignored. For example if an Ajax action returns a Form, the Form's resources (control.js, control.css) will also be sent to the browser and added to the browser's HEAD element. If the Ajax action is invoked a second time, Click will detect that the resources (control.js and control.css) are already available to the browser and won't add them to the HEAD element again.

### Infrastructure ###

One of the main pieces is the JavaScript file [jquery.click.js](http://code.google.com/p/clickclick/source/browse/trunk/clickclick/jquery/src/META-INF/web/clickclick/jquery/jquery.click.js). This script combines the plugins [jQuery Taconite](http://malsup.com/jquery/taconite/), and custom, Click specific code to handle some of the advanced features necessary to seamlessly support Ajax in Click applications.

### Ajax Integration ###

Click-jQuery (CjQ) takes the following approach at integrating Click and Ajax. First a Velocity template (which can be customized) is created that provides the jQuery code needed to make the Ajax call. As the template is Velocity based, we can easily create generic templates and pass in dynamic variables at runtime.

On the Java side a Click control could be used to associate with the template. However this means that each control will have to define the same variables to pass to the template. Ideally we would like to define a template and an independent Java class that can be reused in both Click Pages or custom Click Controls.

Such a class is called a _Helper_ class. Each helper class is associated with a Velocity template and exposes methods to interact with the template. Helpers normally target Click Controls to _ajaxify_ or enhance them with extra behaviors and features. This sounds very sophisticated but is fairly simple and is explained in more detail below.

Using a Helper object in our Click Pages is useful but since Click is a component oriented framework, it makes sense to provide Ajax aware controls to make it easier to work with. With the template and helper in place its very easy to create an Ajax aware control. In most cases this means simply extending an existing control we want to ajaxify, and in the Controls _onInit_ event handler, instantiate and setup the Helper class.

Below we will discuss in detail how CjQ integrates with jQuery.

### jQuery Templates ###

First off we create our template which contains the jQuery code. In this example we want to register a _click_ listener on a specific HTML element:

_hello.js_

```
$('$selector').click(function () {
    alert('Hello World!');
});
```

jQuery makes this very easy, however take note that we do not hard code the HTML element, instead we use a Velocity variable, _$selector_, and at runtime we specify which HTML element we want to target.

### jQuery Helper ###

Next we create an associated Helper class. CjQ already provides a couple of Helpers including the JQHelper class. Since JQHelper already provides most of the code we need, we will extend it here:

_HelloHelper.java_
```
public class HelloHelper extends JQHelper {

    // JQHelper always operates on a target Control
    public HelloHelper(Control target) {
        super(target);

        // We set the associated template to the location of our hello.js template
        setTemplate("/templates/hello.js");
    }
}
```

Notice the target Control passed into the HelloHelper constructor. This Control's ID attribute is automatically passed to the Velocity template under the variable _$selector_ which is what we defined in our template. If you do not want the Control to be the target of our script, you can specify a different selector using the helper method, [setSelector(String)].

### Example usage ###

To use our new template and helper we can invoke them in our Page as follows:

```
public class ClientPage extends Page {

    private Form form = new Form("form");

    public void onInit() {
        addControl(form);

        Button button = new Button("button");
        form.add(button);

        HelloHelper helper = new HelloHelper(button);

        // Once the helper is setup, we invoke the ajaxify method (exposed by JQHelper)
        // which adds the hello.js script to the button's list of JavaScript elements.
        helper.ajaxify();
    }
}
```

The _ajaxify_ method is very important and performs the following:

  1. It registers the Button as an Ajax control on the [AjaxControlRegistry](AjaxControlRegistry.md)
  1. It adds all the Helper's HEAD elements (hello.js) to the Control's head elements. When the button is rendered by the Page, the hello.js script is also rendered

### Result ###

The following HTML will be rendered:

```
<html>
  <head>

    <script type="text/javascript">
    $('#form_button').click(function () {
      alert('Hello World!');
    });
    </script>

  </head>

  <body>

    <form id="form" name="form" action="client.htm">

      <input id="form_button" type="button"/>

    </form>

  </body>
</html>
```

Notice how the _$selector_ variable was replaced by the ID attribute of the button, _#form\_button_.

This is all great but what if you want to reuse this button in other pages. You would have to create the Button, create the HelloHelper and remember to call _helper.ajaxify_.

To maximize reuse you could create a custom Button control that can _ajaxify_ itself, without you having to worry about it.

### jQuery aware Controls ###

We already have the template and Helper, so all we need to do to create a Hello World Button is to subclass Button, and setup the Helper in its _onInit_ event handler.

```
public class HelloButton extends Button {

    // Create the Helper class
    private HelloHelper helper = new HelloHelper(this);

    public HelloButton(String name) {
        super(name);
    }

    public void onInit() {
        // Ajaxify the button by adding the hello.js to the Button's head elements
        helper.ajaxify();
    }
}
```

Now we can simply use our HelloButton in the Page:

```
public class ClientPage extends Page {

    private Form form = new Form("form");

    public void onInit() {
        addControl(form);

        Button button = new HelloButton("button");
        form.add(button);
    }
}
```