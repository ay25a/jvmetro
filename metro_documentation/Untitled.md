# 1. Keep Route exactly as it is

Suppose your administrator creates these routes:

```text
R1: A ─── B
R2: B ─── C
R3: C ─── D
R4: D ─── E
R5: B ─── F
```

Every route still has exactly two stations:

```java
class Route {
    private Station stationA;
    private Station stationB;
    private double distance;
}
```

That's completely fine.

The passenger, however, could ask:

> "I want to go from A to E."

There isn't a route:

```text
A → E
```

But there **is a journey**:

```text
A → B → C → D → E
```

So the application should find that path.

---

# 2. Route and Journey should be separate concepts

This is the conceptual change I'd make.

### Route

A physical/direct connection:

```text
Route R1
A ↔ B
Distance: 2 km
```

### Journey/Path

A collection of routes:

```text
A
 ↓ R1
B
 ↓ R2
C
 ↓ R3
D
 ↓ R4
E
```

Therefore:

```text
RouteManager
    ↓
findJourney(A, E)
    ↓
[Route R1, Route R2, Route R3, Route R4]
```

You don't necessarily even need a permanent `Journey` class. You could simply return a list:

```java
List<Route> findRoute(Station source, Station destination)
```

Although, personally, I'd call it:

```java
List<Station> findPath(...)
```

or create a `Journey` class if you want to display useful information.

---

# 3. This is basically a graph

Your metro network naturally becomes a graph:

```text
       B
      / \
     A   F
      \   \
       C   ?
       |
       D
       |
       E
```

Stations are **nodes**.

Routes are **edges**.

Then route finding becomes a graph traversal problem.

You can use:

- BFS — easiest if you want the fewest station connections
    
- DFS — simple but doesn't necessarily find the best route
    
- Dijkstra — useful if you want the shortest-distance route
    

For your assignment, **BFS is probably enough initially**.

If you want the "shortest distance" route, use Dijkstra.

---

# 4. This also makes your fare calculation better

This is where the new design becomes really useful.

Suppose:

```text
A → B = 2 km
B → C = 3 km
C → D = 4 km
```

The journey is:

```text
A → B → C → D
```

Total distance:

```text
2 + 3 + 4 = 9 km
```

Then:

```text
FareCalculator
        ↓
Total journey distance = 9 km
        ↓
Fare = RM X
```

So your ticket doesn't need to store every route necessarily.

It can store:

```text
sourceStation
destinationStation
fare
```

while the system finds the journey when purchasing the ticket.

---

# 5. What should happen when a passenger buys a ticket?

I'd make the interaction something like this:

```text
================================
        BUY TICKET
================================

Source station:
> A

Destination station:
> E

Ticket type:
1. Single
2. Daily
3. Monthly
> 1

Searching for route...

Route found:
A → B → C → D → E

Total distance: 11.4 km
Fare: RM 4.50

Your balance: RM 20.00

Confirm purchase?
1. Yes
2. No
```

Then:

```text
Ticket purchased successfully.

Ticket ID: T1024
Status: ACTIVE
Valid from: 02/09/2026 19:45
Valid until: 02/09/2026 23:59
Remaining balance: RM 15.50
```

Now you have a **real business process**, not just CRUD.

---

# 6. About "using" the ticket — you're absolutely right

You shouldn't make the passenger sit at the CLI and say:

```text
> Use Ticket
```

That's not how a real metro works.

Physically, the passenger:

```text
Passenger
   ↓
approaches gate
   ↓
scans ticket/card
   ↓
metro system validates ticket
   ↓
gate opens
```

Your CLI application is merely **simulating the gate**.

So I'd rename the operation conceptually from:

> "Passenger uses ticket"

to:

> **Validate Ticket / Scan Ticket**

That feels much more realistic.

---

# 7. Simulating the metro gate

You could have a CLI option:

```text
================================
       METRO GATE
================================

Enter ticket ID:
> T1024

Scanning ticket...

✓ Ticket exists
✓ Ticket belongs to passenger
✓ Ticket is active
✓ Ticket has not expired

ACCESS GRANTED

Entering station: A
```

Then the system records the entry.

You could add:

```text
Ticket
    entryStation
    entryTime
```

For a single ticket:

```text
ACTIVE
   ↓
Passenger enters
   ↓
IN_USE
   ↓
Passenger exits
   ↓
USED
```

But here's where I'd be careful.

---

# 8. You don't actually need `IN_USE`

Unless your lecturer specifically wants it, I'd keep the status model simple:

```text
ACTIVE
CANCELLED
USED
EXPIRED
```

Then scanning can simply mean:

### At entry

```text
ACTIVE
```

→ allow entry.

Record:

```text
entryStation
entryTime
```

### At exit

Check:

```text
ACTIVE
```

→ validate destination.

Then:

```text
status = USED
```

So the system simulates the physical process without pretending that the passenger literally clicks "use ticket."

---

# 9. For daily/monthly tickets

This gets even more interesting.

Suppose:

```text
DAILY
ACTIVE
```

Passenger scans:

```text
Balakong → Entry
```

Ticket remains:

```text
ACTIVE
```

Later:

```text
Cheras → Exit
```

Still:

```text
ACTIVE
```

because the daily ticket can be reused.

The system simply records the usage.

For example:

```text
TicketUsage
-----------------------
ticket
entryStation
exitStation
entryTime
exitTime
```

You **don't necessarily need this class**, though.

You could put:

```java
List<TicketUsage> usageHistory;
```

inside the ticket if you want to demonstrate a more sophisticated design.

But again, don't over-engineer this.

---

# 10. Now, your CLI concern

This is where I would make a significant change.

You said:

> I only have a user session, and user session contains every command line interface page.

I'd avoid that.

Instead, think of your CLI as a **navigation system**.

Something like:

```text
Application
    ↓
LoginPage
    ↓
UserSession
    ↓
MainMenuPage
       ↙        ↘
Passenger      Admin
Menu           Menu
 ↓              ↓
Ticket         Station
Route          Route
Balance        Train
...
```

The session should **not own every page**.

---

# 11. What should UserSession actually contain?

Very little.

Something like:

```java
class UserSession {

    private User currentUser;
    private boolean loggedIn;

    public void login(User user) { ... }

    public void logout() { ... }

    public User getCurrentUser() { ... }

    public boolean isLoggedIn() { ... }
}
```

That's basically it.

Its job is:

> "Who is currently logged in?"

Not:

> "Here are 47 pages and 138 commands."

---

# 12. Introduce a `Page` or `Screen` interface

This is probably the biggest improvement I'd make to your CLI architecture.

```java
public interface Page {
    void display();
}
```

Or:

```java
public interface Page {
    Page run();
}
```

I actually prefer the second approach for a CLI application.

For example:

```java
interface Page {
    Page run();
}
```

Then:

```text
LoginPage
    ↓
MainMenuPage
    ↓
PassengerMenuPage
    ↓
BuyTicketPage
    ↓
TicketConfirmationPage
```

Each page only knows how to perform **its own interaction**.

---

# 13. Your application can have a page loop

Your main application becomes conceptually:

```java
Page currentPage = new LoginPage(...);

while (currentPage != null) {
    currentPage = currentPage.run();
}
```

That's extremely clean.

A page displays itself and decides where the user goes next.

For example:

```text
LoginPage
    |
    | successful login
    ↓
PassengerMenuPage
```

Then:

```text
PassengerMenuPage
    |
    ├── Buy Ticket → BuyTicketPage
    |
    ├── View Tickets → TicketListPage
    |
    ├── Balance → BalancePage
    |
    └── Logout → LoginPage
```

---

# 14. But don't make a page for everything

This is exactly where your instinct is correct.

You said:

> I'm not making every page for every service.

**Don't.**

A page should represent a meaningful user interaction, not every method in a service.

For example, don't do:

```text
AddStationPage
UpdateStationNamePage
UpdateStationLocationPage
UpdateStationStatusPage
DeleteStationPage
```

That's ridiculous for a CLI.

Instead:

```text
StationManagementPage
```

could provide:

```text
================================
      STATION MANAGEMENT
================================

1. List stations
2. Add station
3. Update station
4. Remove station
5. Back
```

Then the page calls the `StationManager`.

---

# 15. Same thing for trains

```text
TrainManagementPage
```

```text
================================
       TRAIN MANAGEMENT
================================

1. List trains
2. Add train
3. Update train
4. Remove train
5. Assign train to route
6. Back
```

You don't need five separate pages.

---

# 16. Same thing for routes

```text
RouteManagementPage
```

```text
================================
       ROUTE MANAGEMENT
================================

1. List routes
2. Add route
3. Update route
4. Remove route
5. Find journey
6. Back
```

Notice that:

**The page handles user interaction.**

**The manager handles business logic.**

That separation is important.

---

# 17. Your layers become very clean

I'd structure your Java project roughly like:

```text
src/
│
├── model/
│   ├── User.java
│   ├── Passenger.java
│   ├── Admin.java
│   ├── Station.java
│   ├── Route.java
│   ├── Train.java
│   ├── Ticket.java
│   └── ...
│
├── service/
│   ├── UserManager.java
│   ├── StationManager.java
│   ├── RouteManager.java
│   ├── TrainManager.java
│   ├── TicketManager.java
│   └── FareCalculator.java
│
├── ui/
│   ├── Page.java
│   ├── LoginPage.java
│   ├── PassengerMenuPage.java
│   ├── AdminMenuPage.java
│   ├── TicketPage.java
│   ├── StationManagementPage.java
│   ├── RouteManagementPage.java
│   ├── TrainManagementPage.java
│   └── ...
│
├── persistence/
│   └── FileManager.java
│
└── Main.java
```

That's a very understandable architecture for a lecturer to inspect.

---

# 18. And here's the really important separation

Imagine the passenger clicks:

```text
Buy Ticket
```

The **UI** shouldn't calculate the fare.

Bad:

```java
// BuyTicketPage
double fare = distance * 0.5;
```

Instead:

```text
BuyTicketPage
      ↓
TicketManager
      ↓
RouteManager
      ↓
findPath()
      ↓
FareCalculator
      ↓
TicketManager
      ↓
Ticket
```

The UI only gathers information:

```text
source
destination
ticket type
```

and sends it to the service.

---

# 19. Same thing for Admin

Admin enters:

```text
Station name
Location
```

The UI calls:

```java
stationManager.addStation(...)
```

The manager validates:

```text
Is name already used?
Is location valid?
```

Then the manager creates the Station.

Then FileManager persists it.

So:

```text
StationManagementPage
          ↓
   StationManager
          ↓
       Station
          ↓
     FileManager
```

Not:

```text
StationManagementPage
       ↓
directly modifies files
```

---

# 20. You can even make a tiny `Console` helper

CLI code tends to become ugly because you repeatedly write:

```java
Scanner scanner = new Scanner(System.in);
System.out.println(...);
scanner.nextLine();
Integer.parseInt(...);
```

I'd create something like:

```java
ConsoleInput
```

with methods:

```java
readString()
readInt()
readDouble()
readChoice()
```

Then your pages become much cleaner:

```java
String name = console.readString("Station name: ");
String location = console.readString("Location: ");
```

This isn't really a business class. It's just a UI utility.

---

# 21. Your final navigation could be very small

You actually don't need dozens of pages.

I'd start with roughly this:

```text
                    LoginPage
                        │
             ┌──────────┴──────────┐
             ↓                     ↓
    PassengerMenuPage         AdminMenuPage
             │                     │
     ┌───────┼────────┐      ┌─────┼─────┐
     ↓       ↓        ↓      ↓     ↓     ↓
 BuyTicket  Tickets  Balance Stations Routes Trains
     │       │               │     │      │
     └───────┴───────────────┴─────┴──────┘
                     │
                  Back/Menu
```

And management pages can themselves contain menus.

You don't need a page for every tiny operation.

---

# 22. One thing I'd change from my previous recommendation

Earlier I suggested a `MetroSystem` that contains all the managers. That's still fine, but **don't let `MetroSystem` become another giant god object**.

I'd use it primarily as a dependency container/coordinator:

```text
MetroSystem
│
├── UserManager
├── StationManager
├── RouteManager
├── TrainManager
├── TicketManager
└── FileManager
```

Then pages receive only what they need.

For example:

```java
new StationManagementPage(stationManager)
```

rather than:

```java
new StationManagementPage(metroSystem)
```

This is a small distinction, but it keeps your design cleaner.

---

# 23. The entire application now tells one coherent story

You can now explain the application to your lecturer in one flow:

```text
ADMIN
 │
 ├── Creates stations
 │
 ├── Connects stations using routes
 │
 └── Assigns trains to routes
 │
 ▼
METRO NETWORK
 │
 ▼
PASSENGER
 │
 ├── Selects source
 ├── Selects destination
 └── Selects ticket type
 │
 ▼
ROUTE MANAGER
 │
 └── Finds a path through multiple routes
 │
 ▼
FARE CALCULATOR
 │
 └── Calculates journey fare
 │
 ▼
TICKET MANAGER
 │
 ├── Checks passenger balance
 ├── Creates ticket
 └── Saves ticket
 │
 ▼
TICKET
 │
 ├── ACTIVE
 ├── CANCELLED
 ├── USED
 └── EXPIRED
 │
 ▼
METRO GATE SIMULATION
 │
 └── Scans/validates ticket
```

And the CLI sits **around** this system rather than being the system itself.

That's the key architectural improvement.

### If I were implementing this assignment

I'd make your next concrete step:

1. **Finalize the entity relationships** (`Passenger`, `Ticket`, `Station`, `Route`, `Train`).
    
2. Define the **ticket lifecycle and date rules** precisely.
    
3. Design `RouteManager.findPath()` using a graph.
    
4. Define the manager/service responsibilities.
    
5. Build the small `Page` interface and navigation system.
    
6. Only then build the actual CLI screens.
    

That order prevents the CLI from dictating your entire architecture. The business logic becomes the core, and the CLI becomes a relatively thin layer on top of it.