package com.ivankrn.transport_tracker_bot.database;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name = "stop")
public class Stop {
    @Id
    private long id;

    @Column(nullable = false)
    @NotBlank
    private String name;

    @NotNull
    @Enumerated(EnumType.ORDINAL)
    private Type type;

    public enum Type {
        BUS, TRAM;
    }
}
