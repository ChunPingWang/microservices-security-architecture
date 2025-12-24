package com.ecommerce.logistics.application.usecases;

import com.ecommerce.logistics.application.dto.CreateShipmentCommand;
import com.ecommerce.logistics.application.dto.ShipmentResponse;
import com.ecommerce.logistics.application.exceptions.ShipmentAlreadyExistsException;
import com.ecommerce.logistics.domain.aggregates.Shipment;
import com.ecommerce.logistics.domain.ports.OrderServicePort;
import com.ecommerce.logistics.domain.ports.ShipmentRepository;
import com.ecommerce.shared.domain.value_objects.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Use case for creating a new shipment.
 */
@Service
public class CreateShipmentUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateShipmentUseCase.class);

    private final ShipmentRepository shipmentRepository;
    private final OrderServicePort orderServicePort;

    public CreateShipmentUseCase(ShipmentRepository shipmentRepository,
                                  OrderServicePort orderServicePort) {
        this.shipmentRepository = shipmentRepository;
        this.orderServicePort = orderServicePort;
    }

    public ShipmentResponse execute(CreateShipmentCommand command) {
        log.info("Creating shipment for order: {}", command.orderId());

        // Check if shipment already exists for this order
        if (shipmentRepository.existsByOrderId(command.orderId())) {
            throw new ShipmentAlreadyExistsException(command.orderId());
        }

        // Convert address DTO to domain object
        Address deliveryAddress = Address.builder()
                .street(command.shippingAddress().street())
                .city(command.shippingAddress().city())
                .district(command.shippingAddress().district())
                .postalCode(command.shippingAddress().postalCode())
                .country(command.shippingAddress().country())
                .recipientName(command.shippingAddress().recipientName())
                .phoneNumber(command.shippingAddress().phoneNumber())
                .build();

        // Create shipment
        Shipment shipment = Shipment.create(
                command.orderId(),
                command.customerId(),
                deliveryAddress,
                command.carrier()
        );

        // Save shipment
        Shipment saved = shipmentRepository.save(shipment);

        // Notify order service
        orderServicePort.notifyShipmentCreated(
                saved.getOrderId(),
                saved.getId(),
                saved.getTrackingNumber().getValue()
        );

        log.info("Shipment created: {} with tracking number: {}",
                saved.getId(), saved.getTrackingNumber());

        return ShipmentResponse.from(saved);
    }
}
