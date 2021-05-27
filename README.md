# jdo-audit-hook

Use of InstanceLifecycleListener to do basic auditing.

Notifications are sent to the various listener methods, and so you can detect INSERT / UPDATE / DELETE events
and store any information you require from that inwhatever backend is appropriate. 
This particular sample just uses logging of the events. 

Note that a persist of a new object will receive a create event as well as a preStore and postStore event.

Use of the DN Persistable interface allows access to field values on the object pre and post store.

You could link in with transaction boundary and demarcate the events in particular transactions.
