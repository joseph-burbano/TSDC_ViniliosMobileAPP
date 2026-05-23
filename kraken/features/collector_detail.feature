Feature: Detalle de coleccionista (HU06)
  @user1 @mobile
  Scenario: Como usuario visitante accedo al detalle de un coleccionista y veo su informacion
    Given I wait
    Then I select role if needed
    Then I tap on element with accessibility id "nav_coleccionistas"
    Then I wait
    Then I wait
    Then I wait
    Then I see the text "DIRECTORIO DE"
    Then I tap on element with text containing "Manolo Bellon"
    Then I wait
    Then I wait
    Then I see the text "ELITE CURATOR"
    Then I scroll down
    Then I see the text "The Vault"
    Then I tap on element with accessibility id "Volver"
    Then I wait
    Then I tap on element with accessibility id "nav_vinilos"
    Then I wait

  @user2 @mobile
  Scenario: Como usuario visitante puedo regresar desde el detalle al listado
    Given I wait
    Then I select role if needed
    Then I tap on element with accessibility id "nav_coleccionistas"
    Then I wait
    Then I wait
    Then I wait
    Then I see the text "DIRECTORIO DE"
    Then I tap on element with text containing "Manolo Bellon"
    Then I wait
    Then I wait
    Then I tap on element with accessibility id "Volver"
    Then I wait
    Then I see the text "DIRECTORIO DE"
    Then I tap on element with accessibility id "nav_vinilos"
    Then I wait
