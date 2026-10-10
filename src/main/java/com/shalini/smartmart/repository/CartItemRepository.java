package com.shalini.smartmart.repository;

import com.shalini.smartmart.entity.Cart;
import com.shalini.smartmart.entity.CartItem;
import com.shalini.smartmart.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}
