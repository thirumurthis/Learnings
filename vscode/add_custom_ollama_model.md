With the latest version of VS code we can add models running in local docker container in this case

- ollama is deployed in the docker and accessible using `http://localhost:11434/`

<img width="676" height="254" alt="image" src="https://github.com/user-attachments/assets/b9e8bd20-56d2-43c1-b4b6-bdef7b709e5d" />

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

---
Recently VS code started supporting Any LLM Provider, for more info refer this video. With reference to the video, wanted to use the local ollama container with llama3.2 model in the VS code chat session. Below is the details of how to configure and interact like using the copilot chat.

Running ollama in docker
To run the ollama container in local, used below command, the ollama volume mounts to local file system from where the command is executed

docker run -e OLLAMA_KEEP_ALIVE=-1 -v ollama:/root/.ollama -d -p 11434:11434 --name ollama ollama/ollama
To download and run a model within the ollama container execute below command

docker exec -it ollama sh
From the container shell execute below command, the # is the shell on the container

# ollama run llama:3.2
Configuring ollama local model in VS Code
Make sure the VS code is up to date and ollama container deployed in the docker is accessible http://localhost:11434. From browser should see a response Ollama is running 

Open VScode, select Ctrl + Shift + p

Type Chat , select Manage Language Models

Minimize image
Edit image
Delete image

Chat option in VS code
Select the Add Models

Select the ollama

Provide a name

Next, add the endpoint http://localhost:11434 

  If the docker instance is running with the model, then we should see list of model info like in below screen shoot

Minimize image
Edit image
Delete image

Add models and selecting the ollama finally displays this info
After selecting the model, in the chat session we would see message like below. This message  indicates that the utility models are not configured, which is used by VS code itself for its features

Maximize image
Edit image
Delete image

configuring utility models after adding the models
Click the configure button on the "Set BYOK utility models" message on chat session, which would open a popup like below

Minimize image
Edit image
Delete image

utility model popup
Minimize image
Edit image
Delete image

select the models that are running in the ollama form the drop down list
After configuring the BYOK, we could see the selected model info in this case llama3.2 listed in the chat session like below

Maximize image
Edit image
Delete image

models supported are listed to be selected for chat
In the chat session we can ask a question to see if the selected model is working. Below screen shot shows the response 

Maximize image
Edit image
Delete image

response from the local ollama llama3.2 model


 

 
