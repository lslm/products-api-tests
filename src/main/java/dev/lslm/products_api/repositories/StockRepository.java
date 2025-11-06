package dev.lslm.products_api.repositories;

import dev.lslm.products_api.models.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import dev.lslm.products_api.models.Product;

import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Integer> {
    Optional<Stock> findByProduct(Product product);
}
