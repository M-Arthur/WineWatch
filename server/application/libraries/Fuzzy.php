<?php  if ( ! defined('BASEPATH')) exit('No direct script access allowed');
class WI_Fuzzy
{
	private $CI;

	public function __construct()
	{
		$this->CI = &get_instance();
		$this->CI->load->database();
		$this->CI->load->helper(array('temp','file'));
		$this->CI->load->config('fuzzy');
		$this->CI->load->library('notification');
	}

	private function load_data($fid)
	{
		$ferment = $this->CI->db->get_where("FERMENTS",array('fid'=>$fid))->result();
		$temps = $this->load_temps($fid);
		if($temps && sizeof($temps) >= 2)
		{
			for ($i=0;$i < 2;$i++) {
	       		$temps_avg[$i] = temp_avg($temps[$i]);
	   		}
    		return array('ferment'=>$ferment[0], 'temp_avg'=>$temps_avg);
    	}else
    	{
    		return false;
    	}
	}

	private function load_temps($fid)
	{
		$this->CI->db->order_by('update_time','desc');
		$query = $this->CI->db->get_where('READING',array('fid' =>$fid),3)->result();
		if($query)
		{
			$reading = array();
			foreach ($query as $row) 
			{
				$data = json_decode($row->data);
				array_push($reading,$data->{'temps'});
			}
			return $reading;
		}else
		{
			return false;
		}
	}
	
	private function rate_fuzzi($rate)
	{
		$members = $this->CI->config->item('Rate');
		$res = array('fd'=>0,'sd'=>0,'ze'=>0,'si'=>0,'fi'=>0);
		if($rate <= $members[0][2])
		{
			$res['fd'] = 1;
			return $res;

		}else if($rate >= $members[sizeof($members)-1][1])
		{
			$res['fi'] = 1;
			return $res;
		}else
		{
			for($i = 1; $i < sizeof($members)-1;$i++)
			{
				if($rate >= $members[$i][1] && $rate <= $members[$i][2])
				{
					$res[$members[$i][0]] = $rate*$members[$i][3] + $members[$i][4] + $members[$i][5];
				}
			}
			return $res;
		}
	}

	private function avgTemp_fuzzi($avgTemp)
	{
		$members = $this->CI->config->item('AvgTemp');
		$res = array('cold'=>0,'cool'=>0,'hot'=>0);
		if($avgTemp <= $members[0][2])
		{
			$res['cold'] = 1;
			return $res;
		}elseif ($avgTemp >= $members[sizeof($members)-1][1]) {
			$res['hot'] = 1;
			return $res;
		}else
		{
			for($i = 1; $i < sizeof($members)-1;$i++)
			{
				if($avgTemp >= $members[$i][1] && $avgTemp <= $members[$i][2])
				{
					$res[$members[$i][0]] = $avgTemp*$members[$i][3] + $members[$i][4] + $members[$i][5];
				}
			}
			return $res;
		}


	}

	private function output_combine($fuzzi_res,$stage)
	{
		$res = array('cooling'=>0,'do_nothing'=>0,'heating'=>0);
		if($stage == 1 || $stage == 3)
		{
			$rate = $fuzzi_res['rate'];
			
			$res['heating'] += pow($rate['fd'],2);
			 
			$res['do_nothing'] += pow($rate['sd'],2);
			 
			$res['do_nothing'] += pow($rate['ze'],2);
			 
			$res['do_nothing'] += pow($rate['si'],2);
			 
			$res['cooling'] += pow($rate['fi'],2);

		} else
		{
			$rate = $fuzzi_res['rate'];
			$avgtemp = $fuzzi_res['avgtemp'];

			$res['heating'] += pow(min($avgtemp['cold'], $rate['fd']),2);

			$res['heating'] += pow(min($avgtemp['cold'], $rate['sd']),2);

			$res['do_nothing'] += pow(min($rate['ze'], $avgtemp['cold']),2);

			$res['do_nothing'] += pow(min($rate['si'], $avgtemp['cold']),2);

			$res['do_nothing'] += pow(min($rate['fi'], $avgtemp['cold']),2);

			$res['do_nothing'] += pow($avgtemp['cool'],2);

			$res['cooling'] += pow($avgtemp['hot'], 2);

		}
		foreach ($res as $key => $value) {
			$res[$key] = sqrt($value);
		}
		return $res;
	}

	public function fuzzification($fid)
	{
		$package = $this->load_data($fid);
		if(!$package)
		{
			return false;
		}
		$d = ($package['temp_avg'][0] - $package['temp_avg'][1]);
		if($package['ferment']->stage == 1 || $package['ferment']->stage == 3)
		{	
			$fuzzi_res = array('rate' => $this->rate_fuzzi($d));
		}else
		{
			$fuzzi_res['avgtemp'] = $this->avgTemp_fuzzi($package['temp_avg'][0]);
			$fuzzi_res ['rate'] = $this->rate_fuzzi($d);
		} 
		$fuzzi_output = $this->output_combine($fuzzi_res,$package['ferment']->stage);
		$this->defuzzification($fuzzi_output,$package['ferment']->wid,$package['ferment']->fid);
	}

	public function defuzzification($fuzzi_output,$wid,$fid)
	{
		$center = $this->CI->config->item('center');
		$output = ($fuzzi_output['cooling']*$center['cooling'] + $fuzzi_output['do_nothing'] * $center['do_nothing'] + $fuzzi_output['heating'] * $center['heating']);
		if(($fuzzi_output['cooling']+$fuzzi_output['do_nothing']+$fuzzi_output['heating'])!=0)
		{
			$output /=($fuzzi_output['cooling']+$fuzzi_output['do_nothing']+$fuzzi_output['heating']);	
		}
		if($output > 1) 
		{
			$msg['status'] = "Heating";
			$msg['winery'] = $wid;
			$msg['ferment'] = $fid;
			$log_msg = $msg['status']."\n";
			$this->CI->notification->notify($wid, $msg, "Ferment ".$fid);
			if (!write_file(APPPATH.'/logs/fuzzy.log', $log_msg, "a+"))
		    {
		            echo 'Unable to write the file';
		    }
		}else if($output < -1)
		{
			$msg['status'] = "Cooling";
			$msg['winery'] = $wid;
			$msg['ferment'] = $fid;
			$log_msg = $msg['status']."\n";
			$this->CI->notification->notify($wid, $msg, "Ferment ".$fid);
			if (!write_file(APPPATH.'/logs/fuzzy.log', $log_msg, "a+"))
		    {
		            echo 'Unable to write the file';
		    }
		}else
		{
			$msg['status'] = "Ferment ".$fid." is good. ";
			$msg['winery'] = $wid;
			$msg['ferment'] = $fid;
			$log_msg = $msg['status']."\n";
			if (!write_file(APPPATH.'/logs/fuzzy.log', $log_msg, "a+"))
		    {
		            echo 'Unable to write the file';
		    }
		}
	}

}