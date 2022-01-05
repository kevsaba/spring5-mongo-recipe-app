package guru.springframework.repositories;

import guru.springframework.domain.Category;
import guru.springframework.repositories.reactive.CategoryReactiveRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataMongoTest
public class CategoryRepositoryIT {

    @Autowired
    CategoryReactiveRepository categoryReactiveRepository;

    @Before
    public void setUp() {
        categoryReactiveRepository.deleteAll().block();
    }

    @Test
    public void addDocumentObject() {
        Category entity = new Category();
        categoryReactiveRepository.save(entity).block();
        Long total = categoryReactiveRepository.findAll().count().block();
        assertEquals(total.longValue(), 1L);
    }

    @Test
    public void findByDescription() {
        Category c = new Category();
        c.setDescription("hola");
        categoryReactiveRepository.save(c).block();
        Category found = categoryReactiveRepository.findByDescription("hola").block();
        assertEquals(found.getDescription(), "hola");
    }
}
