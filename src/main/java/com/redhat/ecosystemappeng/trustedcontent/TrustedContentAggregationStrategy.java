package com.redhat.ecosystemappeng.trustedcontent;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TrustedContentAggregationStrategy implements AggregationStrategy {

    ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        if (oldExchange == null) {
            return newExchange;
        }
        String oldBody = oldExchange.getIn().getBody(String.class);
        String newBody = newExchange.getIn().getBody(String.class);

        if(newBody.isBlank()) {
            return oldExchange;
        }
        try {
            ArrayNode analysis = (ArrayNode) mapper.readTree(oldBody);
            ArrayNode trustedContent = (ArrayNode) mapper.readTree(newBody);
            analysis.forEach(n -> {
                trustedContent.forEach(t -> {
                    //if (n.get("package").asText().equals(t.get("artifactId").asText() + t.get("groupId").asText())) {
                        ((ObjectNode) n).set("trusted-content", t);
                    //}
                });
            });
            oldExchange.getIn().setBody(mapper.writeValueAsBytes(analysis));
        } catch (JsonProcessingException e) {
            oldExchange.setException(e);
        }
        return oldExchange;
    }
    
}