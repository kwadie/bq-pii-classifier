package com.google.cloud.pso.bq_security_classifier.services;

import com.google.cloud.tasks.v2.OidcToken;
import com.google.cloud.tasks.v2.Task;

public interface CloudTasksService {

    public Task submitTask(Task task, String queuePath);
}
