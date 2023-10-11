package kitchenpos.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import kitchenpos.application.OrderService;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderRestController.class)
class OrderRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    void 주문을_생성한다() throws Exception {
        // given
        Order createdOrder = new Order();
        createdOrder.setId(1L);
        createdOrder.setOrderStatus(OrderStatus.MEAL.name());

        // when
        when(orderService.create(any(Order.class))).thenReturn(createdOrder);

        // then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(createdOrder)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/orders/" + createdOrder.getId()))
                .andExpect(content().string(objectMapper.writeValueAsString(createdOrder)));
    }

    @Test
    void 주문_목록을_조회한다() throws Exception {
        // given
        Order order1 = new Order();
        order1.setId(1L);
        order1.setOrderStatus(OrderStatus.MEAL.name());
        Order order2 = new Order();
        order2.setId(2L);
        order2.setOrderStatus(OrderStatus.COOKING.name());

        // when
        when(orderService.list()).thenReturn(List.of(order1, order2));

        // then
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(order1, order2))));
    }

    @Test
    void 주문_상태를_변경한다() throws Exception {
        // given
        Long orderId = 1L;
        Order request = new Order();
        Order response = new Order();

        // when
        when(orderService.changeOrderStatus(anyLong(), any(Order.class)))
                .thenReturn(response);

        // when
        mockMvc.perform(put("/api/orders/{id}/order-status", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
