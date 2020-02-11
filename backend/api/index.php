<?php
    error_reporting(E_ALL);
    ini_set('display_errors', 1);
    
    // required headers
    header("Access-Control-Allow-Origin: *");
    header("Content-Type: application/json; charset=UTF-8");
    header("Access-Control-Allow-Methods: POST");
    header("Access-Control-Max-Age: 3600");
    header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");
    
    // include database
    include_once 'DB.php';
    include_once 'handlerequest.php';

    // instantiate database and product object
    $database = new Database();
    $db = $database->getConnection();

    // get posted data
    $inputRequest = json_decode(file_get_contents("php://input"));
    // get action
    $actionRequest = $inputRequest->action;

    $validRequest = false;

    if($actionRequest > 0){
        //force autentication in every request
        $vars = "";

        if(!isset($inputRequest->username) || !isset($inputRequest->psw)){
            echo json_encode(
                array("result" => "Wrong request")
            );
            die;
        }
        
        //init handler
        $handler = new HandleRequest($db);
        $handler->setParameters($actionRequest, $vars);

        if($handler->validLogin($inputRequest->username, $inputRequest->psw))
            $validRequest = true;
    }

    if(!$validRequest
        && $actionRequest > 0){
        echo json_encode(
            array("result" => "Auth error")
        );
        die;
    }

    //login request
    if($actionRequest == 0)
        $vars = ["username" => $inputRequest->username, "psw" => $inputRequest->psw];
    else if($actionRequest == 1){//data request, 1
        $vars = array();
        $vars["device"] = $inputRequest->device;

        if(isset($inputRequest->RIL)){
            $vars["RIL"] = (array)$inputRequest->RIL;            
        } 
        if(isset($inputRequest->startTime)){
            $vars["startTime"] = $inputRequest->startTime;
        } 
        if(isset($inputRequest->endTime)){
            $vars["endTime"] = $inputRequest->endTime;            
        }        
    } else if($actionRequest == 2){
        $vars = array();        
        $vars["device"] = $inputRequest->device;

        if(isset($inputRequest->startTime)){
            $vars["startTime"] = $inputRequest->startTime;
        } 
        if(isset($inputRequest->endTime)){
            $vars["endTime"] = $inputRequest->endTime;            
        }
    } else if($actionRequest == 3){
        $vars = "";
    }

    //init handler
    $handler = new HandleRequest($db);
    $handler->setParameters($actionRequest, $vars);

    $queryRes = $handler->execQuery();
    $num = $queryRes->rowCount();

    if($num > 0){
        // products array
        $products_arr=array();
        $products_arr["result"]=array();

        while ($row = $queryRes->fetch(PDO::FETCH_ASSOC)){
            //print_r($row);
            
            extract($row);
            
            // valori estratti dal database, quindi coincidenti con 
            // il template SQL nel database (ovviamente i parametri a destra)
            if($actionRequest == 1){   
                $product_item=array(
                    "ParName" => $Nome_parametro,
                    "ParValue" => $Valore_parametro,
                    "Timestamp" => $Timestamp
                );
            } else if($actionRequest == 2){
                $product_item=array(
                    "Latitude" => $Latitudine,
                    "Longitude" => $Longitudine,
                    "Timestamp" => $Timestamp
                );
            } else if($actionRequest == 3){
                $product_item = array(
                    "ID" => $ID_DISPOSITIVO
                );
            }

            switch($actionRequest){
                case 0:
                    if(strcmp($Password, $vars['psw']) == 0)
                        echo json_encode(
                            array("result" => "Login OK")
                        );
                    else
                        echo json_encode(
                            array("result" => "Bad login")
                        );
                    break;
                case 1:
                    array_push($products_arr["result"], $product_item);
                    break;
                case 2:
                    array_push($products_arr["result"], $product_item);
                    break;
                case 3:
                    array_push($products_arr["result"], $product_item);
                    break;                    
            }   
        }
        if($actionRequest >= 1)
            echo json_encode($products_arr);        
    } else {
        echo json_encode(
            array("result" => "No result")
        );
    }
?>
