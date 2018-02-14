<?php if ( ! defined('BASEPATH')) exit('No direct script access allowed');
class Reading extends CI_Controller
{
	function __construct()
	{
		parent::__construct();	
		$this->load->library(array("auth","fuzzy","notification"));
		$this->load->model(array("reading_model","base_model","ferments_model","temp_model"));
		$this->load->helper(array('temp','file'));
	}

	public function update()
	{
		$recv = array
			(
				'bid'   =>  $this->input->post('bid'),
				'token' =>  $this->input->post('token'),
				'data'  =>  $this->input->post('package'),
				'mid' =>  $this->input->post('mote'),
			);
		if($this->auth->verify_base($recv['bid'],$recv['token']))
		{
			$base = $this->base_model->load_base($recv['bid']);
			if(($ferment = $this->ferments_model->load_ferment($base->wid,$recv['mid'])))
			{
				$this->reading_model->update_temp($recv['data'],$ferment->fid);
				$this->fuzzy->fuzzification($ferment->fid);
				$this->identify_stage($recv['data'],$ferment);
				echo json_encode(array("status"=>"Success"));
			}else
			{
				echo json_encode(array("status"=>"No Ferment For Base: ".$recv['bid']." Mote: ".$recv['mid']));
			}
		}else
		{
			echo json_encode(array("status"=>"Permission denided"));
		}
	}

	private function identify_stage($json_data, $ferment)
	{
		$data = json_decode($json_data);
		$avg_temp = temp_avg($data->{'temps'});
		if($ferment->stage == 1 && $avg_temp >= 27)
		{
			$this->ferments_model->update_stage($ferment->fid,2);
			$log_msg['status'] = "Ferment ".$ferment->fid." change stage 1 to stage 2 \n";
			$this->notification->notify($ferment->wid,$log_msg, "Ferment ".$ferment->fid);
			if (!write_file(APPPATH.'/logs/fuzzy.log', $log_msg['status'], "a+"))
		    {
		            echo 'Unable to write the file';
		    }
	    }
	    else if($ferment->stage == 2)
	    {
	    	$temp_avg = array();
	    	$temps = $this->temp_model->load_temp($ferment->fid);
	    	if(sizeof($temps)>=3)
	    	{
	    		for ($i=0;$i<3;$i++) 
	    		{
	    			$temp = json_decode($temps[$i]->data);
	    			$temp_avg[$i] = temp_avg($temp->{'temps'});
	    		}
	    		if($temp_avg[2]>$temp_avg[1] && $temp_avg[1]>$temp_avg[0])
	    		{
	    			$this->ferments_model->update_stage($ferment->fid,3);
	    			$log_msg['status'] = "Ferment ".$ferment->fid." change stage 2 to stage 3 \n";
	    			$this->notification->notify($ferment->wid,$log_msg, "Ferment ".$ferment->fid);
	    			if (!write_file(APPPATH.'/logs/fuzzy.log', $log_msg['status'], "a+"))
	    			{
	    				echo 'Unable to write the file';
	    			}
	    		}
	    	}
	    }
	}
}
?>