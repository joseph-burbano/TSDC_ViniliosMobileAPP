Feature: Asociar premios a artistas (HU13)

  @user1 @mobile
  Scenario: Como visitante NO veo la opción de asociar premios en el detalle del artista
    Given I wait
    Then I select role if needed
    Then I wait
    Then I tap on element with accessibility id "nav_artistas"
    Then I wait
    Then I wait
    Then I wait
    Then I tap on element with text containing "Rubén Blades Bellido de Luna"
    Then I wait
    Then I wait
    Then I scroll up
    Then I scroll up
    Then I don't see the text "Asociar premio"
    Then I tap on element with accessibility id "Volver"
    Then I wait
    Then I tap on element with accessibility id "nav_vinilos"
    Then I wait

  @user2 @mobile
  Scenario: Como coleccionista veo la opción de asociar premios y puedo abrir el formulario
    Given I wait
    Then I select role if needed
    Then I wait
    Then I tap on element with accessibility id "nav_artistas"
    Then I wait
    Then I wait
    Then I wait
    Then I tap on element with text containing "Rubén Blades Bellido de Luna"
    Then I wait
    Then I wait
    Then I tap on element with accessibility id "Abrir menú"
    Then I wait
    Then I tap on element with text containing "Hacerse coleccionista"
    Then I wait
    Then I wait
    Then I scroll up
    Then I scroll up
    Then I see the text "LOS GALARDONES"
    Then I tap on element with text containing "Asociar premio"
    Then I wait
    Then I wait
    Then I see the text "Para Rubén"
    Then I see the text "PASO 1"
    Then I see the text "PASO 2"
    Then I tap on element with text containing "Crear un nuevo premio"
    Then I wait
    Then I see the text "Nombre del premio"
    Then I see the text "Organización"
    Then I tap on element with accessibility id "Volver"
    Then I wait
    Then I tap on element with accessibility id "Abrir menú"
    Then I wait
    Then I tap on element with text containing "Salir del modo coleccionista"
    Then I wait
    Then I tap on element with accessibility id "nav_vinilos"
    Then I wait
