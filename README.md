# Readme

## Screenshots

### Kubernetes Pods running

![](https://github.com/thejasm/task-master-kube/blob/master/Screenshots/kubectl.png?raw=true)

Here my name is present as well as the time and date of the screenshot.
Notice the terminal at the bottom, which displays the running kubernetes pods and services.

### Execution of the program

![](https://github.com/thejasm/task-master-kube/blob/master/Screenshots/execution.png?raw=true)

Here my name is present as well as the time and date of the screenshot.
At the terminal at the bottom you can see that the delete, create, and execute functions are running.

## Task Execution using pods

In **KubeServices.java**, I use the PodBuilder object with a busybox image to quickly and efficiently build the task pods.

```
public class KubeService {
    public String run(String cmd) {
        try (KubernetesClient k8s = new DefaultKubernetesClient()) {
            String podName = "task-run-" + UUID.randomUUID().toString().substring(0, 8);
            String ns = "default";

            Pod pod = new PodBuilder()
                .withNewMetadata().withName(podName).endMetadata()
                .withNewSpec()
                    .withContainers(new ContainerBuilder()
                        .withName("runner")
                        .withImage("busybox:latest")
                        .withCommand("sh", "-c", cmd)
                        .build())
                    .withRestartPolicy("Never")
                .endSpec()
                .build();

            k8s.pods().inNamespace(ns).create(pod);

            k8s.pods().inNamespace(ns).withName(podName)
                .waitUntilCondition(p -> "Succeeded".equals(p.getStatus().getPhase()) || "Failed".equals(p.getStatus().getPhase()), 5, TimeUnit.MINUTES);

            String logs = k8s.pods().inNamespace(ns).withName(podName).getLog();
            
            k8s.pods().inNamespace(ns).withName(podName).delete();
            return logs;
        } catch (Exception e) {
            return "Execution failed: " + e.getMessage();
        }
    }
}
```


## Prerequisites

- Java 17+
- Maven
- MongoDB
- Kubernetes
- Docker
  
## How to run

### 1. Build the Docker Image

Open a terminal in the project's root directory and run:
```
docker build -t task-master:latest .
```
This creates a local Docker image named `task-master` with the tag `latest`

### 2. Deploy MongoDB
Run:
```
kubectl apply -f mongodb.yaml
```
Wait 30 seconds then check if the pod is running:
```
kubectl get pods
```
You should see a pod with a name like `task-mongo-deployment` and status of `Running`

### 3. Deploy the Task Master App
Run:
```
kubectl apply -f app-deployment.yaml
```
verify the pods are running:
```
kubectl get pods
```
You should see two pods, one for task-mongo and one for task-master, both with the `Running` status.
Now, check if the services are running:
```
kubectl get svc
```
you should see something like this:
```
NAME              TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)          AGE
kubernetes        ClusterIP   10.96.0.1        <none>        443/TCP          2d23h
task-master-svc   NodePort    10.100.211.192   <none>        8080:30080/TCP   10m
task-mongo-svc    ClusterIP   10.111.163.209   <none>        27017/TCP        13m
```
Notice the ports for task-master. You will be accessing using the NodePort `30080`

## API Examples

Here are examples of the programs functions using curl.

 ### 1. Create a Task (PUT)

This will create a new task. The ID is 123.

Request:
```
curl -X PUT http://localhost:30080/tasks \
-H "Content-Type: application/json" \
-d '{
    "id": "123",
    "name": "Print Hello",
    "owner": "admin",
    "command": "echo Hello World!"
}'
```

Response (Example):
```
{
  "id": "123",
  "name": "Print Hello",
  "owner": "admin",
  "command": "echo Hello World!",
  "taskExecutions": []
}
```

### 2. Get All Tasks (GET)

This returns a list of all tasks in the database.

Request:
```
curl http://localhost:30080/tasks
```

Response (Example):
```
[
  {
    "id": "123",
    "name": "Print Hello",
    "owner": "admin",
    "command": "echo Hello World!",
    "taskExecutions": []
  }
]
```

### 3. Get Single Task by ID (GET)

Request:
```
curl "http://localhost:30080/tasks?id=123"
```

Response (Example):
```
{
  "id": "123",
  "name": "Print Hello",
  "owner": "admin",
  "command": "echo Hello World!",
  "taskExecutions": []
}
```

### 4.  Find Tasks by Name (GET)

Request:
```
curl "http://localhost:30080/tasks/find?name=Print"
```

Response (Example):
```
[
  {
    "id": "123",
    "name": "Print Hello",
    "owner": "admin",
    "command": "echo Hello World!",
    "taskExecutions": []
  }
]
```

### 5.  Execute a Task (PUT)

This executes the command for task 123.

Request:
```
curl -X PUT http://localhost:30080/tasks/123/execute
```

Response (Example):
```
{
  "id": "123",
  "name": "Print Hello",
  "owner": "admin",
  "command": "echo Hello World!",
  "taskExecutions": [
    {
      "startTime": "2025-11-14T02:34:10.123Z",
      "endTime": "2025-11-14T02:34:15.456Z",
      "output": "Hello World!\n"
    }
  ]
}
```

### 6.  Delete a Task (DELETE)

This deletes the task with the given ID.

Request:
```
curl -v -X DELETE http://localhost:30080/tasks/123
```

Response (Example):
```
< HTTP/1.1 204 No Content
```
