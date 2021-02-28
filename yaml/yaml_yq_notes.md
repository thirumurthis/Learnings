#### install `yq` which is similar to `jq` (json parser cli on linux)
  - `yq` per documentation uses the same syntax of `jq` as much as possible for basic operations


```yaml
# filename: yaml-1.yaml
fruits:
   sweet: 
      - apple
      - banana
   acidic: 
      - orange
      - grape
vegetable:
    green:
     - spinach
    orange:
     - carrot
```
```
$ yq e '.vegetable'  yaml-1.yaml
## output
green:
  - spinach
orange:
  - carrot
```
------------
### Yaml with `|` and `>` representation
  `|` - preserves the white space
     - `|` can have `+` and `-` like `|+` and `|-`.
     - `|+` - indicatest to keep the last white space
     - `|-` - indicates to not consider the last white space
     - 
```yaml
vehicle:
  two.wheeler: |
     cycle=2
     motorcycle=2
     
  four.wheeler: >
     car=4
     truck=4
```
```
>  yq e '.vehicle' .\yaml1.yaml
two.wheeler: |
  cycle=2
  motorcycle=2
four.wheeler: >
  car=4 truck=4
```

##### with `|+` and `>-`
```
vehicle:
  two.wheeler: |+
     cycle=2
     motorcycle=2
     
  four.wheeler: >
     car=4
     truck=4
---
yq e '.vehicle' .\yaml1.yaml  ## note the additional white preseved |+
two.wheeler: |+
  cycle=2
  motorcycle=2

four.wheeler: >
  car=4 truck=4
```
