package com.ecommerce.customer.application.usecases;

import com.ecommerce.customer.application.dto.CustomerResponse;
import com.ecommerce.customer.application.dto.RegisterCustomerCommand;
import com.ecommerce.customer.application.exceptions.EmailAlreadyExistsException;
import com.ecommerce.customer.domain.entities.Customer;
import com.ecommerce.customer.domain.ports.CustomerRepository;
import com.ecommerce.customer.domain.value_objects.Email;
import com.ecommerce.customer.domain.value_objects.Password;
import com.ecommerce.shared.domain.events.DomainEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for registering a new customer.
 */
@Service
public class RegisterCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final DomainEventPublisher eventPublisher;

    public RegisterCustomerUseCase(
            CustomerRepository customerRepository,
            DomainEventPublisher eventPublisher
    ) {
        this.customerRepository = customerRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Registers a new customer.
     *
     * @param command the registration command
     * @return the created customer response
     * @throws EmailAlreadyExistsException if email is already registered
     */
    @Transactional
    public CustomerResponse execute(RegisterCustomerCommand command) {
        Email email = Email.of(command.email());

        if (customerRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(command.email());
        }

        Password password = Password.fromRaw(command.password());

        Customer customer = Customer.register(
                email,
                password,
                command.firstName(),
                command.lastName(),
                command.phoneNumber()
        );

        Customer savedCustomer = customerRepository.save(customer);

        // Publish domain events
        savedCustomer.getDomainEvents().forEach(eventPublisher::publish);
        savedCustomer.clearDomainEvents();

        return CustomerResponse.from(savedCustomer);
    }
}
