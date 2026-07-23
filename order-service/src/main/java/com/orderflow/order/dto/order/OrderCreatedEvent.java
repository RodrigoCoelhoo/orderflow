package com.orderflow.order.dto.order;

import com.orderflow.order.model.Order;

public record OrderCreatedEvent(Order order) {}
