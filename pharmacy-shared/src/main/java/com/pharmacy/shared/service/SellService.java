package com.pharmacy.shared.service;

import com.pharmacy.shared.dto.request.MedicineBatchToSellRequest;
import com.pharmacy.shared.dto.request.InvoiceRequest;
import com.pharmacy.shared.dto.response.InvoiceResponse;

import java.util.List;

public interface SellService {

    MedicineBatchToSellRequest getMedicineAndBatchToSellByBarcode(String barcode);

    MedicineBatchToSellRequest processAddToCart(MedicineBatchToSellRequest request, int sellingQuantity);

    InvoiceResponse processPayment(InvoiceRequest request, List<MedicineBatchToSellRequest> sellRequestList);

    double calculateMoneyChange(String str, double priceToPayment);

    double calculateTotalMoneyToPayment(double totalPrice, double discountPrice);

    double calculateTotalMoneyByMedicineAndBatch(List<MedicineBatchToSellRequest> list);

}
