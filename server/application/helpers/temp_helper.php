<?php  if ( ! defined('BASEPATH')) exit('No direct script access allowed');
if ( ! function_exists('temp_avg'))
{
	function temp_avg($temps)
	{
		if(isset($temps))
		{
			$temp_sum = 0;
			$size = 0;
			foreach ($temps as $temp) {
				$temp_sum += $temp;		
				$size++;
			}
			return $temp_sum/$size;
		}
		return false;
	}
}