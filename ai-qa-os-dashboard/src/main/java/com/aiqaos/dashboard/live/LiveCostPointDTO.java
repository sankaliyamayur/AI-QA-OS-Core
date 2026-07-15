package com.aiqaos.dashboard.live;

import java.time.LocalDateTime;

public class LiveCostPointDTO {
    private String provider;
    private String model;
    private double cost;
    private LocalDateTime timestamp;

    public LiveCostPointDTO(String provider, String model, double cost, LocalDateTime timestamp) {
        this.provider = provider;
        this.model = model;
        this.cost = cost;
        this.timestamp = timestamp;
    }

    public String getProvider() { return provider; }
    public String getModel() { return model; }
    public double getCost() { return cost; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
