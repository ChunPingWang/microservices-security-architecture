package com.ecommerce.logistics.domain.ports;

import com.ecommerce.logistics.domain.value_objects.Carrier;
import com.ecommerce.logistics.domain.value_objects.DeliveryStatus;
import com.ecommerce.shared.domain.value_objects.Address;

import java.time.Instant;
import java.util.List;

/**
 * Port for integrating with external logistics providers.
 */
public interface LogisticsProviderPort {

    /**
     * Creates a shipment with the logistics provider.
     */
    CreateShipmentResult createShipment(CreateShipmentRequest request);

    /**
     * Gets the current tracking status from the provider.
     */
    TrackingResult getTrackingStatus(String trackingNumber, Carrier carrier);

    /**
     * Request to create a shipment with a logistics provider.
     */
    record CreateShipmentRequest(
            String orderId,
            Address pickupAddress,
            Address deliveryAddress,
            Carrier carrier,
            double weightKg,
            String notes
    ) {}

    /**
     * Result of creating a shipment.
     */
    record CreateShipmentResult(
            boolean success,
            String trackingNumber,
            String errorMessage,
            Instant estimatedPickupDate
    ) {
        public static CreateShipmentResult success(String trackingNumber, Instant estimatedPickupDate) {
            return new CreateShipmentResult(true, trackingNumber, null, estimatedPickupDate);
        }

        public static CreateShipmentResult failure(String errorMessage) {
            return new CreateShipmentResult(false, null, errorMessage, null);
        }
    }

    /**
     * Result of tracking query.
     */
    record TrackingResult(
            boolean success,
            DeliveryStatus status,
            List<TrackingEventInfo> events,
            Instant estimatedDeliveryDate,
            String errorMessage
    ) {
        public static TrackingResult success(DeliveryStatus status,
                                              List<TrackingEventInfo> events,
                                              Instant estimatedDeliveryDate) {
            return new TrackingResult(true, status, events, estimatedDeliveryDate, null);
        }

        public static TrackingResult failure(String errorMessage) {
            return new TrackingResult(false, null, List.of(), null, errorMessage);
        }
    }

    /**
     * Tracking event information from provider.
     */
    record TrackingEventInfo(
            String description,
            String location,
            Instant timestamp
    ) {}
}
