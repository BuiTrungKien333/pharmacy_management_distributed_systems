package com.pharmacy.repository;

import com.pharmacy.shared.dto.request.BatchSellRequest;
import com.pharmacy.shared.dto.request.MedicineBatchToSellRequest;
import com.pharmacy.shared.util.enums.BatchStatus;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SellRepository {

    public MedicineBatchToSellRequest getMedicineAndBatchToSellByBarcode(EntityManager em, String barcode) {
        try {
            List<Object[]> rows = em.createNativeQuery("""
                            select b.batch_number, b.expiration_date, b.remaining_quantity, b.selling_price,
                            m.id as medicine_id, m.medicine_name, m.measuring_unit, m.avatar_url, m.deleted_at, b.id as batch_id
                            from tbl_batch b
                            join tbl_medicine m on b.medicine_id = m.id
                            where m.barcode = :barcode
                            and b.batch_status = :batch_status
                            and b.remaining_quantity > 0
                            and b.expiration_date > :expiration_date
                            order by b.expiration_date asc
                            """)
                    .setParameter("barcode", barcode)
                    .setParameter("batch_status", BatchStatus.SELLING.name())
                    .setParameter("expiration_date", LocalDate.now().plusMonths(1))
                    .getResultList();

            if (rows.isEmpty()) {
                return null;
            }

            MedicineBatchToSellRequest request = null;
            List<BatchSellRequest> batchSellRequestList = new ArrayList<>();
            int totalQuantity = 0;

            for (Object[] row : rows) {
                if (request == null) {
                    request = new MedicineBatchToSellRequest();

                    request.setId(((Number) row[4]).longValue());
                    request.setMedicineName((String) row[5]);
                    request.setBarcode(barcode);
                    request.setMeasuringUnit((String) row[6]);
                    request.setAvatarUrl((String) row[7]);
                    request.setDeletedAt((LocalDateTime) row[8]);
                }

                Long batchId = (Long) row[9];
                String batchNumber = (String) row[0];
                LocalDate expirationDate = (LocalDate) row[1];
                int remainingQuantity = ((Number) row[2]).intValue();
                double sellingPrice = ((Number) row[3]).doubleValue();

                batchSellRequestList.add(BatchSellRequest.builder()
                        .batchId(batchId)
                        .batchNumber(batchNumber)
                        .expirationDate(expirationDate)
                        .remainingQuantity(remainingQuantity)
                        .sellingPrice(sellingPrice)
                        .build());

                totalQuantity += remainingQuantity;
            }

            if (request != null) {
                request.setBatchSellRequestList(batchSellRequestList);
                request.setTotalQuantity(totalQuantity);
            }

            return request;
        } catch (Exception e) {
            log.error("event=sell_find_medicine_and_batch_failed barcode={} errorMessage={}", barcode, e.getMessage());
            throw e;
        }
    }

}
