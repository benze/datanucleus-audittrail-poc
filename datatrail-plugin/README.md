# DataNucleus DataTrail Transaction Listener

This package has been designed as a plugin to DataNucleus in order to produce a tracking system of changes made to DataNucleus tracked
entities, similar in concept to Hibernate's Envers, but implemented specifically for DataNucleus.  The plugin aims to track CREATE, UPDATE or 
DELETE changes to FCOs and their fields.

## Requirements
- DataNucleus 5.2

## Developed and designed for
- RDBMS backing store
- Optimistic transactions
- DataStore Identity: KodoDatastoreId

### Limitations
While this package should in theory work for Pessimistic Transactions and Non Transactional, it has not been tested
under those conditions so might have some remaining issues to address.


# How to use
Usage of the plugin is fairly straight forward.  There are a couple of configuration requirements to make in 
the application:
1. Add the datanucleus-plugin to the classpath and enable the following 
settings in the `persistence.xml` file:

```xml

<property name="javax.jdo.option.RestoreValues" value="true"/>  <!-- stores the value of the object before making dirty in the StateManager -->
<property name="datanucleus.objectProvider.className" value="org.datanucleus.datatrail.spi.DataTrailStateManagerImpl"/>
```

2. Attach the transaction listener to the persistence manager
```java
        // Create of object
        PersistenceManager pm = pmf.getPersistenceManager();

        TransactionListener txListener = new TransactionListener(events -> {
            // do something with the entity events at end of transaction
        });
       txListener.attachListener(pm, null);
```

## TransactionListener
### TransactionListener attachment
The [TransactionListener](src/main/java/org/datanucleus/datatrail/spi/TransactionListener.java) is the entry point to enable the DataTrail library.
It requires a `PersistenceManager` and a list of `Persistable` objects to advise.  If the list is `null`, then all classes are automatically
included for advice.  Otherwise, the list can be tailored to the required models on a per-PM basis.  If the list is empty or missing, then 
no classes will be included in the data trail.


### TransactionListener callback
The TransactionListener uses a callback/lambda to supply a collection of events/changes at the end of the transaction
via the [DataTrailHandler](src/main/java/org/datanucleus/datatrail/spi/TransactionListener.java).  This class has a single 
`execute()` method which is supplied a `Collection<Node> entities` to represent the changes that have been tracked for all FCO objects 
that are part of the transaction.  The client application is then free to manage the tracked changes as best suited; 
for example log to a file, write to a NoSQL database, sent to an MQ, etc.

Unlike Envers, the transport layer for the changes is kept independent of the plugin.  Future development of this plugin can 
potentially produce different DataTrailHandlers, responsible for each transport layer.


# Design Approach

## Package structure
All "public" classes can be found in the base `org.datanucleus.datatrail.spi` package.  This includes all interfaces and implementations
that are required to be consumed outside the plugin.

The `org.datanucleus.datatrail.impl` package includes the internal implementation details for the plugin.  There is no practical reason to consume 
any of these classes outside the scope of the plugin.  Furthermore, this package is not part of the SPI and is subject to change without notice.

The `org.datanucleus.datatrail.store.types.wrappers` package are plugin specific implementations of the java-types (ie: proxy-objects) that
DataNucleus defines in the core lib and uses internally to track changes to SCOs.  The default implementations have been extended to 
use a [ChangeTracker](#change-tracker) to allow for change-tracking (ie: store prior values).  


# Implementation

The plugin aims to build a representation of all changes that are made to a FCO's fields during the course of a given 
transaction.  In order to best accomplish this, it builds a tree-like structure using `org.datanucleus.datatrail.Node` objects, with each
node representing either the FCO (the root node), or an FCO or SCO or primitive value in a subtree or leaf node.

## Types of Nodes
There are different families of nodes that exist to represent different types of objects, as represented by the 
[NodeType](src/main/java/org/datanucleus/datatrail/spi/NodeType.java) enum separated in 2 different orders.

#### Leaf Nodes
- [PRIMITIVE](#primitive)
- [REFERENCE](#reference-fco)
- [ENTITY](#entity-fco)

#### Container Nodes
- [ARRAY](#array)
- [COLLECTION](#collection)
- [MAP](#map)

All nodes have the same basic properties which are maintained by the data trail:
- NodeType: the type of node being represented
- Name: the name of the field represented by the node
- ClassName: the java classname for the object
- NodeAction: the action being applied to the object [CREATE, DELETE, UPDATE](#nodeaction)

In addition, the [Leaf Nodes](#leaf-nodes) have the following properties.  Note that not all properties will be populated for each
leaf node type:
- Value: the current value of the object
- Prev: the value of the object at the start of the transaction

In contrast, the [Container Nodes](#container-nodes) have the following properties.  Note that not all properties will be populated
for each container type:
- Added: identifies objects which have been added to the container during the transaction
- Removed: identifies objects which have been removed from the container during the transaction
- Changed: identifies existing objects in the container which have mutated during the transaction
- Contents: the complete list of objects stored in the container at the end of the transaction


In addition, each family may have additional parameters to information to it's specific needs. 

 
### Primitive
This is the most basic type of node/value represented in the tree.  It is essentially a String representation of the value of the data.
It is not limited to primitive java objects, but rather any non-`org.datanucleus.enhancement.Persistable` objects which are not managed by
DataNucleus.

The value of each object not identified as a DN managed object will be converted to a string representation using a [StringConverter](#string-converter).

#### Additional Parameters:
- None

A Primitive node is a leaf node.


### Reference (FCO)
A reference object is a DataNucleus First-Class-Object which implements the `org.datanucleus.enhancement.Persistable` interface.  

#### Additional Parameters:
- Version: the entity version if enabled in DataNucleus
- Description: an entity-specific description of the contents.  Uses a [DataTrailDescription](#datatraildescription-interface) to retrieve the value

- Value: the value represented by a Reference node is the string representation of the Object's ID as retrieved by the `dnGetObjectId()` enhanced method  
- Prev: the prev object ID which was stored in this field

A Reference node is a leaf node.

### Entity (FCO)
This is the top level node found in the tree.  It represents the FCO object being tracked by the persistence manager.  Each Entity object's fields
will be identified and tracked in the data trail.  Entities or fields which are annotated by the [DataTrail](#datatrail-annotation) will be excluded
from the datatrail.  Similarly, any classes or fields annotated by `org.datanucleus.api.jdo.annotations.ReadOnly` annotation will also be skipped
from the datatrail.

Each entity includes a [TransactionInfo](#transactioninfo) object used to identify parameters that are specific to the transaction that is retrieved
from the PersistenceManager.

#### Addtiional Parameters:
- Fields: set of fields that are part of the entity
- DateModified: the date the entity was modified (ie: the date the transaction)
- Username: the user responsible for triggering the changes
- TransactionId: a transaction id used to correlate all entities from the same transaction together

An Entity node is a root node.



### Array
This is container node.  Unfortunately, there is no way to proxy/wrap an array in Java in order to identify changes to the individual elements.
Instead, this type of container will simply be able to list the elements when the array is created, deleted or the full list of elements when the
array is updated.  However, it is unable to identify what changes occured in the array itself.

#### Addtiional Parameters:
- Contents: this is the only additional parameter that is available to this node 

An Array node is an inner node.


### Collection
This is a container node which tracks all implementations of Java collections. This node has the ability to identify objects which have been added,
or removed from the Collection during the transaction.  Only contents are traced - not positional data.  Consequently, for ordered lists/collections, if elements are both added and removed during the same
transaction, it will automatically disable its "change" tracking and only expose the full collection contents at the end.  This is due to the inherent
difficulty in tracking positional changes in a list (ie: if the element is removed from index 0 but inserted at index 2, is the element actually removed 
or added?).  Finally, if the total number of adds or removes during the transaction exceeds the size of the collection, the "change" tracking is disabled
and the full contents are exposed.

#### Addtiional Parameters:
- Contents: complete list of elements in the collection.  Mutually exclusive with Added/Removed.  Exposed if the total number of add/removed exceeds
the size of the collection, or of adds & removes are performed on an ordered collection within the same transaction. 
- Added: elements that have been added from the collection
- Removed: elements that have been removed to the collection

A Collection node is an inner node.


### Map
This is a container node which tracks all implementations of Java maps.  This node will identify which key/value pairs have been added, removed or changed
during the transaction.  Both key changes and value changes are tracked.  Key changes will be idenfied as a remove of the original key and an add of
the updated key.  A value change will be identified in the changed list.  Similarly to the Collection node, if the total number of added, removed and changed
exceed the size of the map, the "change" tracker will be disabled and the full contents will be exposed.

#### Addtiional Parameters:
- Contents: complete list of key/value pairs in the map.  Mutually exclusive with Added/Removed/Changed.  Exposed if the total number of add/removed/changed
exceeds the size of the collection, or of adds & removes are performed on an ordered collection within the same transaction.
- Added: key/value pairs that have been added from the map
- Removed: key/value pairs that have been removed to the map
- Changed: key/value pairs which identify values which have changed during the transaction


A Map node is an inner node.


### NodeAction
The [NodeAction](src/main/java/org/datanucleus/datatrail/spi/NodeAction.java) enum identifies the different action (or mode) under which the object
was required:
- CREATE: represents a new value identified in the transaction which was not previously existant 
- UPDATE: represents a previoulsy persisted value which was modified during the transaction
- DELETE: represents a previously persisted value which was deleted during the transaction


## DataTrailDescription Interface
The [DataTrailDescription](src/main/java/org/datanucleus/datatrail/DataTrailDescription.java) interface is used by the [Reference](#reference-fco) object to retrieve a description specific
to the given instance being tracked.  Each object class can implement an independent implementation to produce a string which describes the instance
uniquely.

## Converters
The [Primitive](#primitive) nodes use a [StringConverter](src/main/java/org/datanucleus/datatrail/StringConverter.java) implementation in order to
get the string representation of the object being tracked.  The `StringConverter` implementations are loaded via the SerivceLoader pattern, by identifying
any custom implementations in the [org.datanucleus.datatrail.StringConverter](src/main/resources/META-INF/services/org.datanucleus.datatrail.StringConverter)
configuration file.  The implementations must be thread-safe as only a single instance of the class will be created and used.  

Each time the [Primitive node](#primitive) converts an Object to a String, it searches for an appropriate converter by prompting each converter
loaded by the ServiceLoader if it is able to support the Object's class, and selects the converter with the highest defined priority.   

By default, the library provides 2 default implementations with lowest precedence priority which cannot be removed:  

- [ObjectConverter](#objectconverter) 
- [NullConverter](#nullconverter)

The interface requires the implementation of 2 methods:
- `supports(Class<?> clazz)`: used to identify if this converter can be used to generate a string for the given type of object
- `getAsString( Object value )`: used to apply logic and retrieve the string representation for the given object.  For instance

Additionally, it is highly recommended to provide implementations of the `hashCode()` and `equals()` methods to ensure that only a single instance is
loaded into memory.  See implementations of the [ObjectConverter](src/main/java/org/datanucleus/datatrail/impl/nodes/converter/ObjectConverter.java)
for an example.

Finally, a [Priority](src/main/java/org/datanucleus/datatrail/spi/Priority.java) can be applied to the converter in case multiple
converters are discovered for a given class.  In the case where two converters are found with the same priority, there is no guarantee which converter
will be selected.


### ObjectConverter
[ObjectConverter](src/main/java/org/datanucleus/datatrail/impl/nodes/converter/ObjectConverter.java) is the basic converter used to create
the string representation of the Object by calling the default `toString()` method on the object.

### NullConverter
[NullConverter](src/main/java/org/datanucleus/datatrail/impl/nodes/converter/NullConverter.java) is a base converter used to prevent NullPointerExceptions
and provide a null value as a string representation. 


## TransactionInfo
The [Transaction Info](src/main/java/org/datanucleus/datatrail/TransactionInfo.java) interface is used to provide the DataTrail with transaction specific
contextual information which is then injected into the [Entity](#entity-fco) node.  The DataTrail searches for a `TransactionInfo` object in the PersistenceManager
in a UserObject keyed by the `TransactionInfo` classname.

```java
    TransactionInfo txInfo = new TransactionInfoImpl( Instant.now() );
    pm.putUserObject(TransactionInfo.class.getName(), txInfo);
```

Currently, the interface supports 3 properties, but this can be expanded
at a future date.
- txDate: the date the transaction occured
- userId: the user id which triggered the transaction
- txId: a unique identifier used to correlate all entities from the same transaction

A default [TransactionInfoImpl](src/main/java/org/datanucleus/datatrail/impl/TransactionInfoImpl.java) is provided in the library, but each client is recommended
to provide their own implementation of the class as the default implementation is used for internal purposes and is subject to change without futher notice.


## @DataTrail annotation
The [DataTrail](src/main/java/org/datanucleus/datatrail/spi/DataTrail.java) annotation is used to identify which models or fields
should be included or excluded from the data trail.  By definition, any `@org.datanucleus.api.jdo.annotations.ReadOnly` and `@Transient`
fields are excluded from the DataTrail as they cannot by modified.  Optionally, other fields can also be identified using the 
`@DataTrail(excludeFromDataTrail=true)` annotation as well.

By default, if no classes (ie: a null list) are specified in the [TransactionListener](#transactionlistener-attachment) then all classes are automatically
included in the datatrail.  The `@DataTrail` annotation can then be used to filter out individual classes.


## DataTrailFactory
The [DataTrailFactory](src/main/java/org/datanucleus/datatrail/impl/DataTrailFactory.java) is the heart of the implementation of the DataTrail.
It is the entry point into creating the tree structure of nodes to represent each type of object being modified during the current transaction.
To accomplish this, it delegates the choice of which node to create to any registered [NodeFactory](#nodefactory).  NodeFactory can be registered
manually, or automatically.  The `DataTrailFactory` supports 2 different types of automatic registration:
- ServiceLoader pattern
- Classpath scanning

### ServiceLoader
The `org.datanucleus.datatrail.impl.DataTrailFactory.getDataTrailFactory()` method will delegate to using Java's ServiceLoader pattern and
load any NodeFactory defined in the [org.datanucleus.datatrail.spi.NodeFactory](src/main/resources/META-INF/services/org.datanucleus.datatrail.spi.NodeFactory)
configuration file.


### Classpath scanning
An alternate mechanism to instantiate a `DataTrailFactory` is to use the `org.datanucleus.datatrail.impl.DataTrailFactory.getDataTrailFactory(java.lang.Class<?>)`
method, in which the factory will automatically scan the given classpath and load any class implementing the [NodeFactory](#nodefactory) 
interface.  This requires an additional dependency in the classpath [io.github.classgraph:classgraph]() dependency in the classpath.



## NodeFactory
Each type of supported node is instantiated via it's [NodeFactory](src/main/java/org/datanucleus/datatrail/spi/NodeFactory.java).  Currently,
there is support for the following type of nodes:
- array
- collection
- entity
- map
- primitive
- reference

The factory exposes a [NodeFactory](src/main/java/org/datanucleus/datatrail/spi/NodeFactory.java) methods to identify if the current
value, with the given DataNucleus metadata is supported by the given node type.  The 
[DataTrailFactory](src/main/java/org/datanucleus/datatrail/impl/DataTrailFactory.java) searches for the factory with the highest priority
defined when selecting the type of node to be created.

The [NodeDefinition](src/main/java/org/datanucleus/datatrail/spi/NodeDefinition.java) annotation can help identify which type of 
node is being created.



## DataNucleus Wrapper (Proxy) objects
DataNucleus uses Wrapper/Proxy objects to track changes to SCOs.  These objects are defined as `java-types` and are found in the DN core libs in the
`org.datanucleus.store.types.wrappers` package.  In order to track changes to the container objects, this lib needs to provide its own implementations
of the wrappers and inject the [Change Tracker](#change-tracker) into the implementation.

Currently, there are a limited number of backed wrapper objects that have been updated and consequently change-tracking support is limited to those types
of objects.


### Change Tracker
The [ChangeTracker](src/main/java/org/datanucleus/datatrail/store/types/wrappers/tracker/ChangeTracker.java) is a component which has been copied in
part from the [Apache OpenJPA](https://github.com/apache/openjpa) project.  The implementation found within this library attempts to stay as close
as possible to the original implementation as it has been tried and tested over many different versions of the project.  A few minor changes were
required to be compliant with the DataNucleus approach, but the concepts remain the same.

#### ChangeTracker and ChangeTrackable Interfaces
The [ChangeTrackable](src/main/java/org/datanucleus/datatrail/store/types/wrappers/tracker/ChangeTrackable.java) is a marker interface to indicate
that the given proxy/wrapper object has been enhanced with the change tracker, and provides a getter to retrieve the tracker.  Additionally, it 
provides a couple of helper implementations to add data to the change tracker without needing to modify the underlying `ChangeTracker` class or
interface.

The [ChangeTracker](src/main/java/org/datanucleus/datatrail/store/types/wrappers/tracker/ChangeTracker.java) interface and implementations are used
to keep track of changes which occur within the proxy objects, such as additions, removal or modifications.  Each time a modification is made via the 
proxy object, a call is made to the `ChangeTracker` to keep a list of the change made.  Finally, during the commit phase of the transaction, the
`ChangeTracker` can be accessed via the proxy object to retrieve the list of changes.



# DataTrailStateManager
In order to identify changes to modified fields, the [DataTrailStateManager](src/main/java/org/datanucleus/datatrail/spi/DataTrailStateManagerImpl.java)
must be attached to all advised objects.  Currently the only way to accomplish this is to specify the custom  `javax.jdo.spi.StateManager` in the
`persistence.xml` file (see [How To Use](#how-to-use)).  Without this configuration, DataNucleus will not know to store initial values
of changed fields and consequently not be able to identify the previous field when creating a Node.

Longer-term, changes to the DataNucleus core project could potentially identify classes which are identified as being part of the 
DataTrail and automatically attach/select the appropriate `StateManager`, but this is not currently present in DataNucleus 5.2.

Add the following entry to the persistence manager:
```xml
    <property name="datanucleus.objectProvider.className" value="org.datanucleus.datatrail.spi.DataTrailStateManagerImpl"/>
```





# Remaining Tasks
- Missing SCO proxy classes need to be implemented
- An opt-in vs opt-out default setting
- Automatic integration in the transaction lifecycle if the plugin is added
- Enabling the plugin persistence.xml properties
- Automatic inclusion of the correct StateManager supplied by the plugin
- More thorough testing using different test-harnesses
- NoSQL integration & testing
- Performance testing and validation
- Rollback support (currently, only a datatrail on successful Commit)
- Testing for application identities
- Testing for different datastore implementations
- Testing for multi-PK
- FCO without ID
- Class-level InstanceLifecycleEvents are not currently fully supported (ie: they are called after the transaction level lifecycle events)
