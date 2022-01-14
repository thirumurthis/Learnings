

| Comparable | Comparator |
|----------|------------|
| Comparable sorts based on one properties of the class. `single sorting sequence` | Comparator provides support to `multiple sorting sequences` many properties in object |
| Comparable `modifies the original class`, we need to implement the class and override `compareTo()` | Comparator doesn't modify the original class |
| `compareTo()` method used for sorting | `compare()` method used for sorting |
| usage: `Collections.sort(List<>)` | Usage `Collections.sort(List<>, Comparator)` |
| Exists in `java.lang` package | Exists in `java.util` package |
