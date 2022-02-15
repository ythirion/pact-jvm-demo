package demo.provider;

import java.util.function.BiConsumer;

interface EventPublisher extends BiConsumer<String, String> {
    @Override
    void accept(String topic, String event);
}