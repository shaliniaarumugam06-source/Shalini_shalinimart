package com.shalini.smartmart.service;

import com.shalini.smartmart.entity.Cart;
import com.shalini.smartmart.entity.CartItem;
import com.shalini.smartmart.entity.Product;
import com.shalini.smartmart.entity.User;
import com.shalini.smartmart.repository.CartItemRepository;
import com.shalini.smartmart.repository.CartRepository;
import com.shalini.smartmart.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    public Cart getCartForUser(User user) {
        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart cart = new Cart(user);
            return cartRepository.save(cart);
        });
    }

    public Cart addToCart(User user, Long productId, int quantity) throws Exception {
        if (quantity <= 0) {
            throw new Exception("Invalid quantity.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new Exception("Product not found."));

        if (product.getStock() <= 0) {
            throw new Exception("Product '" + product.getName() + "' is out of stock.");
        }

        Cart cart = getCartForUser(user);
        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartAndProduct(cart, product);

        if (existingItemOpt.isPresent()) {
            CartItem item = existingItemOpt.get();
            int newQuantity = item.getQuantity() + quantity;
            if (newQuantity > product.getStock()) {
                throw new Exception("Cannot add " + quantity + " more. Total requested (" + newQuantity + ") exceeds available stock (" + product.getStock() + ").");
            }
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            if (quantity > product.getStock()) {
                throw new Exception("Requested quantity (" + quantity + ") exceeds available stock (" + product.getStock() + ").");
            }
            CartItem newItem = new CartItem(cart, product, quantity);
            cartItemRepository.save(newItem);
            cart.getItems().add(newItem);
        }

        return cartRepository.save(cart);
    }

    public Cart updateQuantity(User user, Long cartItemId, int newQuantity) throws Exception {
        Cart cart = getCartForUser(user);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new Exception("Cart item not found."));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new Exception("Unauthorized cart modification.");
        }

        if (newQuantity <= 0) {
            return removeFromCart(user, cartItemId);
        }

        if (newQuantity > item.getProduct().getStock()) {
            throw new Exception("Quantity cannot exceed available stock (" + item.getProduct().getStock() + ").");
        }

        item.setQuantity(newQuantity);
        cartItemRepository.save(item);
        return getCartForUser(user);
    }

    public Cart removeFromCart(User user, Long cartItemId) throws Exception {
        Cart cart = getCartForUser(user);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new Exception("Cart item not found."));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new Exception("Unauthorized cart modification.");
        }

        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        return cartRepository.save(cart);
    }

    public void clearCart(Cart cart) {
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}
