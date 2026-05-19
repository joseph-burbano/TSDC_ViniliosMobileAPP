Feature: Detalle de álbum (HU02)
  @user1 @mobile
  Scenario: Como usuario visitante abro el detalle de un álbum desde el catálogo y lo cierro
    Given I wait
    Then I select role if needed
    Then I wait
    Then I tap on element with accessibility id "nav_albumes"
    Then I wait
    Then I wait
    Then I wait
    Then I see the text "Álbumes"
    Then I scroll down
    Then I see the text "Buscando América"
    Then I tap on element with text containing "Buscando América"
    Then I wait
    Then I see the text "Salsa"
    Then I see the text "ARTISTAS"
    Then I scroll down
    Then I scroll down
    Then I see the text "CANCIONES"
    Then I tap on element with accessibility id "Volver"
    Then I wait
    Then I tap on element with accessibility id "nav_vinilos"
    Then I wait
