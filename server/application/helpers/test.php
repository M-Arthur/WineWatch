<?php
	function log_fuzzy($msg,$ferment)
	{
		if(!file_exists("../logs/fuzzy.log"))
		{
			$f = fopen("../logs/fuzzy.log", "w");			
		}else
		{
			$f = fopen("../logs/fuzzy.log", "a");	
		}
		fwrite($f, $ferment."\t".$msg);
		fclose($f);
	}
	log_fuzzy("aaa","vvv");
?>
