package com.ecommerce.logistics.domain.aggregates;

import com.ecommerce.logistics.domain.value_objects.Carrier;
import com.ecommerce.logistics.domain.value_objects.DeliveryStatus;
import com.ecommerce.logistics.domain.value_objects.TrackingEvent;
import com.ecommerce.logistics.domain.value_objects.TrackingNumber;
import com.ecommerce.shared.domain.value_objects.Address;

import java.time.Instant;
import java.util.*;

/**
 * Shipment aggregate root.
 * Manages the lifecycle of a shipment from creation to delivery.
 */
public class Shipment {

    private final UUID id;
    private final UUID orderId;
    private final UUID customerId;
    private final TrackingNumber trackingNumber;
    private final Address deliveryAddress;
    private final Carrier carrier;
    private DeliveryStatus status;
    private final List<TrackingEvent> trackingEvents;
    private String failureReason;
    private Instant estimatedDeliveryDate;
    private final Instant createdAt;
    private Instant shippedAt;
    private Instant deliveredAt;
    private Instant updatedAt;

    private Shipment(UUID id, UUID orderId, UUID customerId, TrackingNumber trackingNumber,
                     Address deliveryAddress, Carrier carrier, DeliveryStatus status,
                     Instant createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.customerId = customerId;
        this.trackingNumber = trackingNumber;
        this.deliveryAddress = deliveryAddress;
        this.carrier = carrier;
        this.status = status;
        this.trackingEvents = new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    /**
     * Creates a new shipment.
     */
    public static Shipment create(UUID orderId, UUID customerId, Address deliveryAddress, Carrier carrier) {
        Objects.requireNonNull(orderId, "Order ID must not be null");
        Objects.requireNonNull(customerId, "Customer ID must not be null");
        Objects.requireNonNull(deliveryAddress, "Delivery address must not be null");
        Objects.requireNonNull(carrier, "Carrier must not be null");

        return new Shipment(
                UUID.randomUUID(),
                orderId,
                customerId,
                TrackingNumber.generate(carrier),
                deliveryAddress,
                carrier,
                DeliveryStatus.PENDING,
                Instant.now()
        );
    }

    /**
     * Reconstitutes a shipment from persistence.
     */
    public static Shipment reconstitute(UUID id, UUID orderId, UUID customerId,
                                         TrackingNumber trackingNumber, Address deliveryAddress,
                                         Carrier carrier, DeliveryStatus status,
                                         List<TrackingEvent> events, String failureReason,
                                         Instant estimatedDeliveryDate, Instant createdAt,
                                         Instant shippedAt, Instant deliveredAt, Instant updatedAt) {
        Shipment shipment = new Shipment(id, orderId, customerId, trackingNumber,
                deliveryAddress, carrier, status, createdAt);
        shipment.trackingEvents.addAll(events);
        shipment.failureReason = failureReason;
        shipment.estimatedDeliveryDate = estimatedDeliveryDate;
        shipment.shippedAt = shippedAt;
        shipment.deliveredAt = deliveredAt;
        shipment.updatedAt = updatedAt;
        return shipment;
    }

    /**
     * Marks the shipment as picked up by carrier.
     */
    public void markAsPickedUp() {
        if (!status.canPickUp()) {
            throw new IllegalStateException("Cannot pick up shipment. Current status: " + status);
        }
        this.status = DeliveryStatus.PICKED_UP;
        this.updatedAt = Instant.now();
        addTrackingEvent("包裹已由物流商取件", null);
    }

    /**
     * Marks the shipment as in transit.
     */
    public void markAsInTransit() {
        if (!status.canTransit()) {
            throw new IllegalStateException("Cannot mark as in transit. Current status: " + status);
        }
        this.status = DeliveryStatus.IN_TRANSIT;
        this.shippedAt = Instant.now();
        this.updatedAt = Instant.now();
        addTrackingEvent("包裹運送中", null);
    }

    /**
     * Marks the shipment as out for delivery.
     */
    public void markAsOutForDelivery() {
        if (!status.canOutForDelivery()) {
            throw new IllegalStateException("Cannot mark as out for delivery. Current status: " + status);
        }
        this.status = DeliveryStatus.OUT_FOR_DELIVERY;
        this.updatedAt = Instant.now();
        addTrackingEvent("包裹正在配送中", deliveryAddress.getCity());
    }

    /**
     * Marks the shipment as delivered.
     */
    public void markAsDelivered() {
        if (!status.canDeliver()) {
            throw new IllegalStateException("Cannot mark as delivered. Current status: " + status);
        }
        this.status = DeliveryStatus.DELIVERED;
        this.deliveredAt = Instant.now();
        this.updatedAt = Instant.now();
        addTrackingEvent("包裹已成功送達", deliveryAddress.getCity());
    }

    /**
     * Marks the shipment as failed.
     */
    public void markAsFailed(String reason) {
        if (!status.canFail()) {
            throw new IllegalStateException("Cannot mark as failed. Current status: " + status);
        }
        Objects.requireNonNull(reason, "Failure reason must not be null");
        this.status = DeliveryStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt = Instant.now();
        addTrackingEvent("配送失敗: " + reason, deliveryAddress.getCity());
    }

    /**
     * Marks the shipment as returned.
     */
    public void markAsReturned() {
        if (!status.canReturn()) {
            throw new IllegalStateException("Cannot mark as returned. Current status: " + status);
        }
        this.status = DeliveryStatus.RETURNED;
        this.updatedAt = Instant.now();
        addTrackingEvent("包裹已退回寄件人", null);
    }

    /**
     * Adds a tracking event to the shipment.
     */
    public void addTrackingEvent(String description, String location) {
        this.trackingEvents.add(TrackingEvent.create(description, location));
        this.updatedAt = Instant.now();
    }

    /**
     * Sets the estimated delivery date.
     */
    public void setEstimatedDeliveryDate(Instant estimatedDate) {
        this.estimatedDeliveryDate = estimatedDate;
        this.updatedAt = Instant.now();
    }

    /**
     * Gets the carrier's tracking URL for this shipment.
     */
    public String getCarrierTrackingUrl() {
        return carrier.getTrackingUrl(trackingNumber.getValue());
    }

    // Getters

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public TrackingNumber getTrackingNumber() {
        return trackingNumber;
    }

    public Address getDeliveryAddress() {
        return deliveryAddress;
    }

    public Carrier getCarrier() {
        return carrier;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public List<TrackingEvent> getTrackingEvents() {
        return Collections.unmodifiableList(trackingEvents);
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Instant getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getShippedAt() {
        return shippedAt;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shipment shipment = (Shipment) o;
        return id.equals(shipment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Shipment{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", trackingNumber=" + trackingNumber +
                ", status=" + status +
                ", carrier=" + carrier +
                '}';
    }
}
