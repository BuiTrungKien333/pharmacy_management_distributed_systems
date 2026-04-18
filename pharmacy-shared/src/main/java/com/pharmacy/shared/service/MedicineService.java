package com.pharmacy.shared.service;

import com.pharmacy.shared.dto.request.MedicineRequest;
import com.pharmacy.shared.dto.response.MedicineResponse;
import com.pharmacy.shared.util.enums.MedicineType;
import com.pharmacy.shared.util.Pagination;

import java.util.List;

public interface MedicineService {

    int getTotalMedicine();

    int getTotalMedicineFiltered(MedicineType medicineType, int filter);

    boolean addMedicine(MedicineRequest medicineRequest);

    boolean updateMedicine(MedicineRequest medicineRequest);

    boolean checkExistsRegistrationNumber(String registrationNumber);

    boolean checkExistsBarcode(String barcode);

    MedicineResponse getMedicineByBarcode(String barcode);

    List<MedicineResponse> getAllMedicineByPage(Pagination pagination);

    List<MedicineResponse> getMedicineFilteredByTypeAndDeletedAndTotalQty(Pagination pagination, MedicineType medicineType, int filter);

    List<MedicineResponse> getMedicineFilteredAndSearchByMedicineName(MedicineType medicineType, int filter, String keyword);

    List<MedicineResponse> getAllMedicineToExportCSV(MedicineType medicineType, int filter);

    String[] getUnit();

    String[] getRouteUse();

    String[] getQuantityStandards();

    String[] getDosage();


}
