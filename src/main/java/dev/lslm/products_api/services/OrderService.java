package dev.lslm.products_api.services;

import dev.lslm.products_api.models.Order;
import dev.lslm.products_api.models.Product;
import dev.lslm.products_api.models.Stock;
import dev.lslm.products_api.repositories.OrderRepository;
import dev.lslm.products_api.repositories.ProductRepository;
import dev.lslm.products_api.repositories.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final OrderRepository orderRepository;

    public OrderService(ProductRepository productRepository,
                        StockRepository stockRepository,
                        OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * Create a new Order for a given product if stock quantity is sufficient.
     * Rules:
     *  - quantity must be > 0
     *  - product must exist
     *  - stock record must exist and have at least the requested quantity
     *  - discount negative values are treated as 0
     *  - discount values above product maxDiscount are capped to maxDiscount
     * On success, stock is decreased and the Order persisted.
     *
     * @param productId ID of the product to order
     * @param quantity  number of units requested
     * @param discount  discount fraction (0.0 - 1.0)
     * @return persisted Order
     * @throws IllegalArgumentException if validation fails
     * @throws IllegalStateException if insufficient stock
     */
    @Transactional
    public Order createOrder(int productId, int quantity, double discount) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        // Normalize discount
        if (discount < 0) discount = 0.0;
        if (discount > product.getMaxDiscount()) {
            discount = product.getMaxDiscount();
        }

        Stock stock = stockRepository.findByProduct(product)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found for product: " + productId));

        if (stock.getQuantity() < quantity) {
            throw new IllegalStateException("Unavailable stock. Available=" + stock.getQuantity() + ", requested=" + quantity);
        }

        // Decrease stock
        stock.setQuantity(stock.getQuantity() - quantity);
        stockRepository.save(stock);

        // Create and persist order
        Order order = new Order();
        order.setProduct(product);
        order.setQuantity(quantity);
        order.setDiscount(discount);

        return orderRepository.save(order);
    }
}
