# Saving Sitemaps

Sitemaps are typically created by providing `.sitemap` files on the server. 
The class `ManagedSitemapProvider` offers a way to create sitemaps from JSON that can be accepted by a PUT REST endpoint.
Sitemaps created and modified like this will be persisted like many other ESH entities to a `Storage` object, using the sitemap name as a key.

Whenever a sitemap is stored via REST PUT, the result will get rendered again and returned as a result.

When saving sitemaps from a client, some rules have to be obeyed. Just storing the sitemap JSON as it was received by the client is not the way to go. Here is why:

# How to deal with a sitemap to save it?

Some attributes in a sitemap JSON reflect the actual state of the `Item`s rendered on the server. These are:

* `item`
* `label`
* `valuecolor`
* `labelcolor`

It does not make sense trying to store them. You cannot modify an `Item` from a sitemap, neither can you save the labelcolor that corresponds to the `Item`'s current state or value. But it makes sense to store the corresponding configuration data as they are:

* `itemname`
* `labelformat`
* `valuecolors`
* `labelcolors`
* `visibilityrules`

## `itemname`
`Item`s are referenced by name. So put the name of the item to the `itemname` attribut. On next rendering, you will find `itemname == item.name`.

## `labelformat`
The sitemaps on server-side holds a template on how to render the actual `label` from the `Widget`'s (resp. `Item`'s) state. This template is transported by attribute `labelformat`.

```
"labelformat": "Date [%1$tA, %1$td.%1$tm.%1$tY]"
``` 

will result in

```
"label": "Date [Freitag, 13.11.2015]"
```


## `valuecolors`
The color given in `valuecolor` is a result of applying some rules to the `Item`'s state. These rules are stored as an array and evaluated, when JSON is rendered.

```
"valuecolors": [
  {
    "arg": "lightgray",
    "state": "90",
    "condition": ">",
    "itemname": "DemoTemperature"
  },
  {
    "arg": "orange",
    "state": "25",
    "condition": ">"
  },
  {
    "arg": "green",
    "state": "15",
    "condition": ">"
  },
  {
    "arg": "blue",
    "state": "5",
    "condition": "<="
  }
]
```

## `labelcolors`
See `valuecolors`.


## `visibilityrules`

Visibility rules are evaluated and result in setting the `visibility` attribute which is allowed to take the values `INVISIBLE`,`INACTIVE` and `ACTIVE`. Currently only `INVISIBLE` and `ACTIVE` are supported. 

```
"visibilityrules": [
  {
    "state": "OPEN",
    "condition": "==",
    "itemname": "DemoContact"
  }
]
```

In case the state of the `DemoContact`'s state evaluating to `"OPEN"`: `visibility == "ACTIVE"`.


# How to deal with `linkedPage`

The JSON rendering in some cases chooses to introduce a `linkedPage` which contains the actual sub-widgets of a `Text`, `Image`, or `Group` widget. The latter can be considered as the most common case, for a `Group` widget represents a `GroupItem`. The `widgets` array in the `linkedPage` either consists of the configured children of the `Group` or is generated from the `Item`'s members. When writing back, these facts have to respected, otherwise generated content might become configured. 
In order to do so, `widgets` of the `linkedPage` is always ignored on write. In case sub-widget configuration is intended, the respective widgets have to be added to the parent widget of the `linkedPage`, which typically is the `Group`. Leave it empty and the sub-widgets will be auto-generated again on next rendering. The configured sub-widgets will then show up in the `linkedPage` again. The `Group`'s own `widgets` will always render empty.


# saving sitemap cookbook

After you received the current sitemap from the server, use the following hints to transform it into a saveble sitemap. 

* ignore all `item` attributes
 * if you want to reference an `Item`, use `itemname`
* ignore `valuecolor` attribute
* ignore `valuecolor` attribute
 * use `valuecolors` array to configure the rules how `valuecolor` has to be set
* ignore `labelcolor` attribute
 * use `labelcolors` array to configure the rules how `labelcolor` has to be set
* use `visibilityrules` array to configure the rules how visibiliy is treated
* if you need to configure widgets from a `linkedPage`, move them one level up in the JSON hierarchy
 * notice that if you configure one, the auto-enumeration is stopped, so configure all or none
 
