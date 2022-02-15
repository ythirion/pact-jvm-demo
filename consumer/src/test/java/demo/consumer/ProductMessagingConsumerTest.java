package demo.consumer;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.consumer.junit5.ProviderType;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.messaging.Message;
import au.com.dius.pact.core.model.messaging.MessagePact;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerType = ProviderType.ASYNCH, pactVersion = PactSpecVersion.V3)
class ProductMessagingConsumerTest {
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final ProductCreatedHandler productCreatedHandler = new ProductCreatedHandler(mapper);

    @Pact(consumer = "messageListener", provider = "ProductService")
    MessagePact publishNewProductCreated(MessagePactBuilder builder) {
        return builder
                .hasPactWith("ProductService")
                .expectsToReceive("a product creation event")
                //.given("product created")
                .withMetadata(m -> m.add("key", "newProducts"))
                .withContent(newJsonBody(object -> {
                    object.stringType("id", "10");
                    object.stringType("type", "CREDIT_CARD");
                }).build())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "publishNewProductCreated")
    void receive_a_product_created_message(List<Message> messages) {
        assertThat(messages)
                .allSatisfy(message -> {
                    val event = productCreatedHandler.handle(message.contentsAsString());
                    assertThat(event.getId()).isEqualTo("10");
                    assertThat(event.getType()).isEqualTo("CREDIT_CARD");
                });
    }
}