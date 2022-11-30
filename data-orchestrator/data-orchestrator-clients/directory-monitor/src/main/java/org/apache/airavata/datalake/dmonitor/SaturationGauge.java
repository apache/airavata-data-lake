/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.airavata.datalake.dmonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class SaturationGauge {

    private final static Logger logger = LoggerFactory.getLogger(SaturationGauge.class);

    public Map<String, Long> directorySizes = new ConcurrentHashMap<>();
    public Map<String, Integer> monitorCount = new ConcurrentHashMap<>();
    final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    final ExecutorService monitoringService = Executors.newFixedThreadPool(10);

    private int saturationCount = 3;

    public void start(EventNotifier eventNotifier) {
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            List<Future<Boolean>> submitFutures = new ArrayList<>();
            for (String key : directorySizes.keySet()) {
                Future<Boolean> submitFuture = monitoringService.submit(() -> {

                    monitorCount.put(key, monitorCount.get(key) + 1);

                    try {
                        long oldSize = directorySizes.get(key);
                        long newSize = getFolderSize(new File(key));
                        directorySizes.put(key, newSize);
                        logger.info("Directory : " + key + " Size : " + newSize + " Scan count : " + monitorCount.get(key));

                        if (oldSize == newSize && monitorCount.get(key) > saturationCount) {
                            logger.info("Directory " + key + " is saturated. Final size " + oldSize);
                            monitorCount.remove(key);
                            directorySizes.remove(key);
                            eventNotifier.notify(key);
                        }

                        if (oldSize != newSize) {
                            monitorCount.put(key, 0);
                        }

                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (monitorCount.get(key) > 3) {
                            monitorCount.remove(key);
                            directorySizes.remove(key);
                        }
                        return false;
                    }
                });

                submitFutures.add(submitFuture);
            }

            for (Future<Boolean> submitFuture: submitFutures) {
                try {
                    submitFuture.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            logger.debug("All monitor threads were completed");
        }, 1, 20, TimeUnit.SECONDS);
    }

    public void monitorSaturation(File folder) {
        if (! directorySizes.containsKey(folder.getAbsolutePath())) {
            directorySizes.put(folder.getAbsolutePath(), 0L);
            monitorCount.put(folder.getAbsolutePath(), 0);
        }
    }

    private long getFolderSize(File folder) {
        long length = 0;
        File[] files = folder.listFiles();

        int count = files.length;

        for (int i = 0; i < count; i++) {
            if (files[i].isFile()) {
                length += files[i].length();
            }
            else {
                length += getFolderSize(files[i]);
            }
        }
        return length;
    }

    public int getSaturationCount() {
        return saturationCount;
    }

    public void setSaturationCount(int saturationCount) {
        this.saturationCount = saturationCount;
    }
}
