<?php
//print_r( $_REQUEST);
$data = json_encode( $_REQUEST);
$input_json = file_get_contents('php://input');
$content = date("Y-m-d,h:m:s")."\t".$data ."\t".$input_json;
$file_content = file_get_contents("mo.log");
$file_content = $file_content."\n\r".$content;
file_put_contents( "mo.log", $file_content);
echo json_encode(["success"=>true,"message"=>"Done"]);
//file_put_contents( ."\n\r".$content)