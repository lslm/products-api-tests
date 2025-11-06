package dev.lslm.products_api.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OrderTests {

    @Test
    void shouldCalculateOrderPriceWithDiscountWithinLimit() {
        Product product = new Product();
        product.setDescription("Mouse");
        product.setSupplier("LogiTech");
        product.setPrice(100.0);
        product.setMaxDiscount(0.20); // 20%

        Order order = new Order();
        order.setProduct(product);
        order.setQuantity(2);
        order.setDiscount(0.10); // 10% discount

        double expectedUnitPrice = product.getPriceWithDiscount(0.10); // 90.0
        assertEquals(90.0, expectedUnitPrice, 0.0001);
        assertEquals(180.0, order.getOrderPrice(), 0.0001);
    }

    @Test
    void shouldCapDiscountAboveMaxDiscount() {
        Product product = new Product();
        product.setDescription("Keyboard");
        product.setSupplier("LogiTech");
        product.setPrice(100.0);
        product.setMaxDiscount(0.15); // 15%

        Order order = new Order();
        order.setProduct(product);
        order.setQuantity(2);
        order.setDiscount(0.50); // 50% requested, should cap to 15%

        double expectedUnitPrice = product.getPriceWithDiscount(0.50); // capped to 15% -> 85.0
        assertEquals(85.0, expectedUnitPrice, 0.0001);
        assertEquals(170.0, order.getOrderPrice(), 0.0001);
    }
}
