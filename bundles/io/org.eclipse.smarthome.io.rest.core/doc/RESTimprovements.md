# non-breaking REST API changes

## ThingResource@POST
* returns created object as JSON (implemented)
* return http code 409 (CONFLICT) instead of 500 in case the object already exists (implemented)



# breaking REST API changes

## ThingResource@POST
* returns 201 instead of 200 when creation (implemented)


# breaking API changes

## ManagedThingProvider#createThing should throw ThingExistsException
* currently throws IllegalArgumentException which is a RuntimeException
** caller has to guess what the reason is
* ThingExistsException should contain the existing Thing