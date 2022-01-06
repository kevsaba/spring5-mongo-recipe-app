package guru.springframework.services;

import guru.springframework.commands.IngredientCommand;
import guru.springframework.converters.IngredientCommandToIngredient;
import guru.springframework.converters.IngredientToIngredientCommand;
import guru.springframework.domain.Ingredient;
import guru.springframework.domain.Recipe;
import guru.springframework.domain.UnitOfMeasure;
import guru.springframework.repositories.reactive.RecipeReactiveRepository;
import guru.springframework.repositories.reactive.UnitOfMeasureReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Objects.isNull;

/**
 * Created by jt on 6/28/17.
 */
@Slf4j
@Service
public class IngredientServiceImpl implements IngredientService {

    private final IngredientToIngredientCommand ingredientToIngredientCommand;
    private final IngredientCommandToIngredient ingredientCommandToIngredient;
    private final RecipeReactiveRepository recipeReactiveRepository;
    private final UnitOfMeasureReactiveRepository unitOfMeasureRepository;

    public IngredientServiceImpl(IngredientToIngredientCommand ingredientToIngredientCommand,
                                 IngredientCommandToIngredient ingredientCommandToIngredient,
                                 RecipeReactiveRepository recipeReactiveRepository, UnitOfMeasureReactiveRepository unitOfMeasureRepository) {
        this.ingredientToIngredientCommand = ingredientToIngredientCommand;
        this.ingredientCommandToIngredient = ingredientCommandToIngredient;
        this.recipeReactiveRepository = recipeReactiveRepository;
        this.unitOfMeasureRepository = unitOfMeasureRepository;
    }

    @Override
    public Mono<IngredientCommand> findByRecipeIdAndIngredientId(String recipeId, String ingredientId) {

        return recipeReactiveRepository
                .findById(recipeId)
                .flatMapIterable(Recipe::getIngredients)
                .filter(ingredient -> ingredient.getId().equals(ingredientId))
                .single()
                .map(ingredient -> {
                    IngredientCommand command = ingredientToIngredientCommand.convert(ingredient);
                    assert command != null;
                    command.setRecipeId(recipeId);
                    return command;
                });
    }

    @Override
    @Transactional
    public Mono<IngredientCommand> saveIngredientCommand(IngredientCommand command) {
        Mono<Recipe> recipeMono = recipeReactiveRepository.findById(command.getRecipeId());
        Mono<Ingredient> ingredientMono = findOrCreateIngredient(recipeMono, command);
        return recipeMono.zipWith(ingredientMono)
                .publishOn(Schedulers.parallel())
                .doOnNext(recipeIngredient -> recipeReactiveRepository.save(recipeIngredient.getT1()))
                .map(recipeIngredient -> {
                    IngredientCommand commandIngredient = ingredientToIngredientCommand.convert(recipeIngredient.getT2());
                    commandIngredient.setRecipeId(recipeIngredient.getT1().getId());
                    return commandIngredient;
                })
                .defaultIfEmpty(new IngredientCommand());
    }

    private Mono<Ingredient> findOrCreateIngredient(Mono<Recipe> recipeMono, IngredientCommand command) {
        return recipeMono.flatMapIterable(Recipe::getIngredients)
                .filter(ingredient -> ingredient.getId().equals(command.getId()))
                .single()
                .zipWith(unitOfMeasureRepository.findById(command.getUom().getId()))
                .doOnNext(updateIngredient(command))
                .map(Tuple2::getT1)
                .switchIfEmpty(addNewIngredientIntoRecipe(recipeMono, command));
    }

    private Consumer<Tuple2<Ingredient, UnitOfMeasure>> updateIngredient(IngredientCommand command) {
        return ingredientAndUom -> {
            Ingredient ingredient = ingredientAndUom.getT1();
            ingredient.setDescription(command.getDescription());
            ingredient.setAmount(command.getAmount());
            ingredient.setUom(ingredientAndUom.getT2());
        };
    }

    private Mono<Ingredient> addNewIngredientIntoRecipe(Mono<Recipe> recipeMono, IngredientCommand command) {
        return recipeMono
                .zipWith(Mono.just(ingredientCommandToIngredient.convert(command)))
                .doOnNext(m -> m.getT1().addIngredient(m.getT2()))
                .map(Tuple2::getT2);
    }

    @Override
    public Mono<Void> deleteById(String recipeId, String idToDelete) {

        log.debug("Deleting ingredient: " + recipeId + ":" + idToDelete);

        Recipe recipe = recipeReactiveRepository.findById(recipeId).block();

        if (!isNull(recipe)) {
            log.debug("found recipe");

            Optional<Ingredient> ingredientOptional = recipe
                    .getIngredients()
                    .stream()
                    .filter(ingredient -> ingredient.getId().equals(idToDelete))
                    .findFirst();

            if (ingredientOptional.isPresent()) {
                log.debug("found Ingredient");
                Ingredient ingredientToDelete = ingredientOptional.get();
                // ingredientToDelete.setRecipe(null);
                recipe.getIngredients().remove(ingredientOptional.get());
                recipeReactiveRepository.save(recipe).block();
            }
        } else {
            log.debug("Recipe Id Not found. Id:" + recipeId);
        }
        return Mono.empty();
    }
}
