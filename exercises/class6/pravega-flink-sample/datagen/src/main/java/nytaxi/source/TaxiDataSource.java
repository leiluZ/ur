/*
 * Copyright (c) 2017 Dell Inc., or its subsidiaries. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 */

package nytaxi.source;

import io.pravega.client.ClientConfig;
import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.stream.EventRead;
import io.pravega.client.stream.EventStreamReader;
import io.pravega.client.stream.ReaderConfig;
import io.pravega.client.stream.ReinitializationRequiredException;
import io.pravega.client.stream.impl.UTF8StringSerializer;
import nytaxi.common.TripRecord;
import nytaxi.common.ZoneLookup;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

import static nytaxi.common.Constants.DEFAULT_CONTROLLER_URI;
import static nytaxi.common.Constants.DEFAULT_SCOPE;

public class TaxiDataSource implements SourceFunction<TripRecord> {

    private final Map<Integer, ZoneLookup> zoneLookupRecordMap;
    private final long speedup = 1000L;

    public TaxiDataSource(Map<Integer, ZoneLookup> zoneLookupRecordMap) {
        this.zoneLookupRecordMap = zoneLookupRecordMap;
    }

    @Override
    public void run(SourceContext<TripRecord> sourceContext) throws Exception {

        final String readerGroup = UUID.randomUUID().toString().replace("-", "");
        try (EventStreamClientFactory clientFactory = EventStreamClientFactory.withScope(DEFAULT_SCOPE,
                ClientConfig.builder().controllerURI(URI.create(DEFAULT_CONTROLLER_URI)).build());
             EventStreamReader<String> reader = clientFactory.createReader("reader",
                     readerGroup,
                     new UTF8StringSerializer(),
                     ReaderConfig.builder().build())) {
            int count = 0;
            String line;
            TripRecord tripRecord;
            boolean start = true;
            int rideId = 1;
            long startTime = System.nanoTime();
            EventRead<String> event = null;
            do {
                if (start) {
                    start = false;
                    continue;
                }

                try {
                    event = reader.readNextEvent(2000);
                    if (event.getEvent() != null) {
                        System.out.format("Read event '%s'%n", event.getEvent());
                    }
                    line = event.getEvent();
                } catch (ReinitializationRequiredException e) {
                    //There are certain circumstances where the reader needs to be reinitialized
                    e.printStackTrace();
                    continue;
                }

                // read first ride
                tripRecord = TripRecord.parse(line);
                tripRecord.setRideId(rideId++);
                ZoneLookup startLocZoneLookup = zoneLookupRecordMap.get(tripRecord.getStartLocationId());
                ZoneLookup destLocZoneLookup = zoneLookupRecordMap.get(tripRecord.getDestLocationId());
                tripRecord.setStartLocationBorough(startLocZoneLookup.getBorough());
                tripRecord.setStartLocationZone(startLocZoneLookup.getZone());
                tripRecord.setStartLocationServiceZone(startLocZoneLookup.getServiceZone());
                tripRecord.setDestLocationBorough(destLocZoneLookup.getBorough());
                tripRecord.setDestLocationZone(destLocZoneLookup.getZone());
                tripRecord.setDestLocationServiceZone(destLocZoneLookup.getServiceZone());
                sourceContext.collect(tripRecord);
                count++;

                if ((long) count >= speedup) {
                    long endTime = System.nanoTime();
                    for (long diff = endTime - startTime; diff < 1000000000L; diff = endTime - startTime) {
                        Thread.sleep(1L);
                        endTime = System.nanoTime();
                    }
                    startTime = endTime;
                    count = 0;
                }
            } while (event.getEvent() != null);

        }
    }

    @Override
    public void cancel() {
    }

}
