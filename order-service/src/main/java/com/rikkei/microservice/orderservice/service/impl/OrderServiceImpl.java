package com.rikkei.microservice.orderservice.service.impl;

import com.rikkei.microservice.orderservice.dto.OrderRequestDTO;
import com.rikkei.microservice.orderservice.dto.OrderResponseDTO;
import com.rikkei.microservice.orderservice.dto.Product;
import com.rikkei.microservice.orderservice.entity.Order;
import com.rikkei.microservice.orderservice.exception.ResourceNotFoundException;
import com.rikkei.microservice.orderservice.repository.OrderRepository;
import com.rikkei.microservice.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final DiscoveryClient discoveryClient;
    private final RestClient restClient;

    @Override
    public OrderResponseDTO createOrder(OrderRequestDTO requestDTO) {
        if (requestDTO.getQuantity() == null || requestDTO.getQuantity() <= 0) {
            throw new IllegalArgumentException("Số lượng (quantity) phải lớn hơn 0");
        }

        // Lấy thông tin sản phẩm và đơn giá từ Product Service qua @LoadBalanced
        Product product = getProductFromProductService2(String.valueOf(requestDTO.getProductId()));
        BigDecimal unitPrice = (product != null && product.getPrice() != null)
                ? product.getPrice()
                : BigDecimal.valueOf(150.00);
        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(requestDTO.getQuantity()));

        Order order = Order.builder()
                .customerId(requestDTO.getCustomerId())
                .productId(requestDTO.getProductId())
                .orderDate(LocalDateTime.now())
                .totalAmount(totalAmount)
                .build();

        Order savedOrder = orderRepository.save(order);
        return OrderResponseDTO.fromEntity(savedOrder);
    }

    @Override
    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        return OrderResponseDTO.fromEntity(order);
    }

    @Override
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Product getProductFromProductService(String productId) {
        // BƯỚC A: Hỏi Eureka lấy danh sách các máy chủ đang chạy ProductService
        // Lưu ý: Tên service lấy trong file application.properties
        List<ServiceInstance> instances = discoveryClient.getInstances("product-service");
        if (instances == null || instances.isEmpty()) {
            instances = discoveryClient.getInstances("PRODUCT-SERVICE");
        }

        // BƯỚC B: Xử lý lỗi (Theo yêu cầu bài tập: Trả về 503 nếu rỗng)
        if (instances == null || instances.isEmpty()) {
            // Bạn có thể throw ra một Exception tuỳ chỉnh, ở đây dùng tạm ResponseStatusException
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "PRODUCT-SERVICE hiện không khả dụng (Không tìm thấy instance nào)"
            );
        }

        // BƯỚC C: Lấy Instance đầu tiên trong danh sách (Tương lai bạn có thể viết logic random để cân bằng tải)
        ServiceInstance productInstance = instances.get(0);

        // BƯỚC D: Lấy URL gốc (Ví dụ: http://localhost:9090) từ instance
        String baseUrl = productInstance.getUri().toString();

        // BƯỚC E: Nối chuỗi để tạo ra API URL hoàn chỉnh
        // Ví dụ: http://localhost:9090/api/v1/products/123
        String targetUrl = baseUrl + "/api/v1/products/" + productId;

        // BƯỚC F: Thực hiện gọi API lấy về sản phẩm
        try {
            return restClient.get()
                    .uri(targetUrl)
                    .retrieve()
                    .body(Product.class);
        } catch (ResourceAccessException ex) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Không thể kết nối tới PRODUCT-SERVICE: " + ex.getMessage(),
                    ex
            );
        }
    }

    @Override
    public Product getProductFromProductService(Long productId) {
        return getProductFromProductService(String.valueOf(productId));
    }

    @Override
    public Product getProductFromProductService2(String productId) {
        String targetUrl = "http://ProductService/api/v1/products/" + productId;

        // BƯỚC F: Thực hiện gọi API lấy về sản phẩm qua @LoadBalanced
        try {
            return restClient.get()
                    .uri(targetUrl)
                    .retrieve()
                    .body(Product.class);
        } catch (ResourceAccessException ex) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Không thể kết nối tới PRODUCT-SERVICE: " + ex.getMessage(),
                    ex
            );
        }
    }

    @Override
    public Product getProductFromProductService2(Long productId) {
        return getProductFromProductService2(String.valueOf(productId));
    }
}
