
Sample yaml file, content represented as below. 
  
```yaml
Vegetable:
    -  carrot: orange
       beans: green
       spinach: green

```
### Representing data in __`key-value`__ pair.

```
name: Tom
id: 12121
phone: 2021111234
```
  Note: space between : and value: ( name is the key, Tom is the value)
  
### Representing __`Array`__ or __`list`__ in Yaml
 -  use of `-` indicates that is an element of an array

```yaml
Cars:
-   Toyota
-    Hyndai
-    BMW
```

### __`Dictionary`__ or __`Map`__ representation

```yaml

# Note the blank space in the second line, following lines should also had.
Toyota:
    Wheels: 4
    Model: Corolla
    
Hyndai:
    Wheels: 4
    Model: Sonota
```

```yaml
Banana:
   Calaries: 60
   Fat: 0.5 g
```
 Note: value can have space.
 
 ### `#` in yaml file is used for comments.
 
 ### Incorrect representation
 ```
 Banana:
   Calaries: 60
     Fat: 0.5 g
     
  # The above is not incorrect represenation, since there is additional
  # space in the Fat which ideally reports value already exists for Calaries
  # and throw exception
 ```
 
### Representing List containing dictionary which contains list. 
 ```yaml
 Car:
     -   Corolla:
           model: 2000
           make: Toyota
           type: sedan
     -   Camry:
           model: 1999
           make: Toyota
           type: sedan
 ```

```yaml
-  Corolla:
      build: 
         make: Toyota
         model: 2020
      type: sedan
-  Nissan
      build:
         make: Sentra
         model: 2000
      type: sedan
```

### Ordering on List and Dictionaries:
   - Dictionary are unorderd collection
   - List are ordered collection
   
 ##### Dictionary  
```
# Below two representation are valid for dictionary, 
# though items are NOT in order 
# both represent the SAME (the values should match).

Bird:                      |    Bird:        
    name: sparrow          |        name: sparrow
    weight: 30 g           |        type: small
    type: small            |        weight: 30 g
```

##### List
```
# Below two list are NOT the same since order is not correct.
Bird:                     |     Bird:
- sparrow                 |     - crow     
- crow                    |     - parrot
- parrot                  |     - sparrow
```
