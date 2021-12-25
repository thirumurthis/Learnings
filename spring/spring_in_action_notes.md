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
