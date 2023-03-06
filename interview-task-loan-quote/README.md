# Command Line Utility to get load quote for 36 month period

There is a need for a rate calculation system allowing prospective borrowers to
obtain a quote from our pool of lenders for 36 month loans. This system will
take the form of a command-line application.

You will be provided with a file containing a list of all the offers being made
by the lenders within the system in CSV format, see the example market.csv file
provided alongside this specification.

You should strive to provide as low a rate to the borrower as is possible to
ensure that quotes are as competitive as they can be against our
competitors'. You should also provide the borrower with the details of the
monthly repayment amount and the total repayment amount.

Repayment amounts should be displayed to 2 decimal places and the rate of the
loan should be displayed to one decimal place.

Borrowers should be able to request a loan of any £100 increment between
£1000 and £15000 inclusive. If the market does not have sufficient offers from
lenders to satisfy the loan then the system should inform the borrower that it
is not possible to provide a quote at that time.

## The application should take arguments in the form:
`[application] [market_file] [loan_amount]`

### Example:
`quote.exe market.csv 1500`

### The application should produce output in the form:
`[application] [market_file] [loan_amount]`

Requested amount: £XXXX

Rate: X.X%

Monthly repayment: £XXXX.XX

Total repayment: £XXXX.XX

## Example:
`quote.exe market.csv 1000`

Requested amount: £1000

Rate: 7.0%

Monthly repayment: £30.78

Total repayment: £1108.10

## Remarks:
- Please, favour a JVM language for your implementation: Java 8+, Kotlin, Scala...
- Your code should be tested to the appropriate degree.
- The solution should exhibit readability, modularity and good use of abstraction.
- The monthly and total repayment should use monthly compounding interest.
- If you have any questions then don't hesitate to contact us.
- Please send us your solution in a tar/zip file with instruction on how to build your
solution.

# Requirements
JDK 11+

# Building
Run: `./gradlew build` <br/>
Note: For Windows the paths will be separated by \ character

# Running
Run: `java -jar build/libs/get-quote-for-loan-on-36-months-period-0.0.1-SNAPSHOT.jar example/market_data.csv 1000` <br/>
Note: For Windows the paths will be separated by \ character

## Author
Maciej Zieniuk

## License
The Unlicense<br/>
See UNLICENSE file
