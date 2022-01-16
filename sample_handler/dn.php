<?php
$data = json_encode( $_REQUEST);
$input_json = file_get_contents('php://input');
$content = date("Y-m-d,h:m:s")."\t".$data ."\t".$input_json;
$file_content = file_get_contents("dn.log");
$file_content = $file_content."\n\r".$content;
file_put_contents( "dn.log", $file_content);
echo json_encode(["success"=>true,"message"=>"Done"]);