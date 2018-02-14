<?php if ( ! defined('BASEPATH')) exit('No direct script access allowed');

class Ferments extends CI_Controller 
{
	function __construct()
	{
		parent::__construct();
		$this->load->model('ferments_model');
		$this->load->model('base_model');
	}

	public function pull()
	{
		$recv = array
					(
						"uid" 	=> 	$this->input->post('uid'),
						"token" => 	$this->input->post('token'),
						"wid" 	=> 	$this->input->post('wid')
					);
		if($this->auth->verify_user($recv['uid'],$recv['token']))
		{
			if($this->auth->auth_user_winery($recv['uid'],$recv['wid']))
			{
				$ferments = $this->ferments_model->load_ferments($recv['wid']);
				echo json_encode(array("ferments"=>$ferments,"status"=>'success'));
			}else
			{
				echo json_encode(array('status'=>'Permission Denied'));
			}
		
		}else
		{
			echo json_encode(array('status'=>'Permission Denied'));
		}
	}

	public function add_ferment()
	{
		$recv = array
					(
						"uid"		=>	$this->input->post('uid'),
						"token" 	=>	$this->input->post('token'),
						"wid"   	=>  $this->input->post('wid')
					);
		$ferment = array
					(	
						"wid" 		=>	$recv['wid'],
						"tank_num" 	=> 	$this->input->post('tank_num'),
						"bid" 		=> 	$this->input->post('bid'),
						"mid" 		=> 	$this->input->post('mid'),
						"type" 		=> 	$this->input->post('type')
					);
		if(!$this->ferments_model->load_ferment($recv['wid'],$ferment['mid']))
		{
			if($this->auth->verify_user($recv['uid'],$recv['token']))
			{
				if($this->auth->auth_user_winery($recv['uid'],$recv['wid']))
				{
					if($this->base_model->load_base($ferment['bid']))
					{
						$this->ferments_model->add_ferment($ferment);
						echo json_encode(array('status'=>'Success'));
					}else
					{
						echo json_encode(array('status'=>'Base Station not Found'));
					}

				}else
				{
					echo json_encode(array('status'=>'Permission Denied'));
				}
			}else
			{
				echo json_encode(array('status'=>'Permission Denied'));
			}
		}else
		{
			echo json_encode(array('status'=>'Mote Already in Use'));
		}
	}

}