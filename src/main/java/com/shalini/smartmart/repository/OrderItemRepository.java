package com.shalini.smartmart.repository;

import com.shalini.smartmart.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
    boolean existsByProductId(Long productId);

    @Query("SELECT oi.product.id, oi.product.name, oi.product.category.name, " +
           "SUM(oi.quantity), SUM(oi.price * oi.quantity) " +
           "FROM OrderItem oi WHERE oi.order.status != 'Cancelled' " +
           "GROUP BY oi.product.id, oi.product.name, oi.product.category.name " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findBestSellingProducts();
}
