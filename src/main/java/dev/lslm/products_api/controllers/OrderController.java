package dev.lslm.products_api.controllers;

import dev.lslm.products_api.controllers.dto.CreateOrderRequest;
import dev.lslm.products_api.controllers.dto.OrderResponse;
import dev.lslm.products_api.models.Order;
import dev.lslm.products_api.services.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateOrderRequest request) {
        try {
            Order order = orderService.createOrder(request.getProductId(), request.getQuantity(), request.getDiscount());
            OrderResponse response = new OrderResponse();
            response.setId(order.getId());
            response.setProductId(order.getProduct().getId());
            response.setQuantity(order.getQuantity());
            response.setDiscount(order.getDiscount());
            response.setUnitPrice(order.getProduct().getPrice());
            response.setTotalPrice(order.getProduct().getPrice() * order.getQuantity());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}
