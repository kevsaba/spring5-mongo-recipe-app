[![CircleCI](https://circleci.com/gh/springframeworkguru/spring5-mongo-recipe-app.svg?style=svg)](https://circleci.com/gh/springframeworkguru/spring5-mongo-recipe-app)

[![codecov](https://codecov.io/gh/springframeworkguru/spring5-mongo-recipe-app/branch/master/graph/badge.svg)](https://codecov.io/gh/springframeworkguru/spring5-mongo-recipe-app)

# spring5-mongo-recipe-app
Recipe Application Using MongoDB

This repository is for an example application built in my Spring Framework 5 - Beginner to Guru

You can learn about my Spring Framework 5 Online course [here.](http://courses.springframework.guru/p/spring-framework-5-begginer-to-guru/?product_id=363173)


This is a project done with Spring Boot Reactive, Gradel, Mongo, JUnit5, Lombok, Mockito and Thymeleaf (front end).

- The idea of this project its to cover reactive programing this is something that I used when working with Node.js something that doesnt block the UI when reciving request and having to talk to other services or just fetch data from the DataBase, we are using MongoDb because it's capable of handling reactive programing MYSQL im not aware at the time of typing this.
- Reactive programing using Spring its by using in this case ReactiveMongoRepository which will be the interface used like the CrudRepository on the normal recipe with Mysql, but in this case Reactive interface whill handle not Optional but Mono (meaning zero or 1 object at the time) or Flux (meaning zero or multiple objects at the time) this are capable of interacting with other things but will not block any thread until for example .block() its called manually or if the Client UI side its capable of handling this then it will consume it without stoping the UI and will run in the background.
