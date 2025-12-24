package com.ecommerce.logistics.contract;

import com.ecommerce.logistics.application.dto.ShipmentResponse;
import com.ecommerce.logistics.application.exceptions.ShipmentNotFoundException;
import com.ecommerce.logistics.application.usecases.CreateShipmentUseCase;
import com.ecommerce.logistics.application.usecases.TrackShipmentUseCase;
import com.ecommerce.logistics.domain.aggregates.Shipment;
import com.ecommerce.logistics.domain.ports.ShipmentRepository;
import com.ecommerce.logistics.domain.value_objects.Carrier;
import com.ecommerce.logistics.infrastructure.web.controllers.ShipmentController;
import com.ecommerce.logistics.infrastructure.web.GlobalExceptionHandler;
import com.ecommerce.shared.domain.value_objects.Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract tests for GET /api/v1/shipments/{orderId}/tracking endpoint.
 */
@WebMvcTest(excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {ShipmentController.class, GlobalExceptionHandler.class})
@DisplayName("Tracking Contract Tests")
class TrackingContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShipmentRepository shipmentRepository;

    @MockBean
    private TrackShipmentUseCase trackShipmentUseCase;

    @MockBean
    private CreateShipmentUseCase createShipmentUseCase;

    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private Address deliveryAddress;

    @BeforeEach
    void setUp() {
        deliveryAddress = Address.builder()
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
    @DisplayName("Response Contract - Tracking Info")
    class TrackingInfoContract {

        @Test
        @DisplayName("should return tracking info with all required fields")
        void shouldReturnTrackingInfoWithRequiredFields() throws Exception {
            Shipment shipment = createShipment();
            ShipmentResponse response = ShipmentResponse.from(shipment);
            when(trackShipmentUseCase.getByOrderId(ORDER_ID)).thenReturn(response);

            mockMvc.perform(get("/api/v1/shipments/{orderId}/tracking", ORDER_ID)
                            .principal(() -> CUSTOMER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.orderId").value(ORDER_ID.toString()))
                    .andExpect(jsonPath("$.trackingNumber").exists())
                    .andExpect(jsonPath("$.carrier").exists())
                    .andExpect(jsonPath("$.carrierDisplayName").exists())
                    .andExpect(jsonPath("$.status").exists())
                    .andExpect(jsonPath("$.statusDisplayName").exists())
                    .andExpect(jsonPath("$.createdAt").exists());
        }

        @Test
        @DisplayName("should return delivery address in response")
        void shouldReturnDeliveryAddressInResponse() throws Exception {
            Shipment shipment = createShipment();
            ShipmentResponse response = ShipmentResponse.from(shipment);
            when(trackShipmentUseCase.getByOrderId(ORDER_ID)).thenReturn(response);

            mockMvc.perform(get("/api/v1/shipments/{orderId}/tracking", ORDER_ID)
                            .principal(() -> CUSTOMER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.deliveryAddress").exists())
                    .andExpect(jsonPath("$.deliveryAddress.city").value("台北市"))
                    .andExpect(jsonPath("$.deliveryAddress.recipientName").value("王小明"));
        }

        @Test
        @DisplayName("should return tracking events timeline")
        void shouldReturnTrackingEventsTimeline() throws Exception {
            Shipment shipment = createShipmentWithEvents();
            ShipmentResponse response = ShipmentResponse.from(shipment);
            when(trackShipmentUseCase.getByOrderId(ORDER_ID)).thenReturn(response);

            mockMvc.perform(get("/api/v1/shipments/{orderId}/tracking", ORDER_ID)
                            .principal(() -> CUSTOMER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.trackingEvents").isArray())
                    .andExpect(jsonPath("$.trackingEvents[0].description").exists())
                    .andExpect(jsonPath("$.trackingEvents[0].location").exists())
                    .andExpect(jsonPath("$.trackingEvents[0].timestamp").exists());
        }

        @Test
        @DisplayName("should return estimated delivery date when available")
        void shouldReturnEstimatedDeliveryDate() throws Exception {
            Shipment shipment = createShipmentInTransit();
            ShipmentResponse response = ShipmentResponse.from(shipment);
            when(trackShipmentUseCase.getByOrderId(ORDER_ID)).thenReturn(response);

            mockMvc.perform(get("/api/v1/shipments/{orderId}/tracking", ORDER_ID)
                            .principal(() -> CUSTOMER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estimatedDeliveryDate").exists());
        }
    }

    @Nested
    @DisplayName("Response Contract - Status Display")
    class StatusDisplayContract {

        @Test
        @DisplayName("should return PENDING status for new shipment")
        void shouldReturnPendingStatus() throws Exception {
            Shipment shipment = createShipment();
            ShipmentResponse response = ShipmentResponse.from(shipment);
            when(trackShipmentUseCase.getByOrderId(ORDER_ID)).thenReturn(response);

            mockMvc.perform(get("/api/v1/shipments/{orderId}/tracking", ORDER_ID)
                            .principal(() -> CUSTOMER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.statusDisplayName").value("等待取件"));
        }

        @Test
        @DisplayName("should return IN_TRANSIT status for shipment in transit")
        void shouldReturnInTransitStatus() throws Exception {
            Shipment shipment = createShipmentInTransit();
            ShipmentResponse response = ShipmentResponse.from(shipment);
            when(trackShipmentUseCase.getByOrderId(ORDER_ID)).thenReturn(response);

            mockMvc.perform(get("/api/v1/shipments/{orderId}/tracking", ORDER_ID)
                            .principal(() -> CUSTOMER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("IN_TRANSIT"))
                    .andExpect(jsonPath("$.statusDisplayName").value("運送中"));
        }

        @Test
        @DisplayName("should return DELIVERED status for delivered shipment")
        void shouldReturnDeliveredStatus() throws Exception {
            Shipment shipment = createDeliveredShipment();
            ShipmentResponse response = ShipmentResponse.from(shipment);
            when(trackShipmentUseCase.getByOrderId(ORDER_ID)).thenReturn(response);

            mockMvc.perform(get("/api/v1/shipments/{orderId}/tracking", ORDER_ID)
                            .principal(() -> CUSTOMER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("DELIVERED"))
                    .andExpect(jsonPath("$.statusDisplayName").value("已送達"))
                    .andExpect(jsonPath("$.deliveredAt").exists());
        }
    }

    @Nested
    @DisplayName("Response Contract - Carrier Info")
    class CarrierInfoContract {

        @Test
        @DisplayName("should return carrier tracking URL")
        void shouldReturnCarrierTrackingUrl() throws Exception {
            Shipment shipment = createShipment();
            ShipmentResponse response = ShipmentResponse.from(shipment);
            when(trackShipmentUseCase.getByOrderId(ORDER_ID)).thenReturn(response);

            mockMvc.perform(get("/api/v1/shipments/{orderId}/tracking", ORDER_ID)
                            .principal(() -> CUSTOMER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.carrierTrackingUrl").exists());
        }
    }

    @Nested
    @DisplayName("Response Contract - Failure")
    class FailureContract {

        @Test
        @DisplayName("should return 404 when shipment not found")
        void shouldReturn404WhenShipmentNotFound() throws Exception {
            when(trackShipmentUseCase.getByOrderId(ORDER_ID))
                    .thenThrow(new ShipmentNotFoundException("Order", ORDER_ID));

            mockMvc.perform(get("/api/v1/shipments/{orderId}/tracking", ORDER_ID)
                            .principal(() -> CUSTOMER_ID.toString()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("SHIPMENT_NOT_FOUND"));
        }

        @Test
        @DisplayName("should return 400 when order ID is invalid")
        void shouldReturn400WhenOrderIdInvalid() throws Exception {
            mockMvc.perform(get("/api/v1/shipments/{orderId}/tracking", "invalid-uuid")
                            .principal(() -> CUSTOMER_ID.toString()))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Get Shipment by ID Contract")
    class GetShipmentByIdContract {

        @Test
        @DisplayName("should return shipment by ID")
        void shouldReturnShipmentById() throws Exception {
            Shipment shipment = createShipment();
            ShipmentResponse response = ShipmentResponse.from(shipment);
            when(trackShipmentUseCase.getById(shipment.getId())).thenReturn(response);

            mockMvc.perform(get("/api/v1/shipments/{shipmentId}", shipment.getId())
                            .principal(() -> CUSTOMER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(shipment.getId().toString()));
        }
    }

    private Shipment createShipment() {
        return Shipment.create(ORDER_ID, CUSTOMER_ID, deliveryAddress, Carrier.BLACK_CAT);
    }

    private Shipment createShipmentWithEvents() {
        Shipment shipment = createShipment();
        shipment.addTrackingEvent("包裹已收件", "桃園市");
        shipment.addTrackingEvent("包裹運送中", "新北市");
        return shipment;
    }

    private Shipment createShipmentInTransit() {
        Shipment shipment = createShipment();
        shipment.markAsPickedUp();
        shipment.markAsInTransit();
        shipment.setEstimatedDeliveryDate(java.time.Instant.now().plusSeconds(86400 * 2));
        return shipment;
    }

    private Shipment createDeliveredShipment() {
        Shipment shipment = createShipment();
        shipment.markAsPickedUp();
        shipment.markAsInTransit();
        shipment.markAsOutForDelivery();
        shipment.markAsDelivered();
        return shipment;
    }
}
