Feature: Listado de álbumes (HU01)
  @user1 @mobile
  Scenario: Como usuario visitante navego al catálogo de álbumes y veo el contenido
    Given I wait
    Then I select role if needed
    Then I tap on element with accessibility id "nav_albumes"
    Then I wait
    Then I wait
    Then I wait
    Then I scroll up
    Then I see the text "Álbumes"
    Then I scroll down
    Then I see the text "Buscando América"
    Then I tap on element with accessibility id "nav_vinilos"
    Then I wait
