java

```java
    public static byte[] gZipCompress (String inputData) throws AhmException {
        if ((message == null) || (message.length() == 0)) {
            return "".getBytes();
        }


        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
                gzipOutputStream.write(message.getBytes(StandardCharsets.UTF_8));
            }
            return byteArrayOutputStream.toByteArray();

        } catch(IOException e) {
            throw new Exception("IO Exception Failed to zip content" + e.getMessage());
        }

    }
```
check [Link](https://gist.github.com/yfnick/227e0c12957a329ad138)
