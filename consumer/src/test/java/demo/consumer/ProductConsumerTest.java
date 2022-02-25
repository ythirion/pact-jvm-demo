package demo.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import lombok.val;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonArrayMinLike;
import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(pactVersion = PactSpecVersion.V3, providerName = "ProductService")
class ProductConsumerTest {

    public static final String BEARER_REGEX = "Bearer (19|20)\\d\\d-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])T([01][1-9]|2[0123]):[0-5][0-9]";
    private static final String CONSUMER = "FrontendApplication";
    private ProductServiceClient productServiceClient;

    @BeforeEach
    public void init(MockServer mockServer) {
        RestTemplate restTemplate = new RestTemplateBuilder()
                .rootUri(mockServer.getUrl())
                .build();
        productServiceClient = new ProductServiceClient(restTemplate);
    }

    @Pact(consumer = CONSUMER)
    RequestResponsePact getAllProducts(PactDslWithProvider builder) {
        return builder.given("products exist")
                .uponReceiving("get all products")
                .method("GET")
                .path("/products")
                .matchHeader("Authorization", BEARER_REGEX)
                .willRespondWith()
                .status(200)
                .headers(headers())
                .body(newJsonArrayMinLike(2, array ->
                        array.object(object -> {
                            object.stringType("id", "09");
                            object.stringType("type", "CREDIT_CARD");
                            object.stringType("name", "Gem Visa");
                        })
                ).build())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getAllProducts")
    void getAllProducts_whenProductsExist(MockServer mockServer) {
        val product = Product.builder()
                .id("09")
                .type("CREDIT_CARD")
                .name("Gem Visa")
                .build();
        val products = productServiceClient.getAllProducts();

        assertThat(products).isEqualTo(Arrays.asList(product, product));
    }

    @Pact(consumer = CONSUMER)
    RequestResponsePact getAllProductsWithMatchers(PactDslWithProvider builder) {
        return builder.given("products exist")
                .uponReceiving("get all products with matchers")
                .method("GET")
                .path("/products")
                .matchHeader("Authorization", BEARER_REGEX)
                .willRespondWith()
                .status(200)
                .headers(headers())
                .body(newJsonArrayMinLike(1, array ->
                        array.object(object ->
                                object.stringMatcher("id", "[0-9]{2}")
                                        .stringType("type")
                                        .stringType("name"))
                ).build())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getAllProductsWithMatchers")
    void getAllProducts_whenProductsExist_withMatchers(MockServer mockServer) {
        val products = productServiceClient.getAllProducts();
        assertThat(products)
                .hasSizeGreaterThanOrEqualTo(1)
                .allSatisfy(product -> {
                    assertThat(product.getId()).matches("[0-9]{2}");
                    assertThat(product.getType()).isNotBlank();
                    assertThat(product.getType()).isNotBlank();
                });
    }

    @Pact(consumer = CONSUMER)
    RequestResponsePact noProductsExist(PactDslWithProvider builder) {
        return builder.given("no products exist")
                .uponReceiving("get all products")
                .method("GET")
                .path("/products")
                .matchHeader("Authorization", BEARER_REGEX)
                .willRespondWith()
                .status(200)
                .headers(headers())
                .body("[]")
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "noProductsExist")
    void getAllProducts_whenNoProductsExist(MockServer mockServer) {
        val products = productServiceClient.getAllProducts();
        assertThat(products).isEqualTo(Collections.emptyList());
    }

    @Pact(consumer = CONSUMER)
    RequestResponsePact allProductsNoAuthToken(PactDslWithProvider builder) {
        return builder.given("products exist")
                .uponReceiving("get all products with no auth token")
                .method("GET")
                .path("/products")
                .willRespondWith()
                .status(401)
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "allProductsNoAuthToken")
    void getAllProducts_whenNoAuth(MockServer mockServer) {
        assertHttpClientExceptionOn(() -> productServiceClient.getAllProducts(), 401);
    }

    @Pact(consumer = CONSUMER)
    RequestResponsePact getOneProduct(PactDslWithProvider builder) {
        return builder.given("product with ID 10 exists")
                .uponReceiving("get product with ID 10")
                .method("GET")
                .path("/product/10")
                .matchHeader("Authorization", BEARER_REGEX)
                .willRespondWith()
                .status(200)
                .headers(headers())
                .body(newJsonBody(object -> {
                    object.stringType("id", "10");
                    object.stringType("type", "CREDIT_CARD");
                    object.stringType("name", "28 Degrees");
                }).build())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getOneProduct")
    void getProductById_whenProductWithId10Exists(MockServer mockServer) {
        val expected = Product.builder()
                .id("10")
                .type("CREDIT_CARD")
                .name("28 Degrees")
                .build();
        val product = productServiceClient.getProduct("10");
        assertThat(product).isEqualTo(expected);
    }

    @Pact(consumer = CONSUMER)
    RequestResponsePact productDoesNotExist(PactDslWithProvider builder) {
        return builder.given("product with ID 11 does not exist")
                .uponReceiving("get product with ID 11")
                .method("GET")
                .path("/product/11")
                .matchHeader("Authorization", BEARER_REGEX)
                .willRespondWith()
                .status(404)
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "productDoesNotExist")
    void getProductById_whenProductWithId11DoesNotExist(MockServer mockServer) {
        assertHttpClientExceptionOn(() -> productServiceClient.getProduct("11"), 404);
    }

    @Pact(consumer = CONSUMER)
    RequestResponsePact singleProductnoAuthToken(PactDslWithProvider builder) {
        return builder.given("product with ID 10 exists")
                .uponReceiving("get product by ID 10 with no auth token")
                .method("GET")
                .path("/product/10")
                .willRespondWith()
                .status(401)
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "singleProductnoAuthToken")
    void getProductById_whenNoAuth(MockServer mockServer) {
        assertHttpClientExceptionOn(() -> productServiceClient.getProduct("10"), 401);
    }

    private Map<String, String> headers() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; charset=utf-8");
        return headers;
    }

    private void assertHttpClientExceptionOn(ThrowableAssert.ThrowingCallable throwingCallable,
                                             int expectedStatusCode) {
        assertThatExceptionOfType(HttpClientErrorException.class)
                .isThrownBy(throwingCallable)
                .extracting(RestClientResponseException::getRawStatusCode)
                .isEqualTo(expectedStatusCode);
    }
}
