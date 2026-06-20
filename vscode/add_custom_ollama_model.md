With the latest version of VS code we can add models running in local docker container in this case

- ollama is deployed in the docker and accessible using `http://localhost:11434/`

- Open VScode (make sure to use latest version)

- select Ctrl + Shift + p

- Type Chat , select Manage Language Models

<img width="1352" height="516" alt="image" src="https://github.com/user-attachments/assets/c684ef53-d5db-4d8e-b4b2-5f8e6ae91bbb" />

- Select the Add Models, select the ollama, provide a name, add the endpoint `http://localhost:11434`.
  If the docker instance is runnign with the model, then we should see list of model info like in below screen shoot

<img width="2390" height="622" alt="image" src="https://github.com/user-attachments/assets/2487ea72-935d-4f08-be95-f6807cc8f4f0" />


 
