- Define the secret provider
  - In below example we mount the azure blob storage as volume and read the key from it and udpate a properties file.
  - This can be achived using helm chart for azure as well. but below is a simple example with different approach.
```yaml
# This is a SecretProviderClass example using aad-pod-identity to access the key vault
apiVersion: secrets-store.csi.x-k8s.io/v1
kind: SecretProviderClass
metadata:
  name: azure-kvname-podid
spec:
  provider: azure
  parameters:
    usePodIdentity: "true"               # Set to true for using aad-pod-identity to access your key vault
    keyvaultName: <key-vault-name>       # Set to the name of your key vault
    cloudName: ""                        # [OPTIONAL for Azure] if not provided, the Azure environment defaults to AzurePublicCloud
    objects:  |
      array:
        - |
          objectName: secret1
          objectType: secret             # object types: secret, key, or cert
          objectVersion: ""              # [OPTIONAL] object versions, default to latest if empty
        - |
          objectName: key1
          objectType: key
          objectVersion: ""
    tenantId: <tenant-Id>                # The tenant ID of the key vault
```
- configuring it in a pod definition

```yaml
# This is a sample pod definition for using SecretProviderClass and aad-pod-identity to access the key vault
kind: Pod
apiVersion: v1
metadata:
  name: busybox-secrets-store-inline-podid
  labels:
    aadpodidbinding: <name>                   # Set the label value to the name of your pod identity
spec:
  containers:
    - name: busybox
      image: k8s.gcr.io/e2e-test-images/busybox:1.29-1
      imagePullPolicy: Always
      lifecycle:
            postStart:
              exec:
                command:  # incase if we need to do something after pod created and udates something
                  - "sh"
                  - "-c"
                  - >
                    mkdir /path/log;
                    cp /temp/conf-app.properties /etc/conf/myapp/properties/my-app.properties;
                    token=`head -n 1 /mnt/key-vaut/secrets-store/<put-the-key-of-the-token` && sed -ci s/<place-holder-property-name>/${token//\//\\/}/ etc/conf/myapp/properties/my-app.properties;

      volumeMounts:
      - name: secrets-store01-inline    #name to match the volumes name
        mountPath: "/mnt/key-vaut/secrets-store"
        readOnly: true
  volumes:
    - name: secrets-store01-inline
      csi:
        driver: secrets-store.csi.k8s.io
        readOnly: true
        volumeAttributes:
          secretProviderClass: "azure-kvname-podid"
```
More options refere

[Reference 0](https://docs.microsoft.com/en-us/azure/aks/csi-secrets-store-identity-access)
[Reference 1](https://docs.microsoft.com/en-us/azure/aks/csi-secrets-store-driver)
