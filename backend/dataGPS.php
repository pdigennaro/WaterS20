<?php
  error_reporting(E_ALL);
  ini_set('display_errors', 1);

  include_once 'api/DB.php';
  include_once 'api/handlerequest.php';

  // instantiate database and product object
  $database = new Database();
  $db = $database->getConnection();

  $handler = new HandleRequest($db);

  // get posted data
  $inputRequest = json_decode(file_get_contents("php://input")); 
  // get action
  $type = $inputRequest->type;

  //for GPS data acquisition
  if($type == 1){
    $requestBody = file_get_contents('php://input');

    /*if ( $fl = fopen('sigfoxGPS.json','a')) {
      fwrite($fl, $requestBody . "\n" );
      fclose($fl);
    }*/

    $handler->setParameters(1, (array)$inputRequest);
    $handler->insertData();
  }
?>
