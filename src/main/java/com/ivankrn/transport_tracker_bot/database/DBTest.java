package com.ivankrn.transport_tracker_bot.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class DBTest implements CommandLineRunner {
    @Autowired
    StopRepository stopRepository;
    @Autowired
    TransportParser transportParser;

    @Override
    public void run(String[] args) {
    }
}
