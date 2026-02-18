package ai.foodscan.aggregate.db.filter;


import org.springframework.http.HttpHeaders;

import java.util.List;

interface HeadersAndBodyMasker {
    default String getMaskedHttpHeaders(HttpHeaders headers, List<String> maskedHeadersList) {
        HttpHeaders maskedHeaders = new HttpHeaders();
        headers.toSingleValueMap()
                .forEach(maskedHeaders::set);
        maskedHeadersList.stream()
                .filter(maskedHeaders::containsKey)
                .forEach(header -> maskedHeaders.set(header, "******"));
        return maskedHeaders.toString();
    }

    default String getMaskedJsonBody(String body, List<String> maskedJsonFieldsList) {
        return maskedJsonFieldsList.stream()
                .reduce(body, (bodyToMask, fieldName) -> bodyToMask
                        .replaceAll("(\"" + fieldName + "\":)([^\\r\\n,}]*)", "$1 \"******\""));
    }
}