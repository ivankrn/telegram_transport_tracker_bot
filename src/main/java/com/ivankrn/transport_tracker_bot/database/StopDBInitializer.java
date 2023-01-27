package com.ivankrn.transport_tracker_bot.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

@Slf4j
@Service
public class StopDBInitializer implements CommandLineRunner {

    @Autowired
    StopRepository stopRepository;
    @Autowired
    TransportParser transportParser;

    @Override
    public void run(String... args) throws Exception {
        long stopsCount = StreamSupport.stream(stopRepository.findAll().spliterator(), false).count();
        if (stopsCount == 0) {
            log.info("Stop table is empty, beginning filling.");
            List<Stop> stops = transportParser.getAllStops();
            stopRepository.saveAll(stops);
        } else {
            log.info("Stop table is not empty, passing filling.");
        }
    }
}
