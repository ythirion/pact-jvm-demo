package demo.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ProductCreatedHandler {
    private final ObjectMapper mapper;

    public ProductCreated handle(String message) throws JsonProcessingException {
        return mapper.readValue(message, ProductCreated.class);
    }
}