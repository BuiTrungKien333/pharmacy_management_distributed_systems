package com.pharmacy.shared.service;

import com.pharmacy.shared.dto.request.BatchCreateRequest;
import com.pharmacy.shared.dto.request.BatchUpdateRequest;
import com.pharmacy.shared.dto.response.BatchAllResponse;
import com.pharmacy.shared.dto.response.BatchResponse;
import com.pharmacy.shared.util.Pagination;

import java.time.LocalDate;
import java.util.List;

public interface BatchService {

    boolean addBatch(BatchCreateRequest request);

    boolean updateBatch(BatchUpdateRequest request);

    int getTotalBatch();

    BatchResponse getBatchById(Long id);

    BatchAllResponse getBatchAllFieldById(Long id);

    BatchResponse getBatchByBatchNumber(String batchNumber);

    List<BatchResponse> getAllBatchByPage(Pagination page, int option);

    int countRecordFilteredByStatusAndDate(int type, int filter, LocalDate dateFrom, LocalDate toDate);

    List<BatchResponse> getBatchByStatusAndDate(Pagination page, int type, int filter, LocalDate fromDate, LocalDate toDate, int sortOption);

    int countRecordFilteredByStatusAndDateAndSearchByBatchNumber(int type, int filter, LocalDate dateFrom, LocalDate toDate, String keyword);

    List<BatchResponse> getBatchByStatusAndDateAndSearchByBatchNumber(Pagination page, int type, int filter, LocalDate fromDate, LocalDate toDate, int sortOption, String keyword);

    int countRecordFilteredByStatusAndDateAndSearchByBarcode(int type, int filter, LocalDate dateFrom, LocalDate toDate, String barcode);

    List<BatchResponse> getBatchByStatusAndDateAndSearchByBarcode(Pagination page, int type, int filter, LocalDate fromDate, LocalDate toDate, int sortOption, String barcode);

    List<BatchResponse> getAllBatchToExportCSV(int type, int filter, LocalDate fromDate, LocalDate toDate, int sortOption);

    LocalDate convertStringToLocalDate(int filter, String text);

    double calculateMoney(String quantity, String price);

    boolean checkDate(LocalDate manufacturingDate, LocalDate expirationDate);

    boolean checkQuantityAndPrice(double importPrice, double sellPrice, int totalQuantity, int importQuantity);

    void updateBatchStatusWhenExpired();
}
