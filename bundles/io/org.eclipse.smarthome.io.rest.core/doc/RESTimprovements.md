# non-breaking REST API changes

## all
* in case of unhandled exception, don't return a HTML page, but a well defined JSON structure instead. 

```json
{
  "error": {
    "message": "HTTP 404 Not Found",
    "exception": {
      "class": "javax.ws.rs.NotFoundException",
      "message": "HTTP 404 Not Found",
      "localized-message": "HTTP 404 Not Found",
      "stacktrace": [
        {
          "declaringClass": "org.glassfish.jersey.server.ServerRuntime$2",
          "methodName": "run",
          "fileName": "ServerRuntime.java",
          "lineNumber": 305
        },
        {
          "declaringClass": "org.glassfish.jersey.internal.Errors$1",
          "methodName": "call",
          "fileName": "Errors.java",
          "lineNumber": 271
        },
        {
          "declaringClass": "org.glassfish.jersey.internal.Errors$1",
          "methodName": "call",
          "fileName": "Errors.java",
          "lineNumber": 267
        }
      ]
    }
  }
}
```

## ThingResource@POST
* returns created object as JSON (implemented)
* return http code 409 (CONFLICT) instead of 500 in case the object already exists (implemented)
* return existing Things data in case it already exists

```json
{
  "error": {
    "message": "Thing yahooweather:weather:foo already exists!"
  },
  "entity": {
    "statusInfo": {
      "status": "UNINITIALIZED",
      "statusDetail": "NONE"
    },
	"link": "http://localhost:8080/rest/things/yahooweather:weather:foo",
    "configuration": {
      "refresh": 30
    },
    "properties": {},
    "UID": "yahooweather:weather:foo",
    "channels": [
      {
        "linkedItems": [],
        "id": "temperature",
        "itemType": "Number",
        "properties": {}
      },
      {
        "linkedItems": [],
        "id": "humidity",
        "itemType": "Number",
        "properties": {}
      },
      {
        "linkedItems": [],
        "id": "pressure",
        "itemType": "Number",
        "properties": {}
      }
    ]
  }
}
```

## ThingResource@GET
* provide a link to the Thing (as ItemResource does)
```json
{
  "statusInfo": {
    "status": "ONLINE",
    "statusDetail": "NONE"
  },
  "link": "http://localhost:8080/rest/things/yahooweather:weather:foo",
  "configuration": {
    "refresh": 30
  },
  "properties": {},
  "UID": "yahooweather:weather:foo",
  "channels": [ ]
}
```

## ThingResource@PUT
* return the updated Thing
* return 409 (CONFLICT) in case the Thing exists but cannot be updated
* only updates Thing if it is managed, otherwise the altered Thing will remain until next openHAB reboot


## ItemResource@PUT
* return 201 in case of creation, 200 in case of update
* return 409 (CONFLICT) in case the Item exists but cannot be updated
* returns the actual JSON for the object



#
# breaking REST API changes
#

## ThingResource@POST (implemented)
* returns 201 instead of 200 when creation

## ThingResource@GET/thingUID (implemented)
* returns 404 instead of 204 in case thingUID cannot be found  

## ThingResource@DELETE/thingUID (implemented)
* returns 404 only in case thingUID cannot be found
* if un-forced deletion fails, return 202 (Accepted) and the still undeleted object
 * contains a link to check back later for status
* if forced deletion fails, return 500 and the still undeleted object 


# breaking API changes

## ManagedThingProvider#createThing should throw ThingExistsException
* currently throws IllegalArgumentException which is a RuntimeException
 * caller has to guess what the reason is
* ThingExistsException should contain the existing Thing