Feature: Crear álbum (HU07)
  @user1 @mobile
  Scenario: Como coleccionista creo un álbum nuevo desde el listado de álbumes
    Given I wait
    Then I select role if needed
    Then I wait
    Then I ensure collector role
    Then I wait
    Then I tap on element with accessibility id "nav_albumes"
    Then I wait
    Then I wait
    Then I see the text "Álbumes"
    Then I tap on element with accessibility id "fab_create_album"
    Then I wait
    Then I see the text "Crear Álbum"
    Then I see the text "CATALOGING SYSTEM"
    Then I type "Abbey Road Test" on element with id "input_name"
    Then I type "1969" on element with id "input_release_date"
    Then I scroll down
    Then I scroll down
    Then I scroll down
    Then I type "Classic rock album for testing" on element with id "input_description"
    Then I scroll down
    Then I tap on element with accessibility id "dropdown_genre"
    Then I wait
    Then I tap on element with text containing "Rock"
    Then I wait
    Then I tap on element with accessibility id "dropdown_record_label"
    Then I wait
    Then I tap on element with text containing "Sony Music"
    Then I wait
    Then I tap on element with accessibility id "btn_submit_album"
    Then I wait
    Then I wait
    Then I wait
    Then I see the text "Álbumes"
    Then I tap on element with accessibility id "nav_vinilos"
    Then I wait
