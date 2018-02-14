<?php  if ( ! defined('BASEPATH')) exit('No direct script access allowed');

class WI_Notification
{
	private $CI;
	public function __construct()
	{
		$this->CI = &get_instance();
		$this->CI->load->database();
		$this->CI->load->helper('gcm');
	}

	private function load_users($wid)
	{
		$this->CI->db->select("uid,wid");
		$users_winery = $this->CI->db->get_where("USERS_WINERIES",array('wid'=>$wid))->result();
		$users_id = array();
		foreach ($users_winery as $user) {
			$users_id[] = $user->uid;
		}
		$this->CI->db->select("uid,gcm_id");
		$this->CI->db->where_in("uid",$users_id);
		$users = $this->CI->db->get("USERS")->result();
		$gcm_id = array();
		foreach ($users as $user) {
			if($user->gcm_id!=null)
				$gcm_id[] = $user->gcm_id;
		}
		return $gcm_id;
	}

	private function inject_notification($wid,$notice)
	{
		$in = array(
			'wid' => $wid,
			'description' => $notice
			);
		$this->CI->db->insert('NOTIFICATIONS',$in);
	} 

	public function notify($wid,$notice,$title)
	{
		$gcm_list =$this->load_users($wid);
		if(sizeof($gcm_list)!=0)
		{
			$config['time_to_live'] = $this->CI->config->item('gcm_ttl');
			$config['delay_while_idle'] = $this->CI->config->item('gcm_dwi');
			$header = gcm_header($this->CI->config->item('gcm_type'),$this->CI->config->item('gcm_key'));
			$package = gcm_pack($config,$gcm_list,$notice,$title);
			$response = gcm_send($header, $this->CI->config->item('gcm_url'),$package);	
			$this->inject_notification($wid,$notice['status']);
		}
	}
}