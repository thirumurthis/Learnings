///usr/bin/env jbang "$0" "$0" : exit $?

//DEPS io.apicurio:apicurio-registry:3.2.1@pom
//DEPS io.apicurio:apicurio-registry-java-sdk
//DEPS io.apicurio:apicurio-registry-common
//DEPS org.slf4j:slf4j-jdk14

import io.apicurio.registry.client.common.DefaultVertxInstance;

import java.util.UUID;

import io.apicurio.registry.client.RegistryClientFactory;
import io.apicurio.registry.client.common.RegistryClientOptions;
import io.apicurio.registry.rest.client.RegistryClient;
import io.apicurio.registry.rest.client.models.ArtifactMetaData;
import io.apicurio.registry.rest.client.models.ArtifactSearchResults;
import io.apicurio.registry.rest.client.models.CreateArtifact;
import io.apicurio.registry.rest.client.models.CreateVersion;
import io.apicurio.registry.rest.client.models.IfArtifactExists;
import io.apicurio.registry.rest.client.models.VersionContent;
import io.apicurio.registry.utils.IoUtil;
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
        final String artifactId = "employee-info"; // UUID.randomUUID().toString();
        final String groupId = "default";

        try {
            //RegistryDemoUtil.createSchemaInServiceRegistry(client, artifactId, Constants.SCHEMA);
            getSchemaFromRegistry(client, artifactId, groupId);
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
            clientOptions.trustStorePem("browserCertExport.crt");//"./caCert.crt");
            return RegistryClientFactory.create(clientOptions);
        }
    }

      public static ArtifactMetaData getSchemaFromRegistry(RegistryClient service, String artifactId, String groupId) {

        LOGGER.info("---------------------------------------------------------");
        LOGGER.info("=====> Fetching artifact from the registry for JSON Schema with ID: {}", artifactId);
        try {
            ArtifactSearchResults result = service.groups().byGroupId(groupId).artifacts().get();
            result.getArtifacts().forEach(itm -> {
              LOGGER.info("ARTIFACT ID ----- "+itm.getArtifactId());
            });
            final ArtifactMetaData metaData = service.groups().byGroupId(groupId).artifacts()
                    .byArtifactId(artifactId).get();
            LOGGER.info("fetched artifacts from registry");
            assert metaData != null;
            LOGGER.info("=====> Successfully fetched JSON Schema artifact in Service Registry: {}", metaData);
            LOGGER.info("---------------------------------------------------------");
            return metaData;
        } catch (Exception t) {
            throw t;
        }
    }
    
}
