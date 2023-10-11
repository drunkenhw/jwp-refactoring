package kitchenpos.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import kitchenpos.application.TableService;
import kitchenpos.domain.OrderTable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TableRestController.class)
class TableRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TableService tableService;

    @Test
    void 테이블을_생성한다() throws Exception {
        // given
        OrderTable createdTable = new OrderTable();
        createdTable.setId(1L);
        createdTable.setNumberOfGuests(10);

        // when
        when(tableService.create(any(OrderTable.class))).thenReturn(createdTable);

        // then
        mockMvc.perform(post("/api/tables")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(createdTable)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/tables/" + createdTable.getId()))
                .andExpect(content().string(objectMapper.writeValueAsString(createdTable)));
    }

    @Test
    void 테이블을_전체_조회한다() throws Exception {
        // given
        OrderTable table1 = new OrderTable();
        table1.setId(1L);
        table1.setNumberOfGuests(10);
        OrderTable table2 = new OrderTable();
        table2.setId(2L);
        table2.setNumberOfGuests(12);

        // when
        when(tableService.list()).thenReturn(List.of(table1, table2));

        // then
        mockMvc.perform(get("/api/tables"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(table1, table2))));
    }

    @Test
    void 빈_테이블로_변경한다() throws Exception {
        // given
        Long tableId = 1L;
        OrderTable updatedTable = new OrderTable();
        updatedTable.setEmpty(true);

        // when
        when(tableService.changeEmpty(tableId, updatedTable)).thenReturn(updatedTable);

        // then
        mockMvc.perform(put("/api/tables/1/empty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(updatedTable)))
                .andExpect(status().isOk());
    }

    @Test
    void 테이블의_손님_수를_변경한다() throws Exception {
        // given
        Long tableId = 1L;
        OrderTable updatedTable = new OrderTable();
        updatedTable.setNumberOfGuests(4);

        // when
        when(tableService.changeNumberOfGuests(tableId, updatedTable)).thenReturn(updatedTable);

        // then
        mockMvc.perform(put("/api/tables/1/number-of-guests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(updatedTable)))
                .andExpect(status().isOk());
    }
}

