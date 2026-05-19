Feature: Listado de coleccionistas (HU05)
  @user1 @mobile
  Scenario: Como usuario visitante navego al listado de coleccionistas y veo el contenido
    Given I wait
    Then I select role if needed
    Then I tap on element with accessibility id "nav_coleccionistas"
    Then I wait
    Then I wait
    Then I wait
    Then I see the text "DIRECTORIO DE"
    Then I see the text "Coleccionistas"
    Then I see the text "encontrados"
    Then I see the text "Manolo Bellon"
    Then I tap on element with accessibility id "nav_vinilos"
    Then I wait

  @user2 @mobile
  Scenario: Como usuario visitante busco un coleccionista por nombre y veo solo el resultado filtrado
    Given I wait
    Then I select role if needed
    Then I tap on element with accessibility id "nav_coleccionistas"
    Then I wait
    Then I wait
    Then I wait
    Then I see the text "DIRECTORIO DE"
    Then I tap on element with accessibility id "collector_search"
    Then I type "Manolo"
    Then I wait
    Then I see the text "Manolo Bellon"
    Then I don't see the text "Jaime Monsalve"
    Then I tap on element with accessibility id "nav_vinilos"
    Then I wait

  @user3 @mobile
  Scenario: Como usuario visitante busco por artista favorito y veo el coleccionista correcto
    Given I wait
    Then I select role if needed
    Then I tap on element with accessibility id "nav_coleccionistas"
    Then I wait
    Then I wait
    Then I wait
    Then I see the text "DIRECTORIO DE"
    Then I tap on element with accessibility id "collector_search"
    Then I type "Queen"
    Then I wait
    Then I see the text "Jaime Monsalve"
    Then I don't see the text "Manolo Bellon"
    Then I tap on element with accessibility id "nav_vinilos"
    Then I wait
