package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ReportService {
    /**
     * 获取营业额统计
     * @return
     */
    TurnoverReportVO getTurnoverStatistics(LocalDate start, LocalDate end);

    /**
     * 获取用户统计
     * @return
     */
    UserReportVO getUserStatistics(LocalDate start, LocalDate end);

    /**
     * 获取订单统计
     * @param begin
     * @param end
     * @return
     */
    OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end);

    /**
     * 获取销售额前10的商品统计
     * @param begin
     * @param end
     * @return
     */
    SalesTop10ReportVO getSalesTop10Statistics(LocalDate begin, LocalDate end);
}
