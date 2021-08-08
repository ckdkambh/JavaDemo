package com.example.demo;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.SlidingTimeWindowReservoir;
import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * SlidingTimeWindowReservoir用于实现滑动计数器，实现TopN错误码和平均时延
 * 通过继承实现获取一分钟前数据的功能
 */
@Data
@Component
class PredictMetric implements InitializingBean {
    private String asynRetCode = "6|7|8";

    private Integer period = 60;

    private Map<String, Histogram> successMap = new ConcurrentHashMap<>();

    private Map<String, Histogram> failMap = new ConcurrentHashMap<>();

    private Map<String, Histogram> retCodeMap = new ConcurrentHashMap<>();

    private Map<String, Histogram> delayMap = new ConcurrentHashMap<>();

    private Set<Integer> successCodeSet;

    private static String getDesc(String modelName, String modelVersion, String flowId) {
        return modelName + "@" + modelVersion + "@" + flowId;
    }

    public static String getModelInfoByDesc(String desc) {
        String[] infos = desc.split("@");
        return infos[0] + infos[1];
    }

    public static String getSceneInfoByDesc(String desc) {
        return desc.split("@")[2];
    }

    private void increaseSuccess(String modelName, String modelVersion, String flowId) {
        String desc = getDesc(modelName, modelVersion, flowId);
        if (!successMap.containsKey(desc)) {
            Histogram histogram = new Histogram(new SlidingTimeWindowReservoir(period, TimeUnit.SECONDS));
            successMap.put(desc, histogram);
            MetricRegistryFactory.getRegistry().register(desc, histogram);
        }
        successMap.get(desc).update(1);
    }

    private void increaseFail(String modelName, String modelVersion, String flowId) {
        String desc = getDesc(modelName, modelVersion, flowId);
        if (!failMap.containsKey(desc)) {
            Histogram histogram = new Histogram(new SlidingTimeWindowReservoir(period, TimeUnit.SECONDS));
            failMap.put(desc, histogram);
            MetricRegistryFactory.getRegistry().register(desc, histogram);
        }
        failMap.get(desc).update(1);
    }

    private void addFailCode(String modelName, String modelVersion, String flowId, int code) {
        String desc = getDesc(modelName, modelVersion, flowId);
        if (!retCodeMap.containsKey(desc)) {
            Histogram histogram = new Histogram(new SlidingTimeWindowReservoir(period, TimeUnit.SECONDS));
            retCodeMap.put(desc, histogram);
            MetricRegistryFactory.getRegistry().register(desc, histogram);
        }
        retCodeMap.get(desc).update(code);
    }

    public void updateResultMetric(String modelName, String modelVersion, String flowId, int code) {
        if (successCodeSet.contains(code)) {
            increaseSuccess(modelName, modelVersion, flowId);
        } else {
            increaseFail(modelName, modelVersion, flowId);
            addFailCode(modelName, modelVersion, flowId, code);
        }
    }

    public void updateDelayMetric(String modelName, String modelVersion, String flowId, long delay) {
        String desc = getDesc(modelName, modelVersion, flowId);
        if (!delayMap.containsKey(desc)) {
            Histogram histogram = new Histogram(new SlidingTimeWindowReservoir(period, TimeUnit.SECONDS));
            delayMap.put(desc, histogram);
            MetricRegistryFactory.getRegistry().register(desc, histogram);
        }
        delayMap.get(desc).update(delay);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        successCodeSet = Arrays.stream(asynRetCode.split("\\|")).map(Integer::parseInt).collect(Collectors.toSet());
        successCodeSet.add(0);
    }
}
