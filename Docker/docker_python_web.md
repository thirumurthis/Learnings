##### Sample Python web project using Flask and redis (a `Statefull`) example:

##### `webapp.py` content:
```python
import time
import redis

from flask import Flask

app = Flask(__name__)
cache = redis.Redis(host='redis', port=6379)

def get_hit_count():
    retries = 5
    while True:
        try:
            return cache.incr('hits')
        except redis.exceptions.ConnectionError as exc:
            if retries == 0:
                raise exc
            retries -= 1
            time.sleep(0.5)

@app.route('/')
def hello():
    count = get_hit_count()
    return 'Hello World! hits {} times.\n'.format(count)

if __name__ == "__main__":
    app.run(host="0.0.0.0",debug=True)
```

##### `requirement.txt` content, this file is used to determine the dependent package, since we python packaging `pip` needs this.
```
flask
redis
```

Also version specific values can be loaded. Like 
```
#Flask Framework
Flask==1.0.2

Flask==1.0.2
Flask-Migrate==2.0.2
Flask-Script==2.0.5
Flask-SQLAlchemy==2.4.0
Flask-WTF==0.14.2
Flask-User==1.0.1.5
## redis info should include
```

##### `Dockerfile` content:
```
FROM python
ADD . /code
WORKDIR /code
## python packaging 
RUN pip install -r requirements.txt
CMD ["python", "mywebapp.py"]
```

#### To run the above python project.
```
docker run -p 5000:5000 -d <image-id>
```

##### `docker-compose.yml` file content:
```yaml
version: '3'
services:
     web:
       build: .
       ports:
          - "5000:5000"
       volumes:
          - .:/code     
     redis:
       image: "redis:alpine"
```

#### once this image is pushed to docker hub.
 ` $ docker push <image-id> `
