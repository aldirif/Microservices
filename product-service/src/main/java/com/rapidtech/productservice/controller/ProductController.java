package com.rapidtech.productservice.controller;

import com.rapidtech.productservice.dto.ProductDto;
import com.rapidtech.productservice.dto.ProductRequest;
import com.rapidtech.productservice.dto.ProductResponse;
import com.rapidtech.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public List<ProductResponse> getAllProduct() {
    return productService.getAllProduct();
}

    @GetMapping("/checkproduct")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponse checkProduct(@RequestParam String productName){
        return productService.checkProduct(productName);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String insertProduct(@RequestBody ProductRequest productRequest){
        productService.insertProduct(productRequest);
        return "Data product added";
    }

    @PostMapping("/increasestock")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse increaseStock(@RequestBody ProductDto productRequest){
        return productService.increaseStock(productRequest);
    }

    @PostMapping("/decreasestock")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse decreaseStock(@RequestBody ProductDto productRequest){
        return productService.decreaseStock(productRequest);
    }
}
