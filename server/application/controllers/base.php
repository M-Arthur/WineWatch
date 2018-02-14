<?php if ( ! defined('BASEPATH')) exit('No direct script access allowed');

class Base extends CI_Controller 
{

	function __construct()
	{
		parent::__construct();
		$this->load->model('base_model');
	}

	public function index()
	{
		 
	}

	public function add_base()
	{
		$recv = array
				(
					'wid' 	=> 	$this->input->post('wid'),
					'token'	=>	$this->input->post('token'),
					'desc'	=> 	$this->input->post('desc'),
					'uid'	=>	$this->input->post('uid'),
				);
		if($recv['uid'])
		{
			if($this->auth->verify_user($recv['uid'],$recv['token']))
			{

			}else
			{
				echo json_encode(array('status'=>'fail'));
			}
		}else
		{
			if($this->auth->verify_winery($recv['wid'],$recv['token']))
			{
				$reply = $this->base_model->add_base($recv);
				
				echo json_encode(array_merge($reply,array('status'=>'success')));
			}else
			{
				echo json_encode(array('status'=>'fail'));
			}
		}

	}
}

 