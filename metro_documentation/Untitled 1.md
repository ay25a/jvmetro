Yes — I think you're converging on a **good abstraction**, and I would actually simplify it even further.

What you're describing is essentially a small **navigation framework/state machine for CLI applications**. The important realization is:

> A "page" doesn't necessarily need to be a class. A page is primarily **something that can be entered, rendered, and produce a navigation result**.

Once you think about it that way, you can avoid a huge inheritance hierarchy.

### The architecture I'd aim for

Think in terms of four concepts:

```text
Page
 └── renders itself / handles interaction
      ↓
NavigationResult
      ↓
PageController
 └── owns the navigation stack
      ↓
Terminal/UI
```

For example:

```java
public interface Page {
    PageResult show();
}
```

And:

```java
public sealed interface PageResult
        permits PageResult.Next, PageResult.Back, PageResult.Exit {

    record Next(Page page) implements PageResult {}
    record Back(String message) implements PageResult {}
    record Exit() implements PageResult {}
}
```

Then your controller becomes extremely boring—which is **exactly what you want**.

```java
public class PageController {

    private final Deque<Page> stack = new ArrayDeque<>();

    public void start(Page initialPage) {
        stack.push(initialPage);

        while (!stack.isEmpty()) {
            Page current = stack.peek();

            PageResult result = current.show();

            switch (result) {
                case PageResult.Next next -> stack.push(next.page());

                case PageResult.Back back -> {
                    stack.pop();

                    if (back.message() != null) {
                        System.out.println(back.message());
                    }
                }

                case PageResult.Exit ignored -> {
                    stack.clear();
                }
            }
        }
    }
}
```

Now a page can literally be a lambda:

```java
Page mainMenu = () -> {
    System.out.println("=== Main Menu ===");
    System.out.println("1. Settings");
    System.out.println("2. Profile");
    System.out.println("0. Exit");

    switch (scanner.nextLine()) {
        case "1" -> { return new PageResult.Next(settingsPage); }
        case "2" -> { return new PageResult.Next(profilePage); }
        case "0" -> { return new PageResult.Exit(); }
        default -> { return new PageResult.Back(null); }
    }
};
```

Although I'd probably change the interface slightly so a page can distinguish **"stay on this page"** from **"go back"**.

---

## Don't make `Page` responsible for too much

This is where I think your original `showComponent()` → abstract `show()` idea could potentially become unnecessarily complicated.

You might be tempted to build:

```text
Page
 ├── showComponent()
 │    ├── show()
 │    ├── interrupt checking
 │    ├── cleanup
 │    ├── messages
 │    └── navigation
```

But eventually `Page` becomes a mini-framework that every page has to conform to.

Instead, I'd make the framework's responsibility **navigation**, not rendering.

A page should basically answer:

> "I have been entered. What happened?"

For example:

```java
public interface Page {
    PageResult run();
}
```

Then the controller handles the lifecycle.

---

# A particularly useful mechanic: `PageResult`

I think this is probably the biggest improvement you can make.

Instead of having pages directly manipulate the controller:

```java
controller.goBack();
controller.push(...);
controller.exit();
```

have them **return an intention**.

For example:

```java
public enum Navigation {
    STAY,
    BACK,
    EXIT
}
```

But you can go further:

```java
public sealed interface PageResult {

    record Stay() implements PageResult {}

    record Push(Page page) implements PageResult {}

    record Back() implements PageResult {}

    record BackWithMessage(String message) implements PageResult {}

    record Replace(Page page) implements PageResult {}

    record Exit() implements PageResult {}
}
```

Now your page code is declarative:

```java
return new PageResult.Push(settingsPage);
```

or:

```java
return new PageResult.BackWithMessage("Returned to main menu.");
```

or simply:

```java
return new PageResult.Back();
```

This solves your `null` problem nicely.

I would **not** use `null` to mean "no message."

Instead:

```java
record Back(Optional<String> message) implements PageResult {}
```

or, even cleaner, separate results:

```java
Back()
BackWithMessage(String message)
```

Because six months later:

```java
new Back(null)
```

is considerably less expressive than:

```java
new Back()
```

---

# You can also make pages incredibly lightweight

You don't need:

```java
class SettingsPage extends AbstractPage {
    @Override
    protected PageResult show() {
        ...
    }
}
```

unless the page is complicated.

You could have:

```java
Page settings = () -> {
    System.out.println("=== Settings ===");
    ...
};
```

And only promote it into a class when it becomes large.

That's a very healthy approach:

```text
small page
    ↓
lambda

medium page
    ↓
method

large page
    ↓
class
```

The framework doesn't care.

---

# Another mechanic I'd strongly recommend: Page context

Eventually you'll discover that pages need common things:

```java
Scanner
Printer
User/session
Configuration
Database/service
Terminal
```

Don't make every lambda capture 15 variables.

Give the page a context:

```java
public record PageContext(
        Scanner input,
        PrintStream output,
        AppState state
) {}
```

Then:

```java
@FunctionalInterface
public interface Page {
    PageResult run(PageContext context);
}
```

Now:

```java
Page settings = context -> {
    context.output().println("=== Settings ===");

    String input = context.input().nextLine();

    ...
};
```

This is a surprisingly powerful little abstraction.

Your controller owns the context:

```java
PageContext context = new PageContext(
    scanner,
    System.out,
    appState
);
```

and every page gets the same environment.

---

# You can introduce components without making them Pages

This is another distinction that could make your architecture much cleaner.

You mentioned `component`.

I'd keep **Page** and **Component** separate.

For example:

```text
Page
 ├── Header
 ├── Menu
 ├── Form
 ├── Table
 └── Footer
```

A component doesn't navigate.

A page does.

For example:

```java
Menu menu = new Menu(
    "Main Menu",
    List.of(
        new MenuItem("Profile", () -> new PageResult.Push(profile)),
        new MenuItem("Settings", () -> new PageResult.Push(settings)),
        new MenuItem("Exit", PageResult.Exit::new)
    )
);
```

Then the page becomes:

```java
Page main = context -> menu.show(context);
```

This lets you reuse UI without confusing **UI rendering** with **navigation**.

---

# You could even make a reusable MenuPage

CLI applications tend to have _a lot_ of menus.

So this:

```java
new MenuPage(
    "Main Menu",
    List.of(
        option("Profile", profilePage),
        option("Settings", settingsPage),
        option("Help", helpPage)
    )
)
```

could automatically provide:

```text
=== Main Menu ===

1. Profile
2. Settings
3. Help
4. Back
```

And the menu framework handles:

- input validation
    
- invalid input
    
- displaying choices
    
- selecting a page
    
- back
    
- exit
    
- prompts
    
- clearing screen, if desired
    

Then your actual application becomes mostly **declarations**.

That's where I think this framework becomes really worthwhile.

---

# Another important mechanic: `replace`

Don't limit navigation to push/pop.

Suppose you have:

```text
Login
  ↓
Main Menu
```

You may not want Login sitting underneath Main Menu.

You want:

```text
[Main Menu]
```

instead of:

```text
[Login]
[Main Menu]
```

So:

```java
PageResult.Replace(mainMenu)
```

is useful.

Your controller can do:

```java
case Replace replace -> {
    stack.pop();
    stack.push(replace.page());
}
```

Similarly, `push` means:

```text
A
↓
B
↓
C
```

while `back` means:

```text
A
↑
B
```

and `replace` means:

```text
A
→
B
```

---

# You can eventually have `popTo`

Another useful navigation primitive:

```java
PopTo(Page page)
```

or perhaps:

```java
popUntil(...)
```

For example:

```text
Main
 └─ Settings
     └─ Account
         └─ Change Password
```

After changing the password, you may want:

```text
Main
 └─ Settings
```

instead of popping one page at a time.

That's when navigation starts looking like a real state machine.

---

# Don't forget "stay"

This one sounds trivial, but it's important.

Invalid input shouldn't necessarily mean:

```text
Back
```

It should usually mean:

```java
return new PageResult.Stay();
```

So:

```text
Page
 ↓
user enters invalid input
 ↓
STAY
 ↓
same Page
```

Your controller doesn't need to know anything about it.

---

# A nice final architecture

I would probably end up with something like:

```text
cli/
│
├── navigation/
│   ├── Page.java
│   ├── PageResult.java
│   └── PageController.java
│
├── ui/
│   ├── Menu.java
│   ├── MenuItem.java
│   ├── Form.java
│   ├── Table.java
│   └── Prompt.java
│
├── context/
│   └── PageContext.java
│
└── pages/
    ├── MainMenu.java
    ├── Settings.java
    └── Profile.java
```

But don't interpret that as "you must create all these classes."

In fact, **I'd start with only three things**:

```text
Page
PageResult
PageController
```

Then wait until repetition appears.

If you repeatedly write menus, create `Menu`.

If you repeatedly write prompts, create `Prompt`.

If pages need common dependencies, create `PageContext`.

If you repeatedly need confirmation dialogs, create `ConfirmPage`.

**Don't design the entire framework upfront. Let the repetition tell you what the framework needs.**

---

## One conceptual change I'd make to your idea

You said:

> "the page itself is just created by, or in one file."

I'd go one step further:

**Don't think of pages as files. Think of them as values.**

A page can be:

```java
Page main = context -> ...
```

or:

```java
Page main = new MainMenuPage(...);
```

or:

```java
Page main = menuPage(...);
```

All three are the same thing to the controller.

That gives you a very nice property:

> **The navigation framework doesn't care how a page was implemented.**

And that's probably the most valuable part of the architecture you're discovering.

You aren't really building a "Page class hierarchy." You're building a **small navigation runtime**, with pages as interchangeable functions/state objects. That can be extremely clean for a CLI application.