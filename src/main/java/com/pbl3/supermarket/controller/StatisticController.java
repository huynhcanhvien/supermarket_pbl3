package com.pbl3.supermarket.controller;

import com.pbl3.supermarket.dto.response.ApiResponse;
import com.pbl3.supermarket.dto.response.CategorySales;
import com.pbl3.supermarket.dto.response.DailyRevenueResponse;
import com.pbl3.supermarket.exception.AppException;
import com.pbl3.supermarket.exception.ErrorCode;
import com.pbl3.supermarket.service.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/statistic")
public class StatisticController {
    @Autowired
    StatisticService statisticService;

    @GetMapping("/revenue")
    public ApiResponse<List<DailyRevenueResponse>> getRevenueByMonth(@RequestParam("month") int month, @RequestParam("year") int year){
        return ApiResponse.<List<DailyRevenueResponse>>builder()
                .message("Revenue by month: "+month+",year"+year)
                .result(statisticService.getRevenueByMonth(month, year))
                .build();
    }

    @GetMapping("/revenue/date")
    public ApiResponse<DailyRevenueResponse> getRevenueByMonth(@RequestParam("day") int day, @RequestParam("month") int month, @RequestParam("year") int year){
        return ApiResponse.<DailyRevenueResponse>builder()
                .message("Revenue by day: "+day+",month: "+month+",year"+year)
                .result(statisticService.getRevenueByDay(day, month, year))
                .build();
    }
    @GetMapping("/category")
    public ApiResponse<List<CategorySales>> getProductCountByCategory(@RequestParam(value = "day", required = false)
    Optional<Integer> day, @RequestParam("month") int month, @RequestParam("year") int year){
        if (day.isPresent()) {
            return ApiResponse.<List<CategorySales>>builder()
                    .message("Get product count by category by day: "+day.get()+", month: "+month+", year: "+year)
                    .result(statisticService.getProductCountByCategory(day.get(), month, year))
                    .build();
        }
        return ApiResponse.<List<CategorySales>>builder()
                .message("Get product count by category by month: "+month+", year: "+year)
                .result(statisticService.getProductCountByCategory(month, year))
                .build();

    }
}
