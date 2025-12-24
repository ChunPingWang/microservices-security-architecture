package com.ecommerce.customer.application.usecases;

import com.ecommerce.customer.application.dto.MembershipResponse;
import com.ecommerce.customer.application.exceptions.CustomerNotFoundException;
import com.ecommerce.customer.domain.entities.Customer;
import com.ecommerce.customer.domain.ports.CustomerRepository;
import com.ecommerce.customer.domain.services.MemberLevelCalculator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Use case for retrieving customer membership information.
 */
@Service
public class GetMembershipUseCase {

    private final CustomerRepository customerRepository;
    private final MemberLevelCalculator memberLevelCalculator;

    public GetMembershipUseCase(CustomerRepository customerRepository,
                                 MemberLevelCalculator memberLevelCalculator) {
        this.customerRepository = customerRepository;
        this.memberLevelCalculator = memberLevelCalculator;
    }

    /**
     * Gets membership information for a customer.
     */
    public MembershipResponse getMembership(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> CustomerNotFoundException.byId(customerId.toString()));

        BigDecimal spendingToNext = memberLevelCalculator.spendingToNextLevel(customer.getTotalSpending());
        int discountPercentage = memberLevelCalculator.getDiscountPercentage(customer.getMemberLevel());
        String benefitDescription = memberLevelCalculator.getBenefitDescription(
                customer.getMemberLevel(),
                spendingToNext
        );

        return new MembershipResponse(
                customer.getId(),
                customer.getMemberLevel().name(),
                customer.getTotalSpending(),
                spendingToNext,
                discountPercentage,
                benefitDescription
        );
    }
}
