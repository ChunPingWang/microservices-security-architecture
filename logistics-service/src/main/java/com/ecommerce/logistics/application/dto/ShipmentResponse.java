package com.ecommerce.logistics.application.dto;

import com.ecommerce.logistics.domain.aggregates.Shipment;
import com.ecommerce.logistics.domain.value_objects.TrackingEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for shipment information.
 */
public record ShipmentResponse(
        UUID id,
        UUID orderId,
        UUID customerId,
        String trackingNumber,
        String carrier,
        String carrierDisplayName,
        String carrierTrackingUrl,
        String status,
        String statusDisplayName,
        DeliveryAddressResponse deliveryAddress,
        List<TrackingEventResponse> trackingEvents,
        Instant estimatedDeliveryDate,
        Instant createdAt,
        Instant shippedAt,
        Instant deliveredAt
) {
    public static ShipmentResponse from(Shipment shipment) {
        return new ShipmentResponse(
                shipment.getId(),
                shipment.getOrderId(),
                shipment.getCustomerId(),
                shipment.getTrackingNumber().getValue(),
                shipment.getCarrier().name(),
                shipment.getCarrier().getDisplayName(),
                shipment.getCarrierTrackingUrl(),
                shipment.getStatus().name(),
                shipment.getStatus().getDisplayName(),
                DeliveryAddressResponse.from(shipment.getDeliveryAddress()),
                shipment.getTrackingEvents().stream()
                        .map(TrackingEventResponse::from)
                        .toList(),
                shipment.getEstimatedDeliveryDate(),
                shipment.getCreatedAt(),
                shipment.getShippedAt(),
                shipment.getDeliveredAt()
        );
    }

    public record DeliveryAddressResponse(
            String street,
            String city,
            String district,
            String postalCode,
            String country,
            String recipientName,
            String phoneNumber,
            String fullAddress
    ) {
        public static DeliveryAddressResponse from(com.ecommerce.shared.domain.value_objects.Address address) {
            return new DeliveryAddressResponse(
                    address.getStreet(),
                    address.getCity(),
                    address.getDistrict(),
                    address.getPostalCode(),
                    address.getCountry(),
                    address.getRecipientName(),
                    address.getPhoneNumber(),
                    address.getFullAddress()
            );
        }
    }

    public record TrackingEventResponse(
            UUID id,
            String description,
            String location,
            Instant timestamp
    ) {
        public static TrackingEventResponse from(TrackingEvent event) {
            return new TrackingEventResponse(
                    event.id(),
                    event.description(),
                    event.location(),
                    event.timestamp()
            );
        }
    }
}
