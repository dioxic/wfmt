package uk.dioxic.wfmt.model;

import lombok.Data;
import org.springframework.data.annotation.PersistenceConstructor;

@Data
public class OrderSummary {
    private final Integer orderId;
    private final Integer circuitId;
    private final String name;
    // other summary fields

    public OrderSummary(Order order) {
        orderId = order.getOrderPk().getOrderId();
        circuitId = order.getOrderPk().getCircuitId();
        name = order.getName();
    }

    @PersistenceConstructor
    public OrderSummary(Integer orderId, Integer circuitId, String name) {
        this.orderId = orderId;
        this.circuitId = circuitId;
        this.name = name;
    }

    public Order.OrderPk getOrderPk() {
        return new Order.OrderPk(orderId, circuitId);
    }

}
