package com.pharmacy.service.impl;

import com.pharmacy.entity.Batch;
import com.pharmacy.entity.Employee;
import com.pharmacy.entity.Medicine;
import com.pharmacy.entity.Supplier;
import com.pharmacy.persistence.JpaTransactionTemplate;
import com.pharmacy.repository.BatchRepository;
import com.pharmacy.shared.dto.request.BatchCreateRequest;
import com.pharmacy.shared.dto.request.BatchUpdateRequest;
import com.pharmacy.shared.dto.response.BatchAllResponse;
import com.pharmacy.shared.dto.response.BatchResponse;
import com.pharmacy.shared.service.BatchService;
import com.pharmacy.shared.util.Pagination;
import com.pharmacy.shared.util.enums.BatchStatus;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
public class BatchServiceImpl implements BatchService {

    private static final DateTimeFormatter BATCH_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");
    private static final DateTimeFormatter UI_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final BatchRepository batchRepository;

    @Override
    public boolean addBatch(BatchCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Thông tin lô không được để trống.");
        }

        LocalDateTime now = LocalDateTime.now();

        return JpaTransactionTemplate.execute(em -> {
            Batch batch = Batch.builder()
                    .manufacturingDate(request.getManufacturingDate())
                    .expirationDate(request.getExpirationDate())
                    .importDate(now)
                    .importQuantity(request.getImportQuantity())
                    .remainingQuantity(request.getImportQuantity())
                    .importPrice(request.getImportPrice())
                    .totalAmount(request.getImportPrice() * request.getImportQuantity())
                    .sellingPrice(request.getSellingPrice())
                    .batchStatus(BatchStatus.SELLING)
                    .medicine(em.getReference(Medicine.class, request.getMedicineId()))
                    .supplier(em.getReference(Supplier.class, request.getSupplierId()))
                    .employee(em.getReference(Employee.class, request.getEmployeeId()))
                    .build();

            batchRepository.save(em, batch);
            em.flush();

            batch.setBatchNumber(buildBatchNumber(request.getMedicineId(), now.toLocalDate(), batch.getId()));
            batchRepository.update(em, batch);
            return true;
        });
    }

    @Override
    public boolean updateBatch(BatchUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Thông tin lô không được để trống.");
        }
        if (request.getId() == null) {
            throw new IllegalArgumentException("Id lô không được để trống.");
        }
        if (request.getMedicineId() == null || request.getSupplierId() == null || request.getEmployeeId() == null) {
            throw new IllegalArgumentException("Thông tin liên kết thuốc, nhà cung cấp và nhân viên không được để trống.");
        }

        checkDate(request.getManufacturingDate(), request.getExpirationDate());
        checkQuantityAndPrice(
                request.getImportPrice(),
                request.getSellingPrice(),
                request.getRemainingQuantity(),
                request.getImportQuantity()
        );

        return JpaTransactionTemplate.execute(em -> {
            Batch existingBatch = batchRepository.findById(em, request.getId());
            if (existingBatch == null) {
                throw new IllegalArgumentException("Không tìm thấy lô với id=" + request.getId());
            }

            existingBatch.setManufacturingDate(request.getManufacturingDate());
            existingBatch.setExpirationDate(request.getExpirationDate());
            existingBatch.setImportQuantity(request.getImportQuantity());
            existingBatch.setRemainingQuantity(request.getRemainingQuantity());
            existingBatch.setImportPrice(request.getImportPrice());
            existingBatch.setTotalAmount(request.getImportPrice() * request.getImportQuantity());
            existingBatch.setSellingPrice(request.getSellingPrice());
            existingBatch.setBatchStatus(request.getBatchStatus() == null ? BatchStatus.SELLING : request.getBatchStatus());
            existingBatch.setMedicine(em.getReference(Medicine.class, request.getMedicineId()));
            existingBatch.setSupplier(em.getReference(Supplier.class, request.getSupplierId()));
            existingBatch.setEmployee(em.getReference(Employee.class, request.getEmployeeId()));

            batchRepository.update(em, existingBatch);
            return true;
        });
    }

    @Override
    public BatchResponse getBatchById(Long id) {
        return JpaTransactionTemplate.execute(em -> batchRepository.findResponseById(em, id));
    }

    @Override
    public BatchAllResponse getBatchAllFieldById(Long id) {
        return JpaTransactionTemplate.execute(em -> batchRepository.findResponseAllFieldById(em, id));
    }

    @Override
    public BatchResponse getBatchByBatchNumber(String batchNumber) {
        String normalizedBatchNumber = normalize(batchNumber);
        if (normalizedBatchNumber == null) {
            throw new IllegalArgumentException("Batch number không được để trống.");
        }

        return JpaTransactionTemplate.execute(em -> batchRepository.findResponseByBatchNumber(em, normalizedBatchNumber));
    }

    @Override
    public int getTotalBatch() {
        return JpaTransactionTemplate.execute(batchRepository::countAll);
    }

    @Override
    public List<BatchResponse> getAllBatchByPage(Pagination page, int sortOption) {
        validatePagination(page);
        validateSortOption(sortOption);

        return JpaTransactionTemplate.execute(em ->
                batchRepository.findAllByPage(em, page.getSkip(), page.getPageSize(), sortOption)
        );
    }

    @Override
    public int countRecordFilteredByStatusAndDate(int type, int filter, LocalDate dateFrom, LocalDate toDate) {
        validateType(type);

        BatchStatus batchStatus = mapTypeToBatchStatus(type);
        return JpaTransactionTemplate.execute(em ->
                batchRepository.countFilteredByStatusAndDate(em, batchStatus, filter, dateFrom, toDate)
        );
    }

    @Override
    public List<BatchResponse> getBatchByStatusAndDate(Pagination page, int type, int filter, LocalDate fromDate, LocalDate toDate, int sortOption) {
        validatePagination(page);
        validateSortOption(sortOption);
        validateType(type);

        BatchStatus batchStatus = mapTypeToBatchStatus(type);
        List<BatchResponse> execute = JpaTransactionTemplate.execute(em -> batchRepository.findFilteredByStatusAndDate(
                em,
                page.getSkip(),
                page.getPageSize(),
                batchStatus,
                filter,
                fromDate,
                toDate,
                sortOption
        ));
        return execute;
    }

    @Override
    public int countRecordFilteredByStatusAndDateAndSearchByBatchNumber(int type, int filter, LocalDate dateFrom, LocalDate toDate, String keyword) {
        validateType(type);

        BatchStatus batchStatus = mapTypeToBatchStatus(type);
        String normalizedKeyword = normalize(keyword);

        return JpaTransactionTemplate.execute(em ->
                batchRepository.countFilteredByStatusAndDateAndSearchByBatchNumber(
                        em,
                        batchStatus,
                        filter,
                        dateFrom,
                        toDate,
                        normalizedKeyword
                )
        );
    }

    @Override
    public List<BatchResponse> getBatchByStatusAndDateAndSearchByBatchNumber(Pagination page, int type, int filter, LocalDate fromDate, LocalDate toDate, int sortOption, String keyword) {
        validatePagination(page);
        validateSortOption(sortOption);
        validateType(type);

        BatchStatus batchStatus = mapTypeToBatchStatus(type);
        String normalizedKeyword = normalize(keyword);

        return JpaTransactionTemplate.execute(em ->
                batchRepository.findFilteredByStatusAndDateAndSearchByBatchNumber(
                        em,
                        page.getSkip(),
                        page.getPageSize(),
                        batchStatus,
                        filter,
                        fromDate,
                        toDate,
                        sortOption,
                        normalizedKeyword
                )
        );
    }

    @Override
    public int countRecordFilteredByStatusAndDateAndSearchByBarcode(int type, int filter, LocalDate dateFrom, LocalDate toDate, String barcode) {
        validateType(type);

        BatchStatus batchStatus = mapTypeToBatchStatus(type);
        String normalizedBarcode = normalize(barcode);

        return JpaTransactionTemplate.execute(em ->
                batchRepository.countFilteredByStatusAndDateAndSearchByBarcode(
                        em,
                        batchStatus,
                        filter,
                        dateFrom,
                        toDate,
                        normalizedBarcode
                )
        );
    }

    @Override
    public List<BatchResponse> getBatchByStatusAndDateAndSearchByBarcode(Pagination page, int type, int filter, LocalDate fromDate, LocalDate toDate, int sortOption, String barcode) {
        validatePagination(page);
        validateSortOption(sortOption);
        validateType(type);

        BatchStatus batchStatus = mapTypeToBatchStatus(type);
        String normalizedBarcode = normalize(barcode);

        return JpaTransactionTemplate.execute(em ->
                batchRepository.findFilteredByStatusAndDateAndSearchByBarcode(
                        em,
                        page.getSkip(),
                        page.getPageSize(),
                        batchStatus,
                        filter,
                        fromDate,
                        toDate,
                        sortOption,
                        normalizedBarcode
                )
        );
    }

    @Override
    public List<BatchResponse> getAllBatchToExportCSV(int type, int filter, LocalDate fromDate, LocalDate toDate, int sortOption) {
        return List.of();
    }

    @Override
    public void updateBatchStatusWhenExpired() {
        LocalDate today = LocalDate.now();
        JpaTransactionTemplate.execute(em -> {
            batchRepository.updateExpiredBatchStatus(em, today);
            return null;
        });
    }

    @Override
    public LocalDate convertStringToLocalDate(int filter, String text) {
        if (filter != 4) {
            return LocalDate.now();
        }

        if (text == null || text.isBlank()) {
            return null;
        }

        String trimmed = text.trim();
        if (trimmed.contains("/")) {
            return LocalDate.parse(trimmed, UI_DATE_FORMATTER);
        }

        return LocalDate.parse(trimmed);
    }

    @Override
    public double calculateMoney(String quantity, String price) {
        if (quantity.isEmpty() || price.isEmpty())
            return 0;

        try {
            return Integer.parseInt(quantity) * Double.parseDouble(price);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public boolean checkDate(LocalDate manufacturingDate, LocalDate expirationDate) {
        LocalDate today = LocalDate.now();

        if (!manufacturingDate.isBefore(expirationDate))
            throw new IllegalArgumentException("Ngày sản xuất phải trước hạn sử dụng!");

        if (!manufacturingDate.isBefore(today))
            throw new IllegalArgumentException("Ngày sản xuất phải trước ngày hôm nay!");

        if (!expirationDate.isAfter(today))
            throw new IllegalArgumentException("Hạn sử dụng phải sau ngày hôm nay!");

        return true;
    }

    @Override
    public boolean checkQuantityAndPrice(double importPrice, double sellPrice, int totalQuantity, int importQuantity) {
        if (importPrice < 0 || sellPrice < 0)
            throw new IllegalArgumentException("Giá không được < 0");

        if (importPrice > sellPrice)
            throw new IllegalArgumentException("Giá bán ra phải lớn hơn hoặc bằng giá nhập.");

        if (totalQuantity < 0 || importQuantity < 0)
            throw new IllegalArgumentException("Số lượng không được < 0");

        if (totalQuantity > importQuantity)
            throw new IllegalArgumentException("Số lượng còn không được lớn hơn số lượng nhập.");

        return true;
    }

    private void validatePagination(Pagination page) {
        if (page == null) {
            throw new IllegalArgumentException("Thông tin phân trang không được để trống.");
        }
        if (page.getPageNumber() <= 0) {
            throw new IllegalArgumentException("pageNumber phải lớn hơn 0.");
        }
        if (page.getPageSize() <= 0) {
            throw new IllegalArgumentException("pageSize phải lớn hơn 0.");
        }
    }

    private void validateSortOption(int sortOption) {
        if (sortOption < 0 || sortOption > 1) {
            throw new IllegalArgumentException("sortOption chỉ nhận 0 hoặc 1.");
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String buildBatchNumber(Long medicineId, LocalDate importDate, Long batchId) {
        String datePart = importDate.format(BATCH_DATE_FORMATTER);
        String seq = String.format("%03d", batchId);
        return "LO-" + medicineId + "-" + datePart + "-" + seq;
    }

    private BatchStatus mapTypeToBatchStatus(int type) {
        return switch (type) {
            case 0 -> null;
            case 1 -> BatchStatus.SELLING;
            case 2 -> BatchStatus.CANCELLED;
            case 3 -> BatchStatus.SOLD_OUT;
            case 4 -> BatchStatus.EXPIRED;
            default -> throw new IllegalArgumentException("type không hợp lệ: " + type);
        };
    }

    private void validateType(int type) {
        if (type < 0 || type > 4) {
            throw new IllegalArgumentException("type chỉ nhận từ 0 đến 4.");
        }
    }
}

