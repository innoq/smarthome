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
  "thing": {
    "statusInfo": {
      "status": "UNINITIALIZED",
      "statusDetail": "NONE"
    },
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
}```



# breaking REST API changes

## ThingResource@POST
* returns 201 instead of 200 when creation (implemented)


# breaking API changes

## ManagedThingProvider#createThing should throw ThingExistsException
* currently throws IllegalArgumentException which is a RuntimeException
** caller has to guess what the reason is
* ThingExistsException should contain the existing Thing