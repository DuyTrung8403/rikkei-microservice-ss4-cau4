package com.rikkei.microservice.orderservice.controller;

import com.rikkei.microservice.orderservice.dto.OrderRequestDTO;
import com.rikkei.microservice.orderservice.dto.OrderResponseDTO;
import com.rikkei.microservice.orderservice.dto.Product;
import com.rikkei.microservice.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderRequestDTO requestDTO) {
        OrderResponseDTO response = orderService.createOrder(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable("id") Long id) {
        OrderResponseDTO response = orderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/getProduct/{productId}")
    public ResponseEntity<Product> getProduct(@PathVariable("productId") String productId) {
        Product product = orderService.getProductFromProductService(productId);
        return ResponseEntity.ok(product);
    }
}
