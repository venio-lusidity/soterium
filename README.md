# Soterium at a Glance #

Virtually all COTS or open-source database engines require additional components to build a complete enterprise application. There is a rich, diverse market of database engines, lower-level components (for example, various libraries such as Apache Commons, Google Guava, jQuery, Bootstrap, and others), as well as single-layer frameworks (for example, AngularJS for Web clients). However, integrating such components into a complete, full-stack application (typically consisting of database, application, and presentation layers) requires a large amount of “glue” code and infrastructure, such as object/relational mapping (ORM) or object/graph mapping (OGM), data access services, and security.

* Languages: JAVA, C# , HTML, CSS, JavaScript.
* Run Time Environment: JRE 1.8.x.
* Operating System: any, currently on RHEL 7 or higher.
* JavaScript Object Notation (JSON) native.
* Services based API’s  for data consumers using REST.
* REST and lower level interfaces for developers.
* Large scale business oriented ad-hoc and concurrent data modifications.
* Allows for the change out of datastore and/or index store.
* Allows for the Presentation layer to be run on any platform whether it is web, thin or thick client.
* Soterium can be scaled using load balancer.

## Athena Highlights

* Class Object Model
    * Comprises of ApolloVertex>Edge and ApolloVertex>Domain.
    * AtSchemaClass: Class friendly name, definition, discoverable, writable other than system.
    * KeyData<T>: Property model, reads/writes to JSON on demand, uses KeyDataHandler to extend business processes on a per property basis, determines if discoverable.  Objects expected include primitives and vertices.
    * KeyDataCollection<T>:  Same as KeyData<T> except that it supports a collection of objects.  
    * ElementEdges: Collection of edges linking domain types.
    * Note that the KeyData objects and the ElementEdges are only loaded on demand not when the Class is constructed.  The increase in performance is exponential with the more objects being worked with as it does not load unused properties.  An example would be retrieving data requested by the UI and only the JSON is needed to be returned with no further messaging of the data.  
* ElementEdges: Understands the context it is in and can retrieve the linked objects with no further code needed other than definig the annotation AtSchemaProperty.  Can be and should be extended if the known collection size grows exponentially.  An example would be VulnerabilityEdges.
* Edge:  Defines the relationship between two vertices.  Can be extended if extra data about the two objects is needed.  An example would be VulnerabilityEdge.fetchPassed().getValue().
* ETL:  Extract, Transform and Load 
* Inference Engine:  Leveraging the components of Soterium an RMK purpose built to cleanup suspect CPE data. 
* Messaging Service:  Can use REST API to send email and or User Notifications.  User Notifications can be called via the REST API.  Emails can be schema driven.
* Jobs Engine:  A Job is a process that is either ran at specific times of the day, can run continuously and/or manually within a separate thread in the application.  Jobs are configurable in the configuration files.  An example would be the vulnerability auditing job which runs ever day at a specific time.  Can be started automatically using configuration file or a UI using the REST API.
* Worker Engine:  Workers are much like Jobs but run independently of an engine.  Can be started using scripts or by a UI using the REST API.
* Vulnerability Auditor:  Leverages the components of Soterium and is purpose built for RMK.
* WebResourceHelper Interface:  When a REST service is called and an object is retrieved from the datastore, that object can implement a WebResourceHelper to apply business processes to that object.  This could either extend/remove data being returned and/or run some other logic.
* Policy Decision Points (part of CBAC):  An extendable class that generally determines some sort of permissions on a vertex.  Can also be used to run alternate processes depending on what the user is trying to do.  More than one policy can be applied to a vertex at a time.
* Workflow:  The workflow engine allows the customer or developer to create purpose built workflow.  The workflows uses steps that are context aware.

## Discovery Engine Highlights

* The KeyData property class has a parameter during construction called “discoverable”.  When this property is set to true the Discovery Engine will search within that property.  If the value is set to false then it is not searchable via the Discovery Engine.
* Specific classes can be targeted.
* Uses a provider interface to better target search queries.
* Can include child searches that have relationships with a search result.
* Has a generic item class that returns common data among vertices.
* Has a discovery item object that a Class can implement to return extended data about the result item.  An example would be a Person object being returned and wanting to include the contact info for that person in the result.

## Services Highlights (REST)

* Implements the RESTlet.org API.
* Credentials model PKI and Windows Azure Federated Identity (ACS)
* Services are easily implement by extending BaseServerResource
* Define security by declaring AtAthorization annotation.
* Define endpoint by declaring AtWebResource annotation.
* Implements the Security Model either by Groups and/or Scoping.
* API Key authorization extends the Security Model for system to system authentication and authorization.  Access to objects are subject to what the owner of the identity is authorized for.

## Importer Highlights

* Importers can be rapidly built for new data sources 
* Automated class construction
* Soterium client can be used to automate importing

## Data Access Layer Highlights (Apollo)

One of the features of the Soterium framework is that it can use a dedicated indexing engine in combination with a database engine. Both the database engine and indexing engine are “pluggable,” meaning that different underlying engines can be used depending on the requirements of a particular application.  

Apollo features a query factory which implements most of the commonly used queries and a query builder interface which dynamic builds queries regardless of the language of the underlying index and/or datastore.

## JsonData Highlights (JSON Processor)

* Extends org.json.JsonObject and org.json.JsonArray
* Is the underlying data object for all class objects.
* Implements a key parsing for easier retrieval.
    * {“name”: {“last”: “paris”, “first”: “tom” }} 
    * last = jd.getString(“name”, “last”) or jd.getString(“name::last”);
    * fist = jd.getString(“name”, “first”) or jd.getString(“name::first”);
* Implements “get” of many primitive types, i.e.. jd.getDate(“createdWhen”) will return a date time value.
* Natively exportable to Excel or CSV with a schemaless or schema driven layout.
* Can be saved natively as json as a single line or formatted.
* Can be saved as a Soterium “.jd” file.  This file consists of a first row of meta data, customizable, and every row after is a single JSON object.  This allows for streaming line by line reducing memory overhead for large files.  This can also be appended to line by line so the entire file can be updated without being resconstructed.
* Can do limited internal searching.
* Contains a custom iterator if the JsonData is an array.
* Property level collision management.  If the below behavior is not wanted then you would do an update instead of put.
    * jd.put(“color”: “red”)
        * {“color”: “red”}
        * jd.put(“color”: “blue”)
        * {“color”: [“red”, “blue”]}
    * jd.put(“color”: “red”)
        * {“color”: “red”}
        * jd.update(“color”: “blue”)
        * {“color”:  “blue”}
* No known restrictions on value.  Other like frameworks have restrictions on accent characters, special characters and data transformation issues like leading zeros.

## Soterium Client Highlights

* System to system p12 server certificate with API Key identity authentication and authorization.
* JAVA and C# clients.
* Registration must originate from client and authorized by a system administrator.
* Scoping applies, if enabled, to objects queried.
