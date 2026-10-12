Feature: Procesamiento de archivos de pago

  Scenario: Procesar correctamente un archivo de pago
    Given tengo el archivo de entrada "payment/payment_001.txt"
    When dejo el archivo en la ruta SFTP de entrada "/input"
    Then espero que aparezca el archivo "payment_001_response.txt" en la ruta SFTP "/output"
    And el archivo de salida debe coincidir con "payment/payment_001_response.txt"