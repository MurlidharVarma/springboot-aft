Feature: Order Posting

  Scenario: Order Receiving Interaction
    Given User places an order
    When Order api is invoked
    Then User should be known that order is posted for "Boombox"
    And Payment should receive the order details