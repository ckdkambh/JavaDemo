package com.example.demo;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SlidingTimeWindowArrayReservoir;
import com.codahale.metrics.SlidingTimeWindowReservoir;
import lombok.val;
import org.apache.tomcat.jni.Time;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class DemoApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void testTopN() throws InterruptedException {
        System.out.println("--------------------");
        MetricRegistry registry = MetricRegistryFactory.getRegistry();

        SlidingTimeWindowReservoir slidingTimeWindowReservoir = new SlidingTimeWindowReservoir(3, TimeUnit.SECONDS);
        slidingTimeWindowReservoir.update(6);
        slidingTimeWindowReservoir.update(7);
        slidingTimeWindowReservoir.update(6);
        slidingTimeWindowReservoir.update(6);
        slidingTimeWindowReservoir.update(8);
        slidingTimeWindowReservoir.update(6);
        slidingTimeWindowReservoir.update(7);

        Thread.sleep(1000);
        slidingTimeWindowReservoir.update(7);
        slidingTimeWindowReservoir.update(6);
        slidingTimeWindowReservoir.update(6);
        slidingTimeWindowReservoir.update(8);
        slidingTimeWindowReservoir.update(6);
        slidingTimeWindowReservoir.update(7);
        System.out.println("wqqqq");
//        slidingTimeWindowReservoir.getSnapshot().dump(System.out);
        System.out.println("--------------------");

        Thread.sleep(1000);
        slidingTimeWindowReservoir.update(3);
        slidingTimeWindowReservoir.update(3);
        slidingTimeWindowReservoir.update(3);
        slidingTimeWindowReservoir.update(3);
        slidingTimeWindowReservoir.update(4);
        slidingTimeWindowReservoir.update(5);
        Thread.sleep(1000);
        Arrays.stream(slidingTimeWindowReservoir.getSnapshot().getValues());
    }
}
