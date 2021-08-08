package com.example.demo;

import com.codahale.metrics.MetricRegistry;
import lombok.Data;

@Data
public class MetricRegistryFactory {
    private static final MetricRegistry registry = new MetricRegistry();

    public static MetricRegistry getRegistry() {
        return registry;
    }
}
