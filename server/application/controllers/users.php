<?php if ( ! defined('BASEPATH')) exit('No direct script access allowed');

class Users extends CI_Controller 
{
	function __construct()
	{
		parent::__construct();
		$this->load->model('users_model');
		$this->load->model('wineries_model');
	}

	public function login()
	{
		$recv = array
					(
						'mail'		=> 	$this->input->post('mail'),
						'passwd' 	=> 	$this->input->post('passwd'),
						'gcm_key'	=>	$this->input->post('gcm_key')
					);

		if(($user = $this->users_model->user_login($recv['mail'],$recv['passwd'])))
		{
			if($this->users_model->update_gcm($user['uid'],$recv['gcm_key']))
			{
				if(($wineries = $this->wineries_model->load_wineries($user['uid'])))
				{
					echo json_encode(array_merge($user,array("wineries"=>$wineries,'status'=>'success')));
				}else
				{
				 	echo json_encode(array('status'=>'fail'));
				}
			}else
			{
				echo json_encode(array('status'=>'GCM key incorrect'));
			}
		}else
		{
				echo json_encode(array('status'=>'fail'));
		} 
	}


}