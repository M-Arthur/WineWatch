<?php if ( ! defined('BASEPATH')) exit('No direct script access allowed');

class Temp extends CI_Controller 
{
	function __construct()
	{
		parent:: __construct();
		$this->load->model('users_model');
		$this->load->model('temp_model');
		$this->load->model('ferments_model');
	}

	public function pull()
	{
		$recv = array
					(
						'uid' => $this->input->post('uid'),
						'token' => $this->input->post('token'),
						'wid' => $this->input->post('wid'),
						'fid' => $this->input->post('fid'),//change here! no need to obtain wid and mid.
					);
		if($this->auth->verify_user($recv['uid'],$recv['token']))
		{
			if($this->auth->auth_user_winery($recv['uid'],$recv['wid']))
			{
				if($this->auth->verify_winery_ferment($recv['wid'],$recv['fid']))
				{
					if(($reading = $this->temp_model->load_temp($recv['fid'])))
					{
						echo json_encode(array("status"=>"success",'reading'=>$reading));
					}else
					{
						echo json_encode(array('status'=>'No Temperature Data'));
					}
				}else
				{
					echo json_encode(array('status'=>'Ferment Not Found'));
				}
			}else
			{
				echo json_encode(array('status'=>'Permission Denied'));
			}
		}else
		{
			echo json_encode(array('status'=>'Permission Denied'));
		}
	}

}