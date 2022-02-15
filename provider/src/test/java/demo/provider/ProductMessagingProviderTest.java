package demo.provider;

import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit5.MessageTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.UUID;

import static org.mockito.Mockito.*;

@Provider("ProductService")
@PactFolder("src/test/pacts-messaging")
public class ProductMessagingProviderTest {
    public static final String NEW_PRODUCTS = "newProducts";
    private EventPublisher eventPublisher;
    private EventDispatcher dispatcher;

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPact(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @BeforeEach
    void init(PactVerificationContext context) {
        context.setTarget(new MessageTestTarget());
        eventPublisher = Mockito.mock(EventPublisher.class);
        dispatcher = new EventDispatcher(new ObjectMapper(), eventPublisher);
    }

    @PactVerifyProvider("a product creation event")
    public String verifyProductCreatedEvent() throws JsonProcessingException {
        val productCreated = ProductCreated.builder()
                .messageId(UUID.randomUUID())
                .id("10")
                .type("CREDIT_CARD")
                .build();

        dispatcher.publishEvent(productCreated);

        val messageCapture = ArgumentCaptor.forClass(String.class);
        verify(eventPublisher, times(1)).accept(eq(NEW_PRODUCTS), messageCapture.capture());

        return messageCapture.getValue();
    }
}