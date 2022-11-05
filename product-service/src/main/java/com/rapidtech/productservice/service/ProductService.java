package com.rapidtech.productservice.service;

import com.rapidtech.productservice.dto.ProductDto;
import com.rapidtech.productservice.dto.ProductRequest;
import com.rapidtech.productservice.dto.ProductResponse;
import com.rapidtech.productservice.event.OrderPlacedEvent;
import com.rapidtech.productservice.model.Product;
import com.rapidtech.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public void insertProduct(ProductRequest productRequest) {
        Product product = new Product();
        product.setProductName(productRequest.getProductName());
        product.setPrice(productRequest.getPrice());
        product.setQuantity(productRequest.getQuantity());
        productRepository.save(product);

    }
    @SneakyThrows
    public ProductResponse checkProduct(String productName) {
        //log.info("Mulai menunggu");
        //Thread.sleep(10000);
        //log.info("Selesai menunggu");
        Product product = productRepository.findById(productName).get();
        return ProductResponse.builder()
                .productName(product.getProductName())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .build();
    }

    public List<ProductResponse> getAllProduct() {
        List<ProductResponse> productResponseList = new ArrayList<>();
        List<Product> productList = productRepository.findAll();
        for(Product product : productList){
            productResponseList.add(ProductResponse.builder()
                    .productName(product.getProductName())
                    .price(product.getPrice())
                    .quantity(product.getQuantity())
                    .build());
        }
        return productResponseList;
    }

    public ProductResponse increaseStock(ProductDto increaseProduct) {
        ProductDto increase = ProductDto.builder()
                .productName(increaseProduct.getProductName())
                .quantity(increaseProduct.getQuantity())
                .build();
        Product product = productRepository.findById(increase.getProductName()).get();
        Long quantity = product.getQuantity() + increase.getQuantity();
        product.setQuantity(quantity);
        Product result = productRepository.save(product);
        return ProductResponse.builder()
                .productName(result.getProductName())
                .price(result.getPrice())
                .quantity(result.getQuantity())
                .build();
    }

    public ProductResponse decreaseStock(ProductDto decreaseProduct) {
        ProductDto decrease = ProductDto.builder()
                .productName(decreaseProduct.getProductName())
                .quantity(decreaseProduct.getQuantity())
                .build();
        Product product = productRepository.findById(decrease.getProductName()).get();
        Long quantity = product.getQuantity() - decrease.getQuantity();
        product.setQuantity(quantity);
        Product result = productRepository.save(product);
        kafkaTemplate.send("notificationTopic",new OrderPlacedEvent("Product: - "+product.getProductName()+" "+product.getQuantity()));
        return ProductResponse.builder()
                .productName(result.getProductName())
                .price(result.getPrice())
                .quantity(result.getQuantity())
                .build();
    }
}
