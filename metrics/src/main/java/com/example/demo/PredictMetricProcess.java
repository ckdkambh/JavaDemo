package com.example.demo;

import lombok.val;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PredictMetricProcess implements InitializingBean {
    @Autowired
    private PredictMetric predictMetric;

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);

    private Integer period = 60;

    @Override
    public void afterPropertiesSet() throws Exception {
        executor.scheduleAtFixedRate(() -> {}, 0, period, TimeUnit.SECONDS);
    }

    private void calculate(){
        Set<String> descSet = predictMetric.getSuccessMap().keySet();
        descSet.addAll(predictMetric.getFailMap().keySet());

        Map<String, Long> modelSuccess = new HashMap<>();
        Map<String, Long> modelTotal = new HashMap<>();
        Map<String, Map<Long, Integer>> modelFailCode = new HashMap<>();
        Map<String, Long> sceneSuccess = new HashMap<>();
        Map<String, Long> sceneTotal = new HashMap<>();
        Map<String, Map<Long, Integer>> sceneFailCode = new HashMap<>();
        descSet.forEach(desc -> {
            String modelInfo = PredictMetric.getModelInfoByDesc(desc);
            String sceneInfo = PredictMetric.getSceneInfoByDesc(desc);

            Long successCount = Arrays.stream(predictMetric.getSuccessMap().get(desc).getSnapshot().getValues()).sum();
            Long failCount = Arrays.stream(predictMetric.getFailMap().get(desc).getSnapshot().getValues()).sum();
            modelSuccess.put(modelInfo, modelSuccess.getOrDefault(modelInfo, 0L) + successCount);
            modelTotal.put(modelInfo, modelTotal.getOrDefault(modelInfo, 0L) + successCount + failCount);
            sceneSuccess.put(sceneInfo, sceneSuccess.getOrDefault(sceneInfo, 0L) + successCount);
            sceneTotal.put(sceneInfo, sceneTotal.getOrDefault(sceneInfo, 0L) + successCount + failCount);

            Arrays.stream(predictMetric.getRetCodeMap().get(desc).getSnapshot().getValues())
                    .boxed().collect(Collectors.toMap(x -> x, y -> 1, Integer::sum))
                    .forEach((k, v) -> {
                if (!modelFailCode.containsKey(modelInfo)) {
                    modelFailCode.put(modelInfo, new HashMap<>());
                }
                modelFailCode.get(modelInfo).merge(k, v, Integer::sum);

                if (!sceneFailCode.containsKey(sceneInfo)) {
                    sceneFailCode.put(sceneInfo, new HashMap<>());
                }
                sceneFailCode.get(sceneInfo).merge(k, v, Integer::sum);
            });
            Long maxRetCode = modelFailCode.get(modelInfo).entrySet().stream().max(Comparator.comparingInt(Map.Entry::getValue)).get().getKey();
        });


    }
}
