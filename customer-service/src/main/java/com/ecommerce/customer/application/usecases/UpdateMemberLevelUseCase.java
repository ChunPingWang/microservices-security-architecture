package com.ecommerce.customer.application.usecases;

import com.ecommerce.customer.application.exceptions.CustomerNotFoundException;
import com.ecommerce.customer.domain.entities.Customer;
import com.ecommerce.customer.domain.events.LevelUpgraded;
import com.ecommerce.customer.domain.ports.CustomerRepository;
import com.ecommerce.customer.domain.services.MemberLevelCalculator;
import com.ecommerce.customer.domain.value_objects.MemberLevel;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Use case for updating customer member level based on spending.
 */
@Service
public class UpdateMemberLevelUseCase {

    private final CustomerRepository customerRepository;
    private final MemberLevelCalculator memberLevelCalculator;
    private final ApplicationEventPublisher eventPublisher;

    public UpdateMemberLevelUseCase(CustomerRepository customerRepository,
                                     MemberLevelCalculator memberLevelCalculator,
                                     ApplicationEventPublisher eventPublisher) {
        this.customerRepository = customerRepository;
        this.memberLevelCalculator = memberLevelCalculator;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Adds spending to customer and updates member level if needed.
     * Returns true if level was upgraded.
     */
    @Transactional
    public boolean addSpendingAndUpdateLevel(UUID customerId, BigDecimal spendingAmount) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> CustomerNotFoundException.byId(customerId.toString()));

        MemberLevel previousLevel = customer.getMemberLevel();
        BigDecimal newTotalSpending = customer.getTotalSpending().add(spendingAmount);

        // Check if this will result in an upgrade
        boolean willUpgrade = memberLevelCalculator.wouldUpgrade(previousLevel, newTotalSpending);

        // Update spending (this also updates the member level in Customer entity)
        customer.addSpending(spendingAmount);
        customerRepository.save(customer);

        // Publish event if upgraded
        if (willUpgrade) {
            MemberLevel newLevel = customer.getMemberLevel();
            int newDiscount = memberLevelCalculator.getDiscountPercentage(newLevel);

            eventPublisher.publishEvent(new LevelUpgraded(
                    customerId.toString(),
                    previousLevel.name(),
                    newLevel.name(),
                    newDiscount,
                    customer.getTotalSpending()
            ));
        }

        return willUpgrade;
    }
}
