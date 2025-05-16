package com.pbl3.supermarket.service;

import com.pbl3.supermarket.dto.response.CategorySales;
import com.pbl3.supermarket.dto.response.DailyRevenueResponse;
import com.pbl3.supermarket.entity.Category;
import com.pbl3.supermarket.entity.Product;
import com.pbl3.supermarket.entity.Receipt;
import com.pbl3.supermarket.entity.ReceiptProduct;
import com.pbl3.supermarket.exception.AppException;
import com.pbl3.supermarket.exception.ErrorCode;
import com.pbl3.supermarket.repository.CustomerRepository;
import com.pbl3.supermarket.repository.ProductRepository;
import com.pbl3.supermarket.repository.ReceiptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticService {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ReceiptRepository receiptRepository;


    @Autowired
    CustomerRepository customerRepository;

    public Map<String, Integer> countByCategories() {
        Map<String, Integer> result = new HashMap<>();
        for(Product p : productRepository.findAll()) {
            if(p.getCategories() != null) {
                Category c = p.getCategories().getFirst();
                result.put(c.getName(), result.getOrDefault(c.getName(), 0) + 1);
            }
        }
        return result;
    }
    public List<DailyRevenueResponse> getRevenueByMonth(int month, int year) {
        if (month < 1 || month > 12) {
            throw new AppException(ErrorCode.INVALID_ARGUMENT);
        }
        if (year < 2020 || year > LocalDate.now().getYear()) {
            throw new AppException(ErrorCode.INVALID_ARGUMENT);
        }
        List<Receipt> receipts = receiptRepository.findByMonth(month, year);
        Map<Integer, Float> sumByDay = new HashMap<>();
        for (Receipt receipt : receipts) {
            LocalDate date = receipt.getBill_date();
            int day = date.getDayOfMonth();
            sumByDay.put(day, sumByDay.getOrDefault(day, 0f) + receipt.getTotalPrice());
        }
        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();

        List<DailyRevenueResponse> result = new ArrayList<>();
        for (int day = 1; day <= daysInMonth; day++) {
            float revenue = sumByDay.getOrDefault(day, (float) 0);
            result.add(new DailyRevenueResponse(LocalDate.of(year, month, day), revenue));
        }
        result.sort(Comparator.comparing(DailyRevenueResponse::getDate));
        return result;
    }

    public DailyRevenueResponse getRevenueByDay(int day, int month, int year) {
        if (month < 1 || month > 12) {
            throw new AppException(ErrorCode.INVALID_ARGUMENT);
        }
        YearMonth nowYm = YearMonth.now();
        YearMonth inputYm = YearMonth.of(year, month);
        if (inputYm.isAfter(nowYm) || year < 2020) {
            throw new AppException(ErrorCode.INVALID_ARGUMENT);
        }
        if(day > inputYm.lengthOfMonth() || day < 1) throw new AppException(ErrorCode.INVALID_ARGUMENT);

        List<Receipt> receipts = receiptRepository.findByDay(day, month, year);
        DailyRevenueResponse result = new DailyRevenueResponse(LocalDate.of(year, month, day), (float) 0);
        for(Receipt r : receipts){
            result.setRevenue(result.getRevenue() + r.getTotalPrice());
        }
        return result;
    }
    public List<CategorySales> getProductCountByCategory(int month, int year) {
        if (month < 1 || month > 12) {
            throw new AppException(ErrorCode.INVALID_ARGUMENT);
        }
        YearMonth nowYm = YearMonth.now();
        YearMonth inputYm = YearMonth.of(year, month);
        if (inputYm.isAfter(nowYm) || year < 2020) {
            throw new AppException(ErrorCode.INVALID_ARGUMENT);
        }

        // 2. Lấy toàn bộ hóa đơn trong tháng đó
        List<Receipt> receipts = receiptRepository.findByMonth(month, year);

        // 3. Gộp số lượng sold theo category
        Map<String, Integer> countByCategory = new HashMap<>();
        for (Receipt r : receipts) {
            // Giả sử Receipt có getItems() trả về List<ReceiptItem>,
            // ReceiptItem có getProduct().getCategory().getName() và getQuantity()
            for (ReceiptProduct item : r.getReceiptProducts()) {
                String catName = item.getProduct()
                        .getCategories().getFirst()
                        .getName();
                int quantity = item.getQuantity();
                countByCategory.put(
                        catName,
                        countByCategory.getOrDefault(catName, 0) + quantity
                );
            }
        }

        // 4. Chuyển thành List<CategorySalesDto> và sort theo quantity giảm dần
        return countByCategory.entrySet().stream()
                .map(e -> new CategorySales(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingLong(CategorySales::getTotalQuantity).reversed())
                .collect(Collectors.toList());
    }
    public List<CategorySales> getProductCountByCategory(int day, int month, int year){
        if (month < 1 || month > 12) {
            throw new AppException(ErrorCode.INVALID_ARGUMENT);
        }
        YearMonth nowYm = YearMonth.now();
        YearMonth inputYm = YearMonth.of(year, month);
        if (inputYm.isAfter(nowYm) || year < 2020) {
            throw new AppException(ErrorCode.INVALID_ARGUMENT);
        }
        if(day > inputYm.lengthOfMonth() || day < 1) throw new AppException(ErrorCode.INVALID_ARGUMENT);

        // 2. Lấy toàn bộ hóa đơn trong ngày đó
        List<Receipt> receipts = receiptRepository.findByDay(day, month, year);

        // 3. Gộp số lượng sold theo category
        Map<String, Integer> countByCategory = new HashMap<>();
        for (Receipt r : receipts) {
            // Giả sử Receipt có getItems() trả về List<ReceiptItem>,
            // ReceiptItem có getProduct().getCategory().getName() và getQuantity()
            for (ReceiptProduct item : r.getReceiptProducts()) {
                String catName = item.getProduct()
                        .getCategories().getFirst()
                        .getName();
                int quantity = item.getQuantity();
                countByCategory.put(
                        catName,
                        countByCategory.getOrDefault(catName, 0) + quantity
                );
            }
        }

        // 4. Chuyển thành List<CategorySalesDto> và sort theo quantity giảm dần
        return countByCategory.entrySet().stream()
                .map(e -> new CategorySales(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingLong(CategorySales::getTotalQuantity).reversed())
                .collect(Collectors.toList());
    }
}
