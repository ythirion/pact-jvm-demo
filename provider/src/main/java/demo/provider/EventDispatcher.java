package demo.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.val;

@AllArgsConstructor
public class EventDispatcher {
    private final ObjectMapper objectMapper;
    private final EventPublisher eventPublisher;

    void publishEvent(ProductCreated productCreated) throws JsonProcessingException {
        val event = objectMapper.writeValueAsString(productCreated);
        eventPublisher.accept("newProducts", event);
    }
}