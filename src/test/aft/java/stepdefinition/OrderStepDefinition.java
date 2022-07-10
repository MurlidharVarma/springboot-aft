package stepdefinition;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderStepDefinition {

    @Given("^User places an order$")
    public void userPlacesAnOrder(){
        log.info("userPlacesAnOrder");
    }

    @When("Order api is invoked")
    public void orderApiIsInvoked() {
        log.info("orderApiIsInvoked");
    }

    @Then("User should be known that order is posted for {string}")
    public void userShouldBeKnownThatOrderIsPostedFor(String message) {
        log.info("userShouldBeKnownThatOrderIsPostedFor:{}",message);
    }

    @And("Payment should receive the order details")
    public void paymentShouldReceiveTheOrderDetails() {
        log.info("paymentShouldReceiveTheOrderDetails");
    }

}
