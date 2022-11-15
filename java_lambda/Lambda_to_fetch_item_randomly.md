
- Below is pick the item from a list in random fashion.

```java
package org.simpleapp;

import java.util.List;
import java.util.Random;

public class SimpleUtil {
    public static void main(String[] args) {

        final List<String> deviceList = List.of("device1", "device2", "device3");
        Random random = new Random();
        String device = deviceList.stream()
                .map(index -> random.nextInt(deviceList.size() - 1))
                .map(item -> deviceList.get(item))
                .findFirst().orElse("UNKNOWN-DEVICE");

        System.out.println(device);
    }
}
```
