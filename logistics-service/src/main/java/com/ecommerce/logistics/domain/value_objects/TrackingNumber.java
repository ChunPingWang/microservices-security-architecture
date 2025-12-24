package com.ecommerce.logistics.domain.value_objects;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Value object representing a shipment tracking number.
 * Generates unique tracking numbers with carrier prefix and date.
 */
public final class TrackingNumber {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

    private final String value;

    private TrackingNumber(String value) {
        this.value = Objects.requireNonNull(value, "Tracking number value must not be null");
    }

    /**
     * Creates a TrackingNumber from an existing value.
     */
    public static TrackingNumber of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Tracking number cannot be null or blank");
        }
        return new TrackingNumber(value);
    }

    /**
     * Generates a new tracking number for the given carrier.
     */
    public static TrackingNumber generate(Carrier carrier) {
        Objects.requireNonNull(carrier, "Carrier must not be null");

        String prefix = getCarrierPrefix(carrier);
        String datePart = LocalDate.now().format(DATE_FORMAT);
        int sequence = SEQUENCE.incrementAndGet() % 10000;

        String trackingNumber = String.format("%s%s%04d", prefix, datePart, sequence);
        return new TrackingNumber(trackingNumber);
    }

    private static String getCarrierPrefix(Carrier carrier) {
        return switch (carrier) {
            case BLACK_CAT -> "BC";
            case HSINCHU_LOGISTICS -> "HC";
            case SEVEN_ELEVEN -> "SE";
            case FAMILY_MART -> "FM";
            case HI_LIFE -> "HL";
            case POST_OFFICE -> "PO";
            case SF_EXPRESS -> "SF";
        };
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrackingNumber that = (TrackingNumber) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
