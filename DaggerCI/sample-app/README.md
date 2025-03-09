## commands

dagger init

dagger develop --sdk=java

dagger -m .dagger/backend call build --source .

-- terminal into container
dagger -m .dagger/backend call copy --directoryArg . terminal

-- build and copy the jar from the container
dagger -m .dagger/backend call build-jar --source . export --path=sample-app-1.0.0.jar


-- to run the build as service
dagger -m .dagger/backend call run --source . up --ports 8081:80

-- to view the option of the run function we wrote

dagger -m .dagger/backend call run --help

-- note the run function uses the entry point configured 
dagger -m .dagger/backend call run --source . up --ports 8081:8080

-- if everything works, a tunnel should be created and can access the application