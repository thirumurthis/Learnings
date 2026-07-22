///usr/bin/env jbang "$0" "$0" : exit $?

//DEPS io.apicurio:apicurio-registry:3.2.1@pom
//DEPS io.apicurio:apicurio-registry-java-sdk
//DEPS io.apicurio:apicurio-registry-common
//DEPS org.slf4j:slf4j-jdk14

import io.apicurio.registry.client.common.DefaultVertxInstance;

import java.util.ArrayList;
import java.util.List;

import io.apicurio.registry.client.RegistryClientFactory;
import io.apicurio.registry.client.common.RegistryClientOptions;
import io.apicurio.registry.rest.client.RegistryClient;
import io.apicurio.registry.rest.client.models.ArtifactMetaData;
import io.apicurio.registry.rest.client.models.ArtifactSearchResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleAvroRegistryAcces {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleAvroRegistryAcces.class);

    private static final RegistryClient client;
    static {
        // Create a Service Registry client
        String registryUrl = "https://apicurio.app.demo.com/apis/registry/v3";
        client = createProperClient(registryUrl);
    }

    public static void main(String[] args) {
        // Register the JSON Schema schema in the Apicurio registry.
        // final String artifactId = "employee-info"; // UUID.randomUUID().toString();
        final String groupId = "default";

        try {
            // RegistryDemoUtil.createSchemaInServiceRegistry(client, artifactId,
            // Constants.SCHEMA);
            getSchemaFromRegistry(client, groupId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            // If we do not provide our own instance of Vertx, then we must close the
            // default instance that will get used.
            DefaultVertxInstance.close();
        }
    }

    public static RegistryClient createProperClient(String registryUrl) {
        final String tokenEndpoint = System.getenv("AUTH_TOKEN_ENDPOINT");
        boolean dontExecute = false;
        if (tokenEndpoint != null && dontExecute) {
            final String authClient = System.getenv("AUTH_CLIENT_ID");
            final String authSecret = System.getenv("AUTH_CLIENT_SECRET");
            return RegistryClientFactory.create(RegistryClientOptions.create(registryUrl)
                    .oauth2(tokenEndpoint, authClient, authSecret));
        } else {
            RegistryClientOptions clientOptions = RegistryClientOptions.create(registryUrl);
            boolean useCaCertFromSecret = false;
            if (useCaCertFromSecret) {
                clientOptions.trustStorePem("./caCert.crt");
            } else {
                // else extract from the browser option
                clientOptions.trustStorePem("./browserCaCert.crt");
            }
            return RegistryClientFactory.create(clientOptions);
        }
    }

    public static ArtifactMetaData getSchemaFromRegistry(RegistryClient service, String groupId) {

        LOGGER.info("---------------------------------------------------------");
        LOGGER.info("=====> Fetching artifact from the registry for JSON Schema with ID: {}", groupId);
        try {
            List<ArtifactMetaData> metaData = new ArrayList<>();
            ArtifactSearchResults result = service.groups().byGroupId(groupId).artifacts().get();
            result.getArtifacts().forEach(itm -> {
                LOGGER.info("ARTIFACT ID -----[{}] ", itm.getArtifactId());
                ArtifactMetaData metaDataInfo = service.groups().byGroupId(groupId).artifacts()
                        .byArtifactId(itm.getArtifactId()).get();
                assert metaDataInfo != null;
                metaData.add(metaDataInfo);
            });

            LOGGER.info("=====> Successfully fetched JSON Schema artifact in Service Registry: {}",
                    metaData.size() > 0 ? metaData.get(0) : null);
            LOGGER.info("---------------------------------------------------------");
            return metaData.size() > 0 ? metaData.get(0) : null;
        } catch (Exception t) {
            throw t;
        }
    }

}
