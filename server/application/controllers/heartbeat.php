<?php if ( ! defined('BASEPATH')) exit('No direct script access allowed');

class Heartbeat extends CI_Controller 
{
	function __construct()
	{
		parent::__construct();
		$this->load->model('commands_model');
		$this->load->model('base_model');
		$this->load->library('notification');
	}

	public function update_command()
	{
		$recv = array
					(
						"uid"		=>	$this->input->post('uid'),
						"token" 	=>	$this->input->post('token'),
						"bid"   	=>  $this->input->post('bid'),
						"mid"		=>	$this->input->post('mid'),
						"command"	=>	$this->input->post('command'),
						"parameter"	=>	$this->input->post('parameter')
					);

		if($this->auth->verify_user($recv['uid'],$recv['token']))
		{
			if($this->auth->verify_user_base($recv['uid'],$recv['bid']))
			{
				$this->commands_model->update_command($recv['bid'],$recv['mid'],$recv['command'],$recv['parameter']);
				echo json_encode(array('status'=>'Success'));
			}else
			{
				echo json_encode(array('status'=>'Permission Denied'));
			}
		}else
		{
			echo json_encode(array('status'=>'Permission Denied'));
		}
	}

	public function index()
	{
		$recv = array
					(
						"token" 	=>	$this->input->post('token'),
						"bid"   	=>  $this->input->post('bid'),
						"motes"		=>	$this->input->post('inactive_motes')
					);
		if($this->auth->verify_base($recv['bid'],$recv['token']))
		{
			$base = $this->base_model->load_base($recv['bid']);
			$msg['status']	= "Winery ".$base->wid." Base station ".$recv['bid']." motes are inactived";
			$msg['motes']	= $mids;
			$title = "Inactived Motes";
			$this->notification->notify($base->wid,$msg,$title);
			echo json_encode(array('status'=>'Success'));
		}else
		{
			echo json_encode(array('status'=>'Permission Denied'));
		}
	}

	public function query_command()
	{
		$recv = array
					(
						"token" 	=>	$this->input->post('token'),
						"bid"   	=>  $this->input->post('bid'),
					);
		if($this->auth->verify_base($recv['bid'],$recv['token']))
		{
			$commands = $this->commands_model->load_commands($recv['bid']);
			$com = array();
			foreach ($commands as $command) {
				$com[] = array
					(
					'command'	=>	$command->command,
					'parameter'	=>	$command->parameter,
					'mote' 		=>	$command->mid
					);
			 
			}
			echo json_encode(array('status'=>'Success','commands'=>$com));
		}else
		{
			echo json_encode(array('status'=>'Permission Denied'));
		}
	}

}