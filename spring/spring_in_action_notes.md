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
