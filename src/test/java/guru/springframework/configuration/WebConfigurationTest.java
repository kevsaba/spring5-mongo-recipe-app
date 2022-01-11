package guru.springframework.configuration;

import guru.springframework.services.RecipeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import reactor.core.publisher.Flux;


public class WebConfigurationTest {
    WebTestClient webTestClient;
    @Mock
    RecipeService recipeService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        WebConfiguration webConfiguration = new WebConfiguration();
        RouterFunction<?> router = webConfiguration.getRecipes(recipeService);

        webTestClient = WebTestClient.bindToRouterFunction(router).build();
    }


    @Test
    public void testGetRecipes() {
        Mockito.when(recipeService.getRecipes()).thenReturn(Flux.just());
        webTestClient.get().uri("/api/recipes").accept(MediaType.APPLICATION_JSON)
                .exchange()//here is to trigger and now i define what im expecting
                .expectStatus()
                .isOk();

    }
}