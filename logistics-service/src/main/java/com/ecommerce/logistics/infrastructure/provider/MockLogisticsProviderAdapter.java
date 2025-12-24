package com.ecommerce.logistics.infrastructure.provider;

import com.ecommerce.logistics.domain.ports.LogisticsProviderPort;
import com.ecommerce.logistics.domain.value_objects.Carrier;
import com.ecommerce.logistics.domain.value_objects.DeliveryStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Mock logistics provider adapter for development and testing.
 * Simulates integration with external logistics providers.
 */
@Component
public class MockLogisticsProviderAdapter implements LogisticsProviderPort {

    private static final Logger log = LoggerFactory.getLogger(MockLogisticsProviderAdapter.class);

    @Override
    public CreateShipmentResult createShipment(CreateShipmentRequest request) {
        log.info("Creating shipment with carrier: {} for order: {}",
                request.carrier(), request.orderId());

        // Simulate processing delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Generate mock tracking number
        String trackingNumber = generateTrackingNumber(request.carrier());
        Instant estimatedPickup = Instant.now().plus(1, ChronoUnit.DAYS);

        log.info("Shipment created with tracking number: {}", trackingNumber);
        return CreateShipmentResult.success(trackingNumber, estimatedPickup);
    }

    @Override
    public TrackingResult getTrackingStatus(String trackingNumber, Carrier carrier) {
        log.info("Getting tracking status for: {} from carrier: {}",
                trackingNumber, carrier);

        // Simulate processing delay
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Return mock tracking result
        List<TrackingEventInfo> events = List.of(
                new TrackingEventInfo("包裹已收件", "出貨倉庫", Instant.now().minus(2, ChronoUnit.DAYS)),
                new TrackingEventInfo("包裹運送中", "轉運中心", Instant.now().minus(1, ChronoUnit.DAYS)),
                new TrackingEventInfo("包裹已抵達配送站", "當地配送站", Instant.now())
        );

        Instant estimatedDelivery = Instant.now().plus(1, ChronoUnit.DAYS);

        return TrackingResult.success(DeliveryStatus.IN_TRANSIT, events, estimatedDelivery);
    }

    private String generateTrackingNumber(Carrier carrier) {
        String prefix = switch (carrier) {
            case BLACK_CAT -> "BC";
            case HSINCHU_LOGISTICS -> "HC";
            case SEVEN_ELEVEN -> "SE";
            case FAMILY_MART -> "FM";
            case HI_LIFE -> "HL";
            case POST_OFFICE -> "PO";
            case SF_EXPRESS -> "SF";
        };
        return prefix + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
    }
}
