package com.ecommerce.customer.application.usecases;

import com.ecommerce.customer.application.dto.CustomerResponse;
import com.ecommerce.customer.application.exceptions.CustomerNotFoundException;
import com.ecommerce.customer.domain.entities.Customer;
import com.ecommerce.customer.domain.ports.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case for retrieving customer profile.
 */
@Service
public class GetCustomerProfileUseCase {

    private final CustomerRepository customerRepository;

    public GetCustomerProfileUseCase(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Gets a customer profile by ID.
     *
     * @param customerId the customer ID
     * @return the customer response
     * @throws CustomerNotFoundException if customer not found
     */
    @Transactional(readOnly = true)
    public CustomerResponse execute(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> CustomerNotFoundException.byId(customerId.toString()));

        return CustomerResponse.from(customer);
    }
}
