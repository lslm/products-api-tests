package dev.lslm.products_api.controllers;

import dev.lslm.products_api.models.Product;
import dev.lslm.products_api.models.Stock;
import dev.lslm.products_api.repositories.ProductRepository;
import dev.lslm.products_api.repositories.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@AutoConfigureMockMvc
public class OrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockRepository stockRepository;

    private Product product;

    @BeforeEach
    void setup() {
        product = new Product();
        product.setDescription("Keyboard");
        product.setSupplier("LogiTech");
        product.setPrice(200.0);
        product.setMaxDiscount(0.20);
        product = productRepository.save(product);
        Stock stock = new Stock();
        stock.setProduct(product);
        stock.setQuantity(10);
        stockRepository.save(stock);
    }

    @Test
    void shouldCreateOrder() throws Exception {
        String body = String.format("{\n  \"productId\": %d,\n  \"quantity\": 3,\n  \"discount\": 0.10\n}", product.getId());

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertTrue(response.contains("\"productId\":" + product.getId()));
        assertTrue(response.contains("\"quantity\":3"));
    }

    @Test
    void shouldReturnConflictWhenInsufficientStock() throws Exception {
        String body = String.format("{\n  \"productId\": %d,\n  \"quantity\": 50,\n  \"discount\": 0.10\n}", product.getId());

        mockMvc.perform(MockMvcRequestBuilders
                .post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Unavailable stock")));
    }

    @Test
    void shouldReturnBadRequestWhenInvalidQuantity() throws Exception {
        String body = String.format("{\n  \"productId\": %d,\n  \"quantity\": 0,\n  \"discount\": 0.10\n}", product.getId());

        mockMvc.perform(MockMvcRequestBuilders
                .post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());
    }
}
