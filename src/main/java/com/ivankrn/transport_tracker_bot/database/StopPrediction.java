package com.ivankrn.transport_tracker_bot.database;

import lombok.Data;

@Data
public class StopPrediction {
    private final int stopId;
    private final int transportId;
    private final int route;
    private final int distanceToStop;
    private final int stopsCount;
}
