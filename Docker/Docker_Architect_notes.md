`Docker workflow`:

   - Client Host
      - Where the command are issued like pulling, pushing the image.
      - These commands can be issed from CICD pipleine, Dev machine during development, etc.
      - command issued using docker cli, installed in docker host.
      
   - Server Host
      - The images are downloaded and containers are run.
      - There are multiple server host to make the application `highly avialable`.
      - Each server host has `Docker Engine` installed that is listening for commands.
      
   - Registry 
      - Stateless highly scalable server side application
          - stores the docker images.
          - also distributes docker images.
      - This can be hosted using jFrog artifactory (on-prem or inside firewal), AWS Elastic container registry (cloud)
      
      
      1. Assume the developed image is developed and uploded to the registry.
      2. Developer issued command, `$ docker container run` using docker CLI.
      3. The Docker CLI translates this command to a REST request to `Docker Engine` on the server host.
      4. Docker Enginer checks if the image is available in the server host.
      5. If the image already exists, it runs the container.
      6. If the image is not available, it is downloaded from the preconfigured registry.
      7. The image is stored in the server host unless ther is a new version.
      
      
      
