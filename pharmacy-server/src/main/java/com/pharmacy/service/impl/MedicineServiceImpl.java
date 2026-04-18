package com.pharmacy.service.impl;

import com.pharmacy.entity.Medicine;
import com.pharmacy.mapper.DataMapper;
import com.pharmacy.persistence.JpaTransactionTemplate;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.shared.dto.request.MedicineRequest;
import com.pharmacy.shared.dto.response.MedicineResponse;
import com.pharmacy.shared.service.MedicineService;
import com.pharmacy.shared.util.Pagination;
import com.pharmacy.shared.util.enums.MedicineType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class MedicineServiceImpl implements MedicineService {

    private static final int SEARCH_LIMIT = 50;

    private final MedicineRepository medicineRepository;
    private final DataMapper dataMapper;

    @Override
    public int getTotalMedicine() {
        return JpaTransactionTemplate.execute(medicineRepository::countAll);
    }

    @Override
    public boolean addMedicine(MedicineRequest medicineRequest) {
        if (medicineRequest == null) {
            throw new IllegalArgumentException("Thông tin thuốc không được để trống.");
        }

        String barcode = normalize(medicineRequest.getBarcode());
        String registrationNumber = normalize(medicineRequest.getRegistrationNumber());

        try {
            return JpaTransactionTemplate.execute(em -> {
                if (medicineRepository.existsByBarcode(em, barcode)) {
                    log.info("event=add_medicine_duplicate_barcode barcode={}", barcode);
                    throw new IllegalArgumentException("Barcode này đã tồn tại");
                }

                if (medicineRepository.existsByRegistrationNumber(em, registrationNumber)) {
                    log.info("event=add_medicine_duplicate_registration_number registrationNumber={}", registrationNumber);
                    throw new IllegalArgumentException("Số đăng kí này đã tồn tại");
                }

                Medicine medicine = dataMapper.toEntity(medicineRequest);
                medicineRepository.save(em, medicine);
                return true;
            });
        } catch (Exception e) {
            log.error("event=add_medicine_failed barcode={} errorMessage={}", barcode, e.getMessage(), e);
            throw new RuntimeException("Không thể thêm mới thuốc: " + e.getMessage());
        }
    }

    @Override
    public boolean updateMedicine(MedicineRequest medicineRequest) {
        if (medicineRequest == null) {
            throw new IllegalArgumentException("Thông tin thuốc không được để trống.");
        }

        String barcode = normalize(medicineRequest.getBarcode());
        String registrationNumber = normalize(medicineRequest.getRegistrationNumber());

        try {
            return JpaTransactionTemplate.execute(em -> {
                Medicine existing = medicineRepository.findByBarcode(em, barcode);
                if (existing == null) {
                    log.info("event=update_medicine_not_found barcode={}", barcode);
                    throw new IllegalArgumentException("Không tìm thấy thuốc với barcode=" + medicineRequest.getBarcode());
                }

                if (medicineRepository.existsByRegistrationNumberExcludingBarcode(em, registrationNumber, barcode)) {
                    log.info("event=update_medicine_duplicate_registration_number registrationNumber={} barcode={}", registrationNumber, barcode);
                    throw new IllegalArgumentException("Số đăng kí này đã tồn tại");
                }

                dataMapper.updateEntityFromRequest(medicineRequest, existing);
                medicineRepository.update(em, existing);
                return true;
            });
        } catch (Exception e) {
            log.error("event=update_medicine_failed barcode={} errorMessage={}", barcode, e.getMessage(), e);
            throw new RuntimeException("Không thể cập nhật thông tin thuốc: " + e.getMessage());
        }
    }

    @Override
    public boolean checkExistsRegistrationNumber(String registrationNumber) {
        try {
            return JpaTransactionTemplate.execute(em -> medicineRepository.existsByRegistrationNumber(em, registrationNumber));
        } catch (Exception e) {
            log.error("event=check_exists_registration_number_failed registrationNumber={} errorMessage={}", registrationNumber, e.getMessage(), e);
            throw new RuntimeException("Không thể kiểm tra số đăng ký thuốc.", e);
        }
    }

    @Override
    public boolean checkExistsBarcode(String barcode) {
        try {
            return JpaTransactionTemplate.execute(em -> medicineRepository.existsByBarcode(em, barcode));
        } catch (Exception e) {
            log.error("event=check_exists_barcode_failed barcode={} errorMessage={}", barcode, e.getMessage(), e);
            throw new RuntimeException("Không thể kiểm tra barcode thuốc.", e);
        }
    }

    @Override
    public MedicineResponse getMedicineByBarcode(String barcode) {
        String normalizedBarcode = normalize(barcode);
        if (normalizedBarcode == null) {
            throw new IllegalArgumentException("Barcode không được để trống.");
        }

        try {
            return JpaTransactionTemplate.execute(em -> {
                Medicine medicine = medicineRepository.findByBarcode(em, normalizedBarcode);
                return medicine == null ? null : dataMapper.toMedicineResponse(medicine);
            });
        } catch (Exception e) {
            log.error("event=get_medicine_by_barcode_failed barcode={} errorMessage={}", normalizedBarcode, e.getMessage(), e);
            throw new RuntimeException("Không thể lấy thông tin thuốc theo barcode.", e);
        }
    }

    @Override
    public List<MedicineResponse> getAllMedicineByPage(Pagination pagination) {
        validatePagination(pagination);

        try {
            return JpaTransactionTemplate.execute(em -> {
                List<Medicine> medicines = medicineRepository.findAllByPage(em, pagination.getSkip(), pagination.getPageSize());
                return dataMapper.toMedicineResponses(medicines);
            });
        } catch (Exception e) {
            log.error("event=get_all_medicine_by_page_failed pagination={} errorMessage={}", pagination, e.getMessage(), e);
            throw new RuntimeException("Không thể lấy danh sách thuốc theo trang.", e);
        }
    }

    @Override
    public int getTotalMedicineFiltered(MedicineType medicineType, int filter) {
        MedicineType normalizedType = normalizeMedicineType(medicineType);
        validateFilter(filter);

        try {
            return JpaTransactionTemplate.execute(em -> medicineRepository.countFiltered(em, normalizedType, filter));
        } catch (Exception e) {
            log.error("event=get_total_medicine_filtered_failed medicineType={} filter={} errorMessage={}",
                    normalizedType, filter, e.getMessage(), e);
            throw new RuntimeException("Không thể lấy tổng số thuốc đã lọc.", e);
        }
    }

    @Override
    public List<MedicineResponse> getMedicineFilteredByTypeAndDeletedAndTotalQty(Pagination pagination, MedicineType medicineType, int filter) {
        validatePagination(pagination);
        MedicineType normalizedType = normalizeMedicineType(medicineType);
        validateFilter(filter);

        try {
            return JpaTransactionTemplate.execute(em -> {
                List<Medicine> medicines = medicineRepository.findFilteredByPage(
                        em,
                        pagination.getSkip(),
                        pagination.getPageSize(),
                        normalizedType,
                        filter
                );
                return dataMapper.toMedicineResponses(medicines);
            });
        } catch (Exception e) {
            log.error("event=get_medicine_filtered_by_type_deleted_qty_failed pagination={} medicineType={} filter={} errorMessage={}",
                    pagination, normalizedType, filter, e.getMessage(), e);
            throw new RuntimeException("Không thể lọc danh sách thuốc theo điều kiện.", e);
        }
    }

    @Override
    public List<MedicineResponse> getMedicineFilteredAndSearchByMedicineName(MedicineType medicineType, int filter, String keyword) {
        MedicineType normalizedType = normalizeMedicineType(medicineType);
        validateFilter(filter);
        String normalizedKeyword = normalize(keyword);

        try {
            return JpaTransactionTemplate.execute(em -> {
                List<Medicine> medicines = medicineRepository.findFilteredAndSearchByName(
                        em,
                        normalizedType,
                        filter,
                        normalizedKeyword,
                        SEARCH_LIMIT
                );
                return dataMapper.toMedicineResponses(medicines);
            });
        } catch (Exception e) {
            log.error("event=get_medicine_filtered_and_search_name_failed medicineType={} filter={} keyword={} errorMessage={}",
                    normalizedType, filter, normalizedKeyword, e.getMessage(), e);
            throw new RuntimeException("Không thể lọc và tìm kiếm thuốc theo tên.", e);
        }
    }

    @Override
    public List<MedicineResponse> getAllMedicineToExportCSV(MedicineType medicineType, int filter) {
        MedicineType normalizedType = normalizeMedicineType(medicineType);
        validateFilter(filter);

        try {
            return JpaTransactionTemplate.execute(em -> {
                List<Medicine> medicines = medicineRepository.findAllFiltered(em, normalizedType, filter);
                return dataMapper.toMedicineResponses(medicines);
            });
        } catch (Exception e) {
            log.error("event=get_all_medicine_to_export_csv_failed medicineType={} filter={} errorMessage={}",
                    normalizedType, filter, e.getMessage(), e);
            throw new RuntimeException("Không thể lấy danh sách thuốc để xuất CSV.", e);
        }
    }

    @Override
    public String[] getUnit() {
        return JpaTransactionTemplate.execute(medicineRepository::findAllUnits);
    }

    @Override
    public String[] getRouteUse() {
        return JpaTransactionTemplate.execute(medicineRepository::findAllRouteUse);
    }

    @Override
    public String[] getQuantityStandards() {
        return JpaTransactionTemplate.execute(medicineRepository::findAllQuantityStandards);
    }

    @Override
    public String[] getDosage() {
        return JpaTransactionTemplate.execute(medicineRepository::findAllDosage);
    }

    private void validatePagination(Pagination pagination) {
        if (pagination == null) {
            throw new IllegalArgumentException("Thông tin phân trang không được để trống.");
        }

        if (pagination.getPageNumber() <= 0) {
            throw new IllegalArgumentException("pageNumber phải lớn hơn 0.");
        }

        if (pagination.getPageSize() <= 0) {
            throw new IllegalArgumentException("pageSize phải lớn hơn 0.");
        }
    }

    private void validateFilter(int filter) {
        if (filter < 0 || filter > 4) {
            throw new IllegalArgumentException("filter phải nằm trong khoảng từ 0 đến 4.");
        }
    }

    private MedicineType normalizeMedicineType(MedicineType medicineType) {
        return medicineType == null ? MedicineType.ALL : medicineType;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
