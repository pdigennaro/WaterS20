<?php
    error_reporting(E_ALL);
    ini_set('display_errors', 1);

    class HandleRequest {
        private $connection;
        private $type = -1;
        private $vars;

        public function __construct($db){
            $this->connection = $db;
        }

        public function setParameters($type, $vars){
            $this->type = $type;
            $this->vars = $vars;
        }

        public function validLogin($user, $psw){
            $query = "SELECT * FROM Utenti WHERE Username='$user' AND Password='$psw'";

            // prepare query
            $stmt = $this->connection->prepare($query);  
            // exec query      
            $stmt->execute();
            $num = $stmt->rowCount();
            
            //should get exactly one result 
            //login is OK...
            if($num > 0)
                return true;
            else 
                return false;
        }

        public function execQuery(){
            $query = "";

            switch($this->type){
                case 0: //login
                    $query = "SELECT * FROM Utenti WHERE Username='{$this->vars['username']}'";
                    break;
                case 1: //data request
                    $query = "SELECT Nome_parametro, Valore_parametro, Timestamp";
                    $wherec = " WHERE ";
                    $whereone = true;

                    $timeSet = false;
                    $limitC = "";

                    if(isset($this->vars["RIL"])){
                        for($k = 0; $k < count($this->vars["RIL"]); $k++){
                            $whereone = false;
                            $comma = "";

                            if($k > 0)
                                $comma = " OR ";

                            $wherec = $wherec . $comma . "Nome_parametro='" . $this->vars["RIL"][$k] . "'";
                        }
                    }

                    if(isset($this->vars["startTime"])){
                        if($whereone)
                            $wherec = $wherec . "Timestamp>=" . $this->vars["startTime"];                        
                        else
                            $wherec = $wherec . " AND Timestamp>=" . $this->vars["startTime"];

                        $whereone = false;    
                        $timeSet = true;                    
                    }
                    
                    if(isset($this->vars["endTime"])){
                        if($whereone)
                            $wherec = $wherec . "Timestamp<=" . $this->vars["endTime"];                             
                        else
                            $wherec = $wherec . " AND Timestamp<=" . $this->vars["endTime"];     
                        
                        $whereone = false;     
                        $timeSet = true;                   
                    }                        

                    $query = $query . " FROM Rilevamenti";

                    if($whereone)
                        $wherec = $wherec . "ID_DISPOSITIVO='" . $this->vars['device'] . "'";
                    else
                        $wherec = $wherec . " AND ID_DISPOSITIVO='" . $this->vars['device'] . "'";

                    if(!$timeSet)
                        $limitC = " LIMIT 6";

                    $query = $query . $wherec . " ORDER BY Timestamp DESC" . $limitC . ";";
                    break;

                case 2: //GPS data request
                    $query = "SELECT Latitudine, Longitudine, Timestamp"; 
                    $query = $query . " FROM GPS ";                    
                    $wherec = " WHERE ";
                    $whereone = true;

                    $timeSet = false;
                    $limitC = "";

                    if(isset($this->vars["startTime"])){
                        if($whereone)
                            $wherec = $wherec . "Timestamp>=" . $this->vars["startTime"];                        
                        else
                            $wherec = $wherec . " AND Timestamp>=" . $this->vars["startTime"];

                        $whereone = false;   
                        $timeSet = true;                                            
                    }
                
                    if(isset($this->vars["endTime"])){
                        if($whereone)
                            $wherec = $wherec . "Timestamp<=" . $this->vars["endTime"];                             
                        else
                            $wherec = $wherec . " AND Timestamp<=" . $this->vars["endTime"];     
                    
                        $whereone = false;   
                        $timeSet = true;                        
                    }         
                    
                    if($whereone)
                        $wherec = $wherec . "ID_DISPOSITIVO_GPS='" . $this->vars['device'] . "'";
                    else
                        $wherec = $wherec . " AND ID_DISPOSITIVO_GPS='" . $this->vars['device'] . "'";

                    if(!$timeSet)
                        $limitC = " LIMIT 6";

                    $query = $query . $wherec . " ORDER BY Timestamp DESC" . $limitC . ";";         
                break;

                case 3: //devices' list request
                    $query = "SELECT DISTINCT ID_DISPOSITIVO FROM Dispositivi;";
                    break;
            }

            //echo $query;

            // prepare query
            $stmt = $this->connection->prepare($query);  
            // exec query      
            $stmt->execute();
            return $stmt;
        }

        public function insertData(){
            $time1 = $this->vars['time'];
            $insquery = "";

            // water data acquisition
            if($this->type == 0){
                $insquery = "INSERT INTO Rilevamenti VALUES('', '{$this->vars['device']}', 'temp', '{$this->vars['temp']}', '{$time1}');";
                $stmt = $this->connection->prepare($insquery);
                $stmt->execute();

                $insquery = "INSERT INTO Rilevamenti VALUES('', '{$this->vars['device']}', 'torb', '{$this->vars['turb']}', '{$time1}');";
                $stmt = $this->connection->prepare($insquery);
                $stmt->execute();

                $insquery = "INSERT INTO Rilevamenti VALUES('', '{$this->vars['device']}', 'ph', '{$this->vars['ph']}', '{$time1}');";                
                $stmt = $this->connection->prepare($insquery);
                $stmt->execute();
            } else { // GPS data acquisition
                $insquery = "INSERT INTO GPS VALUES('', '{$this->vars['device']}', '{$this->vars['lat']}', '{$this->vars['long']}', '{$time1}');";
                $stmt = $this->connection->prepare($insquery);
                $stmt->execute();
            }
        }
    }
?>