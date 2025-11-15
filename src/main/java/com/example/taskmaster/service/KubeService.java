package com.example.taskmaster.service;

import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
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