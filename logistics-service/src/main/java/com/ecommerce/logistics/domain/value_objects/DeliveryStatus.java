package com.ecommerce.logistics.domain.value_objects;

/**
 * Enum representing the delivery status of a shipment.
 * Follows a state machine pattern for valid transitions.
 */
public enum DeliveryStatus {

    /**
     * Shipment created, waiting for carrier pickup.
     */
    PENDING("等待取件"),

    /**
     * Package picked up by carrier.
     */
    PICKED_UP("已取件"),

    /**
     * Package in transit to destination.
     */
    IN_TRANSIT("運送中"),

    /**
     * Package out for delivery.
     */
    OUT_FOR_DELIVERY("配送中"),

    /**
     * Package delivered successfully.
     */
    DELIVERED("已送達"),

    /**
     * Delivery failed (e.g., recipient not available).
     */
    FAILED("配送失敗"),

    /**
     * Package returned to sender.
     */
    RETURNED("已退回");

    private final String displayName;

    DeliveryStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Checks if transition to picked up is allowed.
     */
    public boolean canPickUp() {
        return this == PENDING;
    }

    /**
     * Checks if transition to in transit is allowed.
     */
    public boolean canTransit() {
        return this == PICKED_UP;
    }

    /**
     * Checks if transition to out for delivery is allowed.
     */
    public boolean canOutForDelivery() {
        return this == IN_TRANSIT;
    }

    /**
     * Checks if transition to delivered is allowed.
     */
    public boolean canDeliver() {
        return this == OUT_FOR_DELIVERY;
    }

    /**
     * Checks if the shipment can be marked as failed.
     */
    public boolean canFail() {
        return this == IN_TRANSIT || this == OUT_FOR_DELIVERY || this == PICKED_UP;
    }

    /**
     * Checks if the shipment can be returned.
     */
    public boolean canReturn() {
        return this == FAILED;
    }

    /**
     * Checks if this is a terminal state.
     */
    public boolean isTerminal() {
        return this == DELIVERED || this == RETURNED;
    }
}
