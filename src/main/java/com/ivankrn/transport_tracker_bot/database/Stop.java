package com.ivankrn.transport_tracker_bot.database;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "stop")
public class Stop {
    @Id
    private long id;
    private String name;
    @Enumerated(EnumType.ORDINAL)
    private Type type;

    public enum Type {
        BUS, TRAM;
    }
}
