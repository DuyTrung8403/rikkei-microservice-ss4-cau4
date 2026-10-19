package com.rikkei.microservice.apigateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class ResponseHeaderFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // chain.filter(exchange) gửi request đi tới service đích.
        // .then(...) chỉ định đoạn code sẽ chạy SAU KHI service đích trả response về (Post-Filter)
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            // Thêm header vào Response trước khi trả về Client
            exchange.getResponse().getHeaders().add("X-System-Name", "Api-Gateway-System");
        }));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
