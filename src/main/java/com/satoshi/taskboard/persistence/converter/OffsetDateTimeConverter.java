package com.satoshi.taskboard.persistence.converter;

import static java.time.ZoneOffset.UTC;
import static lombok.AccessLevel.PRIVATE;
import static java.util.Objects.nonNull;

import java.sql.Timestamp;
import java.time.OffsetDateTime;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public final class OffsetDateTimeConverter {

    public static OffsetDateTime toOffsetDateTime(final Timestamp value){
        return nonNull(value) ? OffsetDateTime.ofInstant(value.toInstant(), UTC) : null;
    }
}