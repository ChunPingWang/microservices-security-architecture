package com.ecommerce.logistics.unit.domain;

import com.ecommerce.logistics.domain.aggregates.Shipment;
import com.ecommerce.logistics.domain.value_objects.Carrier;
import com.ecommerce.logistics.domain.value_objects.DeliveryStatus;
import com.ecommerce.logistics.domain.value_objects.TrackingNumber;
import com.ecommerce.shared.domain.value_objects.Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Shipment aggregate.
 * Tests cover creation, status transitions, and tracking updates.
 */
@DisplayName("Shipment Aggregate")
class ShipmentTest {

    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static Address DELIVERY_ADDRESS;

    @BeforeEach
    void setUp() {
        DELIVERY_ADDRESS = Address.builder()
                .street("信義路五段7號")
                .city("台北市")
                .district("信義區")
                .postalCode("110")
                .country("台灣")
                .recipientName("王小明")
                .phoneNumber("0912345678")
                .build();
    }

    @Nested
    @DisplayName("Shipment Creation")
    class ShipmentCreation {

        @Test
        @DisplayName("should create shipment with valid data")
        void shouldCreateShipmentWithValidData() {
            Shipment shipment = Shipment.create(
                    ORDER_ID,
                    CUSTOMER_ID,
                    DELIVERY_ADDRESS,
                    Carrier.BLACK_CAT
            );

            assertNotNull(shipment.getId());
            assertEquals(ORDER_ID, shipment.getOrderId());
            assertEquals(CUSTOMER_ID, shipment.getCustomerId());
            assertEquals(DELIVERY_ADDRESS, shipment.getDeliveryAddress());
            assertEquals(Carrier.BLACK_CAT, shipment.getCarrier());
            assertEquals(DeliveryStatus.PENDING, shipment.getStatus());
            assertNotNull(shipment.getTrackingNumber());
            assertNotNull(shipment.getCreatedAt());
            assertNull(shipment.getShippedAt());
            assertNull(shipment.getDeliveredAt());
        }

        @Test
        @DisplayName("should generate unique tracking number")
        void shouldGenerateUniqueTrackingNumber() {
            Shipment shipment1 = Shipment.create(ORDER_ID, CUSTOMER_ID, DELIVERY_ADDRESS, Carrier.BLACK_CAT);
            Shipment shipment2 = Shipment.create(ORDER_ID, CUSTOMER_ID, DELIVERY_ADDRESS, Carrier.BLACK_CAT);

            assertNotEquals(shipment1.getTrackingNumber(), shipment2.getTrackingNumber());
        }

        @Test
        @DisplayName("should fail when order ID is null")
        void shouldFailWhenOrderIdIsNull() {
            assertThrows(NullPointerException.class, () ->
                    Shipment.create(null, CUSTOMER_ID, DELIVERY_ADDRESS, Carrier.BLACK_CAT)
            );
        }

        @Test
        @DisplayName("should fail when customer ID is null")
        void shouldFailWhenCustomerIdIsNull() {
            assertThrows(NullPointerException.class, () ->
                    Shipment.create(ORDER_ID, null, DELIVERY_ADDRESS, Carrier.BLACK_CAT)
            );
        }

        @Test
        @DisplayName("should fail when delivery address is null")
        void shouldFailWhenDeliveryAddressIsNull() {
            assertThrows(NullPointerException.class, () ->
                    Shipment.create(ORDER_ID, CUSTOMER_ID, null, Carrier.BLACK_CAT)
            );
        }

        @Test
        @DisplayName("should fail when carrier is null")
        void shouldFailWhenCarrierIsNull() {
            assertThrows(NullPointerException.class, () ->
                    Shipment.create(ORDER_ID, CUSTOMER_ID, DELIVERY_ADDRESS, null)
            );
        }
    }

    @Nested
    @DisplayName("Shipment Pickup")
    class ShipmentPickup {

        @Test
        @DisplayName("should mark shipment as picked up")
        void shouldMarkShipmentAsPickedUp() {
            Shipment shipment = Shipment.create(ORDER_ID, CUSTOMER_ID, DELIVERY_ADDRESS, Carrier.BLACK_CAT);

            shipment.markAsPickedUp();

            assertEquals(DeliveryStatus.PICKED_UP, shipment.getStatus());
        }

        @Test
        @DisplayName("should fail to pick up when not in pending status")
        void shouldFailToPickUpWhenNotPending() {
            Shipment shipment = Shipment.create(ORDER_ID, CUSTOMER_ID, DELIVERY_ADDRESS, Carrier.BLACK_CAT);
            shipment.markAsPickedUp();
            shipment.markAsInTransit();

            assertThrows(IllegalStateException.class, shipment::markAsPickedUp);
        }
    }

    @Nested
    @DisplayName("Shipment In Transit")
    class ShipmentInTransit {

        @Test
        @DisplayName("should mark shipment as in transit")
        void shouldMarkShipmentAsInTransit() {
            Shipment shipment = Shipment.create(ORDER_ID, CUSTOMER_ID, DELIVERY_ADDRESS, Carrier.BLACK_CAT);
            shipment.markAsPickedUp();

            shipment.markAsInTransit();

            assertEquals(DeliveryStatus.IN_TRANSIT, shipment.getStatus());
            assertNotNull(shipment.getShippedAt());
        }

        @Test
        @DisplayName("should fail to transit when not picked up")
        void shouldFailToTransitWhenNotPickedUp() {
            Shipment shipment = Shipment.create(ORDER_ID, CUSTOMER_ID, DELIVERY_ADDRESS, Carrier.BLACK_CAT);

            assertThrows(IllegalStateException.class, shipment::markAsInTransit);
        }
    }

    @Nested
    @DisplayName("Shipment Out For Delivery")
    class ShipmentOutForDelivery {

        @Test
        @DisplayName("should mark shipment as out for delivery")
        void shouldMarkShipmentAsOutForDelivery() {
            Shipment shipment = Shipment.create(ORDER_ID, CUSTOMER_ID, DELIVERY_ADDRESS, Carrier.BLACK_CAT);
            shipment.markAsPickedUp();
            shipment.markAsInTransit();

            shipment.markAsOutForDelivery();

            assertEquals(DeliveryStatus.OUT_FOR_DELIVERY, shipment.getStatus());
        }

        @Test
        @DisplayName("should fail when not in transit")
        void shouldFailWhenNotInTransit() {
            Shipment shipment = Shipment.create(ORDER_ID, CUSTOMER_ID, DELIVERY_ADDRESS, Carrier.BLACK_CAT);
            shipment.markAsPickedUp();

            assertThrows(IllegalStateException.class, shipment::markAsOutForDelivery);
        }
    }

    @Nested
    @DisplayName("Shipment Delivery")
    class ShipmentDelivery {

        @Test
        @DisplayName("should mark shipment as delivered")
        void shouldMarkShipmentAsDelivered() {
            Shipment shipment = Shipment.create(ORDER_ID, CUSTOMER_ID, DELIVERY_ADDRESS, Carrier.BLACK_CAT);
            shipment.markAsPickedUp();
            shipment.markAsInTransit();
            shipment.markAsOutForDelivery();

            shipment.markAsDelivered();

            assertEquals(DeliveryStatus.DELIVERED, shipment.getStatus());
            assertNotNull(shipment.getDeliveredAt());
        }

        @Test
        @DisplayName("should fail when not out for delivery")
        void shouldFailWhenNotOutForDelivery() {
            Shipment shipment = Shipment.create(ORDER_ID, CUSTOMER_ID, DELIVERY_ADDRESS, Carrier.BLACK_CAT);
            shipment.markAsPickedUp();
            shipment.markAsInTransit();

            assertThrows(IllegalStateException.class, shipment::markAsDelivered);
        }

        @Test
        @DisplayName("should not allow status change after delivery")
        void shouldNotAllowStatusChangeAfterDelivery() {
            Shipment shipment = Shipment.create(ORDER_ID, CUSTOMER_ID, DELIVERY_ADDRESS, Carrier.BLACK_CAT);
            shipment.markAsPickedUp();
            shipment.markAsInTransit();
            shipment.markAsOutForDelivery();
            shipment.markAsDelivered();

            assertThrows(IllegalStateException.class, shipment::markAsInTransit);
        }
    }

    @Nested
    @DisplayName("Shipment Failure")
    class ShipmentFailure {

        @Test
        @DisplayName("should mark shipment as failed with reason")
        void shouldMarkShipmentAsFailedWithReason() {
            Shipment shipment = Shipment.create(ORDER_ID, CUSTOMER_ID, DELIVERY_ADDRESS, Carrier.BLACK_CAT);
            shipment.markAsPickedUp();
            shipment.markAsInTransit();

            shipment.markAsFailed("收件人不在家，無法聯繫");

            assertEquals(DeliveryStatus.FAILED, shipment.getStatus());
            assertEquals("收件人不在家，無法聯繫", shipment.getFailureReason());
        }

        @Test
        @DisplayName("should fail when trying to fail a delivered shipment")
        void shouldFailWhenTryingToFailDeliveredShipment() {
            Shipment shipment = Shipment.create(ORDER_ID, CUSTOMER_ID, DELIVERY_ADDRESS, Carrier.BLACK_CAT);
            shipment.markAsPickedUp();
            shipment.markAsInTransit();
            shipment.markAsOutForDelivery();
            shipment.markAsDelivered();

            assertThrows(IllegalStateException.class, () ->
                    shipment.markAsFailed("無法配送"));
        }
    }

    @Nested
    @DisplayName("Shipment Returned")
    class ShipmentReturned {

        @Test
        @DisplayName("should mark failed shipment as returned")
        void shouldMarkFailedShipmentAsReturned() {
            Shipment shipment = Shipment.create(ORDER_ID, CUSTOMER_ID, DELIVERY_ADDRESS, Carrier.BLACK_CAT);
            shipment.markAsPickedUp();
            shipment.markAsInTransit();
            shipment.markAsFailed("地址錯誤");

            shipment.markAsReturned();

            assertEquals(DeliveryStatus.RETURNED, shipment.getStatus());
        }

        @Test
        @DisplayName("should fail when shipment is not failed")
        void shouldFailWhenShipmentIsNotFailed() {
            Shipment shipment = Shipment.create(ORDER_ID, CUSTOMER_ID, DELIVERY_ADDRESS, Carrier.BLACK_CAT);
            shipment.markAsPickedUp();
            shipment.markAsInTransit();

            assertThrows(IllegalStateException.class, shipment::markAsReturned);
        }
    }

    @Nested
    @DisplayName("Tracking Events")
    class TrackingEvents {

        @Test
        @DisplayName("should add tracking event")
        void shouldAddTrackingEvent() {
            Shipment shipment = Shipment.create(ORDER_ID, CUSTOMER_ID, DELIVERY_ADDRESS, Carrier.BLACK_CAT);

            shipment.addTrackingEvent("包裹已抵達台北轉運中心", "台北市");

            assertEquals(1, shipment.getTrackingEvents().size());
            assertEquals("包裹已抵達台北轉運中心", shipment.getTrackingEvents().get(0).description());
            assertEquals("台北市", shipment.getTrackingEvents().get(0).location());
        }

        @Test
        @DisplayName("should maintain tracking event order")
        void shouldMaintainTrackingEventOrder() {
            Shipment shipment = Shipment.create(ORDER_ID, CUSTOMER_ID, DELIVERY_ADDRESS, Carrier.BLACK_CAT);

            shipment.addTrackingEvent("包裹已收件", "桃園市");
            shipment.addTrackingEvent("包裹運送中", "新北市");
            shipment.addTrackingEvent("包裹已抵達", "台北市");

            assertEquals(3, shipment.getTrackingEvents().size());
            assertEquals("包裹已收件", shipment.getTrackingEvents().get(0).description());
            assertEquals("包裹已抵達", shipment.getTrackingEvents().get(2).description());
        }

        @Test
        @DisplayName("should set estimated delivery date")
        void shouldSetEstimatedDeliveryDate() {
            Shipment shipment = Shipment.create(ORDER_ID, CUSTOMER_ID, DELIVERY_ADDRESS, Carrier.BLACK_CAT);
            Instant estimatedDate = Instant.now().plusSeconds(86400 * 2); // 2 days later

            shipment.setEstimatedDeliveryDate(estimatedDate);

            assertEquals(estimatedDate, shipment.getEstimatedDeliveryDate());
        }
    }

    @Nested
    @DisplayName("Carrier Information")
    class CarrierInformation {

        @Test
        @DisplayName("should return correct carrier display name")
        void shouldReturnCorrectCarrierDisplayName() {
            assertEquals("黑貓宅急便", Carrier.BLACK_CAT.getDisplayName());
            assertEquals("新竹物流", Carrier.HSINCHU_LOGISTICS.getDisplayName());
            assertEquals("7-11 超商取貨", Carrier.SEVEN_ELEVEN.getDisplayName());
            assertEquals("全家超商取貨", Carrier.FAMILY_MART.getDisplayName());
        }

        @Test
        @DisplayName("should return correct carrier tracking URL pattern")
        void shouldReturnCorrectCarrierTrackingUrlPattern() {
            assertNotNull(Carrier.BLACK_CAT.getTrackingUrlPattern());
            assertTrue(Carrier.BLACK_CAT.getTrackingUrlPattern().contains("{trackingNumber}"));
        }
    }
}
