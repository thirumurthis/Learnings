#### `.html` file `<form>` element without `action` to redirect, will reload the same page.

#### Below is to perform property validation using Java Bean API Validation and hibernate validation.

- `@CreditCardNumber` annotation uses **Luhn algorithm**.

```
import org.hibernate.validator.constraints.CreditCardNumber;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Pattern;

@CreditCardNumber(message="Not a Valid Credit card number")
private String ccNum;

@Pattern(regexp="^(0[1-9]|1[0-2])([\\/])([1-9][0-9])$", message="Must be formatted MM/YY")
private String ccExpiration;

@Digits(integer=3, fraction=0, message="Invalid CVV")
private String ccCVV;

```

#### In order to disable the default `Hibernate` dependency, we can exclude the hibernate dependency from pom.xml and and include the necessary provider, below uses ExclipseLink instead of Hibernate.

```xml
<dependency>
     <groupId>org.springframework.boot</groupId>
     <artifactId>spring-boot-starter-data-jpa</artifactId>
     <exclusions>
         <exclusion>
            <artifactId>hibernate-entitymanager</artifactId>
            <groupId>org.hibernate</groupId>
         </exclusion>
     </exclusions>
</dependency>
<dependency>
       <groupId>org.eclipse.persistence</groupId>
       <artifactId>eclipselink</artifactId>
       <version>2.5.2</version>
</dependency>
```
#### Lombok annotation

```
@RequiredArgsConstructor
@NoArgsConstructor(access=AccessLevel.PRIVATE, force=true)
```
Notes:

In addition to the JPA-specific annotations, you’ll also note that you’ve added a @NoArgsConstructor annotation at the class level. JPA requires that entities have a noarguments
constructor, so Lombok’s @NoArgsConstructor does that for you. You don’t want to be able to use it, though, so you make it private by setting the access attribute to AccessLevel.PRIVATE. And because there are final properties that must be set, you also set the force attribute to true, which results in the Lombok-generated constructor setting them to null.

The `@RequiredArgsConstructor` is needed since the `@Data` implicitly adds a required arguments constructor, but when a `@NoArgsConstructor` is used, that constructor gets removed. An explicit `@RequiredArgsConstructor` ensures that you’ll still have a required arguments constructor in addition to the private no-arguments constructor.
