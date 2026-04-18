package com.pharmacy.repository;

import com.pharmacy.entity.Medicine;
import com.pharmacy.shared.util.enums.MedicineType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class MedicineRepository {

    public int countAll(EntityManager em) {
        try {
            Number total = (Number) em.createNativeQuery("SELECT COUNT(*) FROM tbl_medicine")
                    .getSingleResult();
            return total.intValue();
        } catch (Exception e) {
            log.error("event=medicine_count_failed errorMessage={}", e.getMessage());
            throw e;
        }
    }

    public void save(EntityManager em, Medicine medicine) {
        try {
            em.persist(medicine);
        } catch (Exception e) {
            log.error("event=medicine_insert_failed barcode={} errorMessage={}", medicine.getBarcode(), e.getMessage());
            throw e;
        }
    }

    public Medicine update(EntityManager em, Medicine medicine) {
        try {
            return em.merge(medicine);
        } catch (Exception e) {
            log.error("event=medicine_update_failed barcode={} errorMessage={}", medicine.getBarcode(), e.getMessage());
            throw e;
        }
    }

    public boolean existsByRegistrationNumber(EntityManager em, String registrationNumber) {
        try {
            Long count = em.createQuery(
                            "select count(m) from Medicine m where m.registrationNumber = :registrationNumber",
                            Long.class)
                    .setParameter("registrationNumber", registrationNumber)
                    .getSingleResult();
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("event=medicine_check_registration_number_failed registrationNumber={} errorMessage={}", registrationNumber, e.getMessage());
            throw e;
        }
    }

    public boolean existsByRegistrationNumberExcludingBarcode(EntityManager em, String registrationNumber, String barcode) {
        try {
            Long count = em.createQuery(
                            "select count(m) from Medicine m where m.registrationNumber = :registrationNumber and m.barcode <> :barcode",
                            Long.class)
                    .setParameter("registrationNumber", registrationNumber)
                    .setParameter("barcode", barcode)
                    .getSingleResult();
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("event=medicine_check_registration_number_excluding_barcode_failed registrationNumber={} barcode={} errorMessage={}", registrationNumber, barcode, e.getMessage());
            throw e;
        }
    }

    public boolean existsByBarcode(EntityManager em, String barcode) {
        try {
            Long count = em.createQuery(
                            "select count(m) from Medicine m where m.barcode = :barcode",
                            Long.class)
                    .setParameter("barcode", barcode)
                    .getSingleResult();
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("event=medicine_check_barcode_failed barcode={} errorMessage={}", barcode, e.getMessage());
            throw e;
        }
    }

    public Medicine findByBarcode(EntityManager em, String barcode) {
        try {
            List<Medicine> medicines = em.createQuery(
                            "select m from Medicine m where m.barcode = :barcode",
                            Medicine.class)
                    .setParameter("barcode", barcode)
                    .setMaxResults(1)
                    .getResultList();
            return medicines.isEmpty() ? null : medicines.get(0);
        } catch (Exception e) {
            log.error("event=medicine_find_by_barcode_failed barcode={} errorMessage={}", barcode, e.getMessage());
            throw e;
        }
    }

    public List<Medicine> findAllByPage(EntityManager em, int skip, int pageSize) {
        try {
            return em.createQuery("select m from Medicine m order by m.id desc", Medicine.class)
                    .setFirstResult(skip)
                    .setMaxResults(pageSize)
                    .getResultList();
        } catch (Exception e) {
            log.error("event=medicine_find_all_by_page_failed skip={} pageSize={} errorMessage={}", skip, pageSize, e.getMessage());
            throw e;
        }
    }

    public int countFiltered(EntityManager em, MedicineType medicineType, int filter) {
        try {
            StringBuilder jpql = new StringBuilder("select count(m) from Medicine m where 1=1");
            Map<String, Object> params = new HashMap<>();

            appendMedicineTypeCondition(jpql, params, medicineType);
            appendFilterCondition(jpql, filter);

            TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
            params.forEach(query::setParameter);

            Long total = query.getSingleResult();
            return total == null ? 0 : total.intValue();
        } catch (Exception e) {
            log.error("event=medicine_count_filtered_failed medicineType={} filter={} errorMessage={}", medicineType, filter, e.getMessage());
            throw e;
        }
    }

    public List<Medicine> findFilteredByPage(EntityManager em, int skip, int pageSize, MedicineType medicineType, int filter) {
        try {
            StringBuilder jpql = new StringBuilder("select m from Medicine m where 1=1");
            Map<String, Object> params = new HashMap<>();

            appendMedicineTypeCondition(jpql, params, medicineType);
            appendFilterCondition(jpql, filter);
            jpql.append(" order by m.id desc");

            TypedQuery<Medicine> query = em.createQuery(jpql.toString(), Medicine.class)
                    .setFirstResult(skip)
                    .setMaxResults(pageSize);
            params.forEach(query::setParameter);

            return query.getResultList();
        } catch (Exception e) {
            log.error("event=medicine_find_filtered_by_page_failed skip={} pageSize={} medicineType={} filter={} errorMessage={}", skip, pageSize, medicineType, filter, e.getMessage());
            throw e;
        }
    }

    public List<Medicine> findFilteredAndSearchByName(EntityManager em, MedicineType medicineType, int filter, String keyword, int maxResults) {
        try {
            StringBuilder jpql = new StringBuilder("select m from Medicine m where 1=1");
            Map<String, Object> params = new HashMap<>();

            appendMedicineTypeCondition(jpql, params, medicineType);
            appendFilterCondition(jpql, filter);

            if (keyword != null) {
                jpql.append(" and m.nameWithoutAccents like :keyword");
                params.put("keyword", "%" + keyword + "%");
            }

            jpql.append(" order by m.id desc");
            TypedQuery<Medicine> query = em.createQuery(jpql.toString(), Medicine.class)
                    .setMaxResults(maxResults);
            params.forEach(query::setParameter);
            return query.getResultList();
        } catch (Exception e) {
            log.error("event=medicine_find_filtered_and_search_name_failed medicineType={} filter={} keyword={} maxResults={} errorMessage={}", medicineType, filter, keyword, maxResults, e.getMessage());
            throw e;
        }
    }

    public List<Medicine> findAllFiltered(EntityManager em, MedicineType medicineType, int filter) {
        try {
            StringBuilder jpql = new StringBuilder("select m from Medicine m where 1=1");
            Map<String, Object> params = new HashMap<>();

            appendMedicineTypeCondition(jpql, params, medicineType);
            appendFilterCondition(jpql, filter);
            jpql.append(" order by m.id desc");

            TypedQuery<Medicine> query = em.createQuery(jpql.toString(), Medicine.class);
            params.forEach(query::setParameter);

            return query.getResultList();
        } catch (Exception e) {
            log.error("event=medicine_find_all_filtered_failed medicineType={} filter={} errorMessage={}", medicineType, filter, e.getMessage());
            throw e;
        }
    }

    private void appendMedicineTypeCondition(StringBuilder jpql, Map<String, Object> params, MedicineType medicineType) {
        if (medicineType != null && medicineType != MedicineType.ALL) {
            jpql.append(" and m.medicineType = :medicineType");
            params.put("medicineType", medicineType);
        }
    }

    private void appendFilterCondition(StringBuilder jpql, int filter) {
        switch (filter) {
            case 0 -> jpql.append(" and m.deletedAt is null");
            case 1 -> jpql.append(" and m.deletedAt is not null");
            case 2 -> jpql.append(" and m.deletedAt is null and coalesce(m.totalQuantity, 0) > 0");
            case 3 ->
                    jpql.append(" and m.deletedAt is null and coalesce(m.totalQuantity, 0) > 0 and coalesce(m.totalQuantity, 0) <= 50");
            case 4 -> jpql.append(" and m.deletedAt is null and coalesce(m.totalQuantity, 0) = 0");
            default -> throw new IllegalArgumentException("Giá trị filter không hợp lệ: " + filter);
        }
    }

    public String[] findAllUnits(EntityManager em) {
        List<String> units = em.createNativeQuery("select unit_name from tbl_unit", String.class)
                .getResultList();
        return units.toArray(String[]::new);
    }

    public String[] findAllRouteUse(EntityManager em) {
        List<String> routeUse = em.createNativeQuery("select name from tbl_route_use", String.class)
                .getResultList();
        return routeUse.toArray(String[]::new);
    }

    public String[] findAllQuantityStandards(EntityManager em) {
        List<String> standards = em.createNativeQuery("select name from tbl_quality_standards", String.class)
                .getResultList();
        return standards.toArray(String[]::new);
    }

    public String[] findAllDosage(EntityManager em) {
        List<String> dosage = em.createNativeQuery("select name from tbl_dosage", String.class)
                .getResultList();
        return dosage.toArray(String[]::new);
    }
}
