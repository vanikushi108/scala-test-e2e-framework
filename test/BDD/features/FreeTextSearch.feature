Feature: Free text search on World check data

  As a User
  I need a search feature
  So that I can see world check data

  Scenario Outline: free text search
    Given logging service has started
    When I enter URL "BING_URL"
    And I enter "<SEARCH_TERM>" on text field "TEXT_SEARCH_BOX"
    And I click on button "BTN_SEARCH"
    Then I should see "<SEARCH_TERM>" on the Search Results
    Then I should see response status code is 200 for rest "FREE_TEXT_SEARCH_API"
    Examples:
      | SEARCH_TERM   |
      | David Cameron |

