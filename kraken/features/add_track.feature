Feature: Asociar canción al álbum (HU08)
  @user1 @mobile
  Scenario: Como coleccionista asocio una canción nueva al álbum desde su detalle
    Given I wait
    Then I select role if needed
    Then I wait
    Then I ensure collector role
    Then I wait
    Then I tap on element with accessibility id "nav_álbumes"
    Then I wait
    Then I wait
    Then I see the text "Álbumes"
    Then I scroll down
    Then I see the text "Buscando América"
    Then I tap on element with text containing "Buscando América"
    Then I wait
    Then I see the text "Salsa"
    Then I scroll down
    Then I scroll down
    Then I see the text "CANCIONES"
    Then I tap on element with accessibility id "album_detail_btn_add_track"
    Then I wait
    Then I see the text "Asociar canción"
    Then I see the text "TRACK ASSOCIATION"
    Then I type "Cancion Kraken HU08" on element with id "input_track_name"
    Then I type "3:45" on element with id "input_track_duration"
    Then I scroll down
    Then I tap on element with accessibility id "btn_submit_track"
    Then I wait
    Then I wait
    Then I wait
    Then I see the text "Buscando América"
    Then I tap on element with accessibility id "Volver"
    Then I wait
    Then I tap on element with accessibility id "nav_vinilos"
    Then I wait

  @user2 @mobile
  Scenario: Validaciones de formulario: campos vacíos muestran errores
    Given I wait
    Then I select role if needed
    Then I wait
    Then I ensure collector role
    Then I wait
    Then I tap on element with accessibility id "nav_álbumes"
    Then I wait
    Then I wait
    Then I see the text "Álbumes"
    Then I scroll down
    Then I see the text "Buscando América"
    Then I tap on element with text containing "Buscando América"
    Then I wait
    Then I scroll down
    Then I scroll down
    Then I see the text "CANCIONES"
    Then I tap on element with accessibility id "album_detail_btn_add_track"
    Then I wait
    Then I see the text "Asociar canción"
    Then I scroll down
    Then I tap on element with accessibility id "btn_submit_track"
    Then I wait
    Then I see the text "El nombre de la canción es obligatorio"
    Then I see the text "La duración es obligatoria"
    Then I tap on element with accessibility id "btn_discard_track"
    Then I wait
    Then I tap on element with accessibility id "Volver"
    Then I wait
    Then I tap on element with accessibility id "nav_vinilos"
    Then I wait
