### User
- Name, Email, Password, and Role.
- Email is the ID
### Station
* Name, Location, and Station Status
* Name is the ID
* Status: 
	* Open (can make ticket)
	* Closed/Maintenance (cannot make ticket) 

### Train
* ID, Capacity, Status, and Route
* Status: 
	* Active (can make ticket)
	* Out of Service/Maintenance (cannot make ticket)

### Route
- ID, Source, Destination, and Distance.

### Ticket
- ID, Passenger, Source, Destination, Fare, Type, Issue Date, Expiry Date, and Status.
- Type:
	- Single: one time use
	- Daily: every day subscribtion
	- Monthly: every month subscribtion
- Status:
	- Active: Can still use
	- Cancelled: Cancelled before Use
	- Used: Used and Finished