package com.shalini.smartmart.repository;

import com.shalini.smartmart.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);
    List<Order> findAllByOrderByOrderDateDesc();
    Optional<Order> findByOrderNumber(String orderNumber);
    long countByUserId(Long userId);
    long countByStatus(String status);

    List<Order> findByStatusOrderByOrderDateDesc(String status);

    @Query("SELECT o FROM Order o WHERE " +
           "(LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(o.user.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "o.user.phone LIKE CONCAT('%', :keyword, '%')) " +
           "ORDER BY o.orderDate DESC")
    List<Order> searchOrders(@Param("keyword") String keyword);

    @Query("SELECT o FROM Order o WHERE " +
           "o.status = :status AND " +
           "(LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(o.user.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "o.user.phone LIKE CONCAT('%', :keyword, '%')) " +
           "ORDER BY o.orderDate DESC")
    List<Order> searchOrdersWithStatus(@Param("keyword") String keyword, @Param("status") String status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0.0) FROM Order o WHERE o.status != 'Cancelled'")
    Double calculateTotalSales();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0.0) FROM Order o WHERE o.status = 'Delivered'")
    Double calculateCompletedSales();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0.0) FROM Order o WHERE o.status = 'Cancelled'")
    Double calculateCancelledSales();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status NOT IN ('Delivered', 'Cancelled')")
    long countPendingOrders();
}
