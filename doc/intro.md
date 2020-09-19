# Introduction to gandalf

TODO: write [great documentation](http://jacobian.org/writing/what-to-write/)
   
# Overview
Gandalf is a Clojure(script) library for Rapid Applictio Web Development. The library has a number of aims:

- Data Driven
- Allow for simple definition of basic CRUD patterns but enable more complex operations

Gandalf doesn't aim to solve all web development problems but tries to implement a number of common patterns to
get a solution up and running. 
# Links

- [Widgets](widgets.html)

# Assumptions
Gandalf makes a couple of "big" assumptions about the system you are deveoping:

- All resources have a unique `:id` key the value of which uniquely identifies the resource in the database.

TODO: Would it be possible to have a logical resource make up of data from a number of physical resources - not sure
if at the moment this is a genuine scenario. Should probably wait and see how the library deveops before answering this
question.
