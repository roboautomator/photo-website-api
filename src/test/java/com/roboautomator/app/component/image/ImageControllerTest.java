package com.roboautomator.app.component.image;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.verifyNoInteractions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonpath.JsonPath;
import com.roboautomator.app.component.util.AbstractMockMvcTest;
import com.roboautomator.app.component.util.TestHelper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@AutoConfigureMockMvc
@WebMvcTest(ImageRepository.class)
public class ImageControllerTest extends AbstractMockMvcTest {

    private static final String TEST_ENDPOINT = "/image";
    private static final String TEST_TITLE = "test-title";
    private static final String TEST_URL = "test-url";
    private static final Integer TEST_INDEX = 0;
    private static final String TEST_DESCRIPTION = "test-description";

    private MockMvc mockMvc;

    @BeforeEach
    void setupMockMvc() throws JsonProcessingException {
        var imageController = new ImageController(imageRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(imageController).setControllerAdvice(ImageControllerAdvice.class)
                .build();
    }

    @Test
    void shouldReturn400WhenImageIdIsNotValidUUIDForUpdates() throws Exception {

        var id = "invalid-UUID";

        var response = mockMvc
                .perform(put(TEST_ENDPOINT + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(TestHelper.serializeObject(createValidImageBuilder().build())))
                .andExpect(status().isBadRequest()).andReturn();

        verifyNoInteractions(imageRepository);

        var responseAsString = response.getResponse().getContentAsString();

        assertThat(responseAsString).isNotNull();
        assertThat(JsonPath.<String>read(responseAsString, "$.message")).isEqualTo("Validation failed");
        assertThat(JsonPath.<String>read(responseAsString, "$.errors[0].field")).isEqualTo("collectionId");
        assertThat(JsonPath.<String>read(responseAsString, "$.errors[0].error"))
                .contains("invalid-UUID is not a valid UUID");
    }

    @Test
    void shouldReturn404NotFoundWhenImageDoesNotExistOnUpdate() {

    }

    @Test
    void shouldReturn200OKWhenUpdatingImageEntity() throws JsonProcessingException, Exception {

        var id = UUID.randomUUID();

        willReturn(Optional.of(createValidImage().description("previous-description").url("previous-url")
                .title("previous-title").id(id).build())).given(imageRepository).findById(id);

        var response = mockMvc
                .perform(put(TEST_ENDPOINT + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(TestHelper.serializeObject(createValidImageBuilder().build())))
                .andExpect(status().isOk()).andReturn();

        var unpackedResponse = response.getResponse().getContentAsString();

        System.out.println("RESPONSE " + unpackedResponse);

        assertThat(unpackedResponse).isNotEmpty();
        assertThat(unpackedResponse).doesNotContain("previous-title");
        assertThat(unpackedResponse).doesNotContain("previous-description");
        assertThat(unpackedResponse).doesNotContain("previous-url");

        assertThat(unpackedResponse).contains(TEST_TITLE);
        assertThat(unpackedResponse).contains(TEST_DESCRIPTION);
        assertThat(unpackedResponse).contains(TEST_URL);

    }

    @Test
    void shouldReturn400WhenImageIsNotValidUUIDForGet() {

    }

    @Test
    void shouldReturn404NotRoundWhenImageDoesNotExistOnGet() {

    }

    @Test
    void shouldReturn200OKWhenGettingImage() throws Exception {

        var id = UUID.randomUUID();

        willReturn(Optional.of(createValidImage().id(id).build())).given(imageRepository).findById(id);

        var response = mockMvc.perform(get(TEST_ENDPOINT + "/" + id)).andExpect(status().isOk()).andReturn();

        var unpackedResponse = response.getResponse().getContentAsString();

        assertThat(unpackedResponse).isNotEmpty();
        assertThat(unpackedResponse).contains(id.toString());
        assertThat(unpackedResponse).contains(TEST_TITLE);
        assertThat(unpackedResponse).contains(TEST_URL);
        assertThat(unpackedResponse).contains(TEST_DESCRIPTION);

    }

    @Test
    void shouldReturn200OKWhenCreatingImage() {

    }

    @Test
    void shouldReturn400WhenImageIsNotValidUUIDForDelete() {

    }

    @Test
    void shouldReturn404WhenImageIsNotFoundForDelete() {

    }

    @Test
    void shouldReturn200OKWhenDeletingImage() {

    }

    private static ImageEntity.ImageEntityBuilder createValidImage() {
        return ImageEntity.builder().title(TEST_TITLE).url(TEST_URL).index(TEST_INDEX).description(TEST_DESCRIPTION);
    }

    private static ImageUpdate.ImageUpdateBuilder createValidImageBuilder() {
        return ImageUpdate.builder().title(TEST_TITLE).url(TEST_URL).index(TEST_INDEX).description(TEST_DESCRIPTION);
    }

}
