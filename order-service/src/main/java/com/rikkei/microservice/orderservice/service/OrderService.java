package com.rikkei.microservice.orderservice.service;

import com.rikkei.microservice.orderservice.dto.OrderRequestDTO;
import com.rikkei.microservice.orderservice.dto.OrderResponseDTO;
import com.rikkei.microservice.orderservice.dto.Product;

import java.util.List;

public interface OrderService {
    OrderResponseDTO createOrder(OrderRequestDTO requestDTO);
    OrderResponseDTO getOrderById(Long id);
    List<OrderResponseDTO> getAllOrders();
    Product getProductFromProductService(String productId);
    Product getProductFromProductService(Long productId);
    Product getProductFromProductService2(String productId);
    Product getProductFromProductService2(Long productId);
}
