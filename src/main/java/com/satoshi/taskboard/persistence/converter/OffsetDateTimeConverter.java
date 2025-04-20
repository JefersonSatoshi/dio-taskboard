package com.satoshi.taskboard.persistence.converter;

import java.sql.Timestamp;
import java.time.OffsetDateTime;

import lombok.NoArgsConstructor;
import static java.time.ZoneOffset.UTC;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class OffsetDateTimeConverter {

    public static OffsetDateTime toOffsetDateTime(final Timestamp value){
        return OffsetDateTime.ofInstant(value.toInstant(), UTC);
    }
}