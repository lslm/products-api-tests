package dev.lslm.products_api.services;

import dev.lslm.products_api.models.Order;
import dev.lslm.products_api.models.Product;
import dev.lslm.products_api.models.Stock;
import dev.lslm.products_api.repositories.ProductRepository;
import dev.lslm.products_api.repositories.StockRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@SpringBootTest
@Transactional
class OrderServiceTests {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockRepository stockRepository;

    private Product createSampleProduct(double maxDiscount) {
        Product p = new Product();
        p.setDescription("Sample Product");
        p.setSupplier("ACME");
        p.setPrice(100.0);
        p.setMaxDiscount(maxDiscount);
        return productRepository.save(p);
    }

    private Stock createStock(Product product, int quantity) {
        Stock s = new Stock();
        s.setProduct(product);
        s.setQuantity(quantity);
        return stockRepository.save(s);
    }

    @Test
    void createOrderShouldReduceStock() {
        Product p = createSampleProduct(0.2);
        createStock(p, 10);

        Order order = orderService.createOrder(p.getId(), 5, 0.1);

        Assertions.assertTrue(order.getId() > 0, "Order should be persisted");
        Optional<Stock> updatedStock = stockRepository.findByProduct(p);
        Assertions.assertTrue(updatedStock.isPresent());
        Assertions.assertEquals(5, updatedStock.get().getQuantity(), "Stock should be reduced by ordered quantity");
    }

    @Test
    void createOrderInsufficientStockShouldThrow() {
        Product p = createSampleProduct(0.3);
        createStock(p, 3);

        IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class,
                () -> orderService.createOrder(p.getId(), 5, 0.1));
        Assertions.assertTrue(ex.getMessage().contains("Unavailable stock"));
    }

    @Test
    void discountAboveMaxShouldBeCapped() {
        Product p = createSampleProduct(0.15);
        createStock(p, 5);

        Order order = orderService.createOrder(p.getId(), 2, 0.5); // request 50% discount, max is 15%
        Assertions.assertEquals(0.15, order.getDiscount(), "Discount should be capped to product maxDiscount");
    }
}
