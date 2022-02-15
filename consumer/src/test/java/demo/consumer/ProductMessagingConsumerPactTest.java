package demo.consumer;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.messaging.MessagePact;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(pactVersion = PactSpecVersion.V3)
class ProductMessagingConsumerPactTest {
    @Pact(consumer = "FrontendApplication", provider = "ProductService")
    MessagePact publishNewProductCreated(MessagePactBuilder builder) {
        throw new NotImplementedException();
    }
}