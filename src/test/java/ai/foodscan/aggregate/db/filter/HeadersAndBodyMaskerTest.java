package ai.foodscan.aggregate.db.filter;

import ai.foodscan.aggregate.db.utils.TestFileUtil;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HeadersAndBodyMaskerTest implements HeadersAndBodyMasker {

    @Test
    void getMaskedHttpHeaders_withDefinedListOfHeadersToMask_returnsMaskedHeaderValue() {
        List<String> headersToMask = List.of("Authorization");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("testToken");
        String expectedMaskedHeaders = "[Authorization:\"******\"]";
        assertEquals(expectedMaskedHeaders, getMaskedHttpHeaders(headers, headersToMask));
    }

    @Test
    void getMaskedJsonBody_withDefinedListOfFieldsToMask_returnsMaskedFieldValue() {
        List<String> bodyJsonFieldsToMask = List.of("someFieldTwo", "token");
        String body = TestFileUtil.readStringFromResourceFile("json/sample_payload.json");
        String expectedMaskedBody = TestFileUtil.readStringFromResourceFile("json/sample_payload_masked.json");
        assertEquals(expectedMaskedBody, getMaskedJsonBody(body, bodyJsonFieldsToMask));
    }
}
