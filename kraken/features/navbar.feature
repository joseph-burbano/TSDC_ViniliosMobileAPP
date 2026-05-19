Feature: Navegación principal (ISSUE02)
  @user1 @mobile
  Scenario: Como usuario verifico que la navbar tiene todas las secciones y son navegables
    Given I wait
    Then I select role if needed
    Then I see the text "Vinilos"
    Then I see the text "Álbumes"
    Then I see the text "Artistas"
    Then I see the text "colecc."
    Then I tap on element with accessibility id "nav_artistas"
    Then I wait
    Then I wait
    Then I wait
    Then I see the text "Artistas"
    Then I tap on element with accessibility id "nav_vinilos"
    Then I wait
    Then I wait
    Then I see the text "Vinilos"
    Then I tap on element with accessibility id "nav_albumes"
    Then I wait
    Then I wait
    Then I see the text "Álbumes"
    Then I tap on element with accessibility id "nav_coleccionistas"
    Then I wait
    Then I wait
    Then I see the text "colecc."
