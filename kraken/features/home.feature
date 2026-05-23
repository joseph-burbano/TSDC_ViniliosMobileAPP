Feature: Página principal (ISSUE01)
  @user1 @mobile
  Scenario: Como usuario visitante veo el header y los ultimos albumes en la pantalla principal
    Given I wait
    Then I select role if needed
    Then I tap on element with accessibility id "nav_vinilos"
    Then I wait
    Then I wait
    Then I wait
    Then I see the text "RESUMEN"
    Then I see the text "Vinilos"
    Then I see the text "ÚLTIMOS ÁLBUMES"
    Then I scroll down
    Then I see the text "ARTISTAS CONSULTADOS"
    Then I wait

  @user2 @mobile
  Scenario: Como usuario visitante navego desde la home al detalle de un album
    Given I wait
    Then I select role if needed
    Then I tap on element with accessibility id "nav_vinilos"
    Then I wait
    Then I wait
    Then I wait
    Then I see the text "ÚLTIMOS ÁLBUMES"
    Then I scroll down
    Then I see the text "ARTISTAS CONSULTADOS"
    Then I wait

  @user3 @mobile
  Scenario: Como usuario visitante veo la seccion de coleccionistas en la home
    Given I wait
    Then I select role if needed
    Then I tap on element with accessibility id "nav_vinilos"
    Then I wait
    Then I wait
    Then I wait
    Then I see the text "RESUMEN"
    Then I scroll down
    Then I scroll down
    Then I scroll down
    Then I see the text "COLECCIONISTAS"
    Then I wait
