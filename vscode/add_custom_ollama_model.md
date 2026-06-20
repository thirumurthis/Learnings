With the latest version of VS code we can add models running in local docker container in this case

- ollama is deployed in the docker and accessible using `http://localhost:11434/`

- Open VScode (make sure to use latest version)

- select Ctrl + Shift + p

- Type Chat , select Manage Language Models

<img width="1352" height="516" alt="image" src="https://github.com/user-attachments/assets/c684ef53-d5db-4d8e-b4b2-5f8e6ae91bbb" />

- Select the Add Models, select the ollama, provide a name, add the endpoint `http://localhost:11434`.
  If the docker instance is runnign with the model, then we should see list of model info like in below screen shoot

<img width="2390" height="622" alt="image" src="https://github.com/user-attachments/assets/2487ea72-935d-4f08-be95-f6807cc8f4f0" />

- After adding the model in the chat session we will see below message
This is the message to indicate that the utility models are not configured
These are models used by various features by the VScode.
<img width="686" height="1674" alt="image" src="https://github.com/user-attachments/assets/09804085-302e-49fe-a3e5-26a84a7b4d77" />

- From the "Set BYOK utlity models", click the `configure` button
should see the screen like below

<img width="2296" height="896" alt="image" src="https://github.com/user-attachments/assets/e58b2db4-a8fb-45c4-8ddd-987dc384c958" />

<img width="2282" height="1062" alt="image" src="https://github.com/user-attachments/assets/5bb799be-88b4-4073-a831-86b966f2a9d0" />

After configuring the BYOK, then the model will be listed in the chat session like below

<img width="842" height="1638" alt="image" src="https://github.com/user-attachments/assets/c0009816-659c-469c-9a47-360e0aa9f404" />

- In the chat session ask a question for response, make sure the model is running in this case used `docker exec -it ollama bash` and then in the shell, issued `ollama run llama:3.2`
 
<img width="938" height="1604" alt="image" src="https://github.com/user-attachments/assets/00969757-1a2d-4192-8153-36bdc3795ddd" />



 
