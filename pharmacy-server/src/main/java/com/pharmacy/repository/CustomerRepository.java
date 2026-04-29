package com.pharmacy.repository;

import com.pharmacy.entity.Customer;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class CustomerRepository {

    public void save(EntityManager em, Customer customer) {
        try {
            em.persist(customer);
        } catch (Exception e) {
            log.error("event=customer_save_failed errorMessage={}", e.getMessage());
            throw e;
        }
    }

    public Customer update(EntityManager em, Customer customer) {
        try {
            return em.merge(customer);
        } catch (Exception e) {
            log.error("event=customer_update_failed phoneNumber={} errorMessage={}", customer.getPhoneNumber(), e.getMessage());
            throw e;
        }
    }

    public Customer findByPhoneNumber(EntityManager em, String phoneNumber) {
        try {
            List<Customer> customers = em.createQuery("select c from Customer c where c.phoneNumber = :phone", Customer.class)
                    .setParameter("phone", phoneNumber)
                    .setMaxResults(1)
                    .getResultList();

            return customers.isEmpty() ? null : customers.get(0);
        } catch (Exception e) {
            log.error("event=customer_find_by_phone_failed phoneNumber={} errorMessage={}", phoneNumber, e.getMessage());
            throw e;
        }
    }

}
