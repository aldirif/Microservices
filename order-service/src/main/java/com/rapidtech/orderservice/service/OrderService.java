package com.rapidtech.orderservice.service;

import com.rapidtech.orderservice.dto.*;
import com.rapidtech.orderservice.event.OrderPlacedEvent;
import com.rapidtech.orderservice.model.Order;
import com.rapidtech.orderservice.model.OrderLineItems;
import com.rapidtech.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public String placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setUsername(orderRequest.getUsername());
        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsList()
                .stream()
                .map(this::mapToDto).toList();
        order.setOrderLineItemsList(orderLineItems);

        List<Boolean> stockAvailable = new ArrayList<Boolean>();
        Double totalPrice = 0.00;
        for (OrderLineItems orderLine : orderLineItems) {
            String productName = orderLine.getProductName();
            ProductResponse productResponse = webClientBuilder.build().get()
                    .uri("http://product-service/api/product/checkproduct",
                            uriBuilder -> uriBuilder.queryParam("productName", productName).build())
                    .retrieve()
                    .bodyToMono(ProductResponse.class)
                    .block();

            Long quantityRequest = orderLine.getQuantity();
            Long quantityProduct = productResponse.getQuantity();
            Boolean available = stockAvailable(quantityProduct, quantityRequest);
            stockAvailable.add(available);

            Double productPrice = productResponse.getPrice() * orderLine.getQuantity();
            totalPrice = totalPrice + productPrice;
        }
        boolean allStockAvailable = stockAvailable.contains(false);
        allStockAvailable = !allStockAvailable;
        if (!allStockAvailable) {
            return "Insufficient Stock";
        }

        String username = order.getUsername();
        WalletResponse walletResponse = webClientBuilder.build().get()
                .uri("http://wallet-service/api/wallet/checkbalance",
                        uriBuilder -> uriBuilder.queryParam("username", username).build())
                .retrieve()
                .bodyToMono(WalletResponse.class)
                .block();

        Double balance = walletResponse.getBalance();
        Boolean balanceAvailable = balanceAvailable(balance, totalPrice);
        if (!balanceAvailable) {
            return "You don't have enough Balance";
        }

        for (OrderLineItems orderLine : orderLineItems) {
            ProductDto decreaseStock = ProductDto.builder()
                    .productName(orderLine.getProductName())
                    .quantity(orderLine.getQuantity())
                    .build();
            ProductResponse newStock = webClientBuilder.build()
                    .post()
                    .uri("http://product-service/api/product/decreasestock")
                    .accept(MediaType.APPLICATION_JSON)
                    .body(Mono.just(decreaseStock), ProductDto.class)
                    .retrieve()
                    .bodyToMono(ProductResponse.class)
                    .block();
        }

        WalletResponse decreaseBalance = WalletResponse.builder()
                .username(walletResponse.getUsername())
                .balance(totalPrice)
                .build();
        WalletResponse newBalance = webClientBuilder.build()
                .post()
                .uri("http://wallet-service/api/wallet/decreasebalance")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(decreaseBalance), WalletResponse.class)
                .retrieve()
                .bodyToMono(WalletResponse.class)
                .block();

        List<Boolean> availableList = new ArrayList<Boolean>();
        availableList.add(balanceAvailable);
        availableList.addAll(stockAvailable);

        boolean allAvailable = availableList.contains(false);
        allAvailable = !allAvailable;
        if (allAvailable) {
            orderRepository.save(order);
            kafkaTemplate.send("notificationTopic", new OrderPlacedEvent(order.getOrderNumber()));
        }
        return "Order Successful";
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setProductName(orderLineItemsDto.getProductName());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        return orderLineItems;
    }

    private boolean stockAvailable(Long stockProduct, Long stockRequest) {
        boolean check;
        check = stockProduct >= stockRequest;
        return check;
    }

    private boolean balanceAvailable(Double balance, Double totalPrice) {
        boolean check;
        check = balance >= totalPrice;
        return check;
    }
}
