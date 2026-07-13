package com.aiqaos.execution.component;

import com.aiqaos.core.contract.ExecutionRequest;
import org.springframework.stereotype.Component;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.Comparator;

@Component
public class ExecutionQueue {
    private final PriorityBlockingQueue<ExecutionRequest> queue = new PriorityBlockingQueue<>(
        11, Comparator.comparing(req -> req.getMetadata().getTimestamp())
    );

    public void enqueue(ExecutionRequest request) {
        if (request != null) {
            queue.offer(request);
        }
    }

    public ExecutionRequest dequeue() {
        return queue.poll();
    }

    public int size() {
        return queue.size();
    }
}
