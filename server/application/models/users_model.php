<?php if ( ! defined('BASEPATH')) exit('No direct script access allowed');

class users_model extends CI_Model
{
	function __construct()
	{
		parent::__construct();
		$this->load->database();
	}

	function user_login($mail,$passwd)
	{
		if(!$mail || !$passwd)
		{
			return false;
		}
		
		$query = $this->db->get_where('USERS',array('mail'=>$mail))->result();
		if(sizeof($query) > 0)
		{
			$cal_hp = md5(md5($passwd).$query[0]->salt);
			if($cal_hp === $query[0]->hashed_passwd)
			{
				$res['token'] = md5(md5($mail).'Winewatch@earth^_^');//should store the token in database
				$res['uid'] = $query[0]->uid; //need to disscus.
				$this->db->where('uid',$res['uid']);
				$this->db->update('USERS',$res);
				return $res;
			}
			return false;
		}
		return false;
	}
	
	function update_gcm($uid,$gcm_key)
	{
		if(!$uid || !$gcm_key)
		{
			return false;
		}
		$this->db->where('uid',$uid);
		$this->db->update('USERS',array('gcm_id'=>$gcm_key));
		return true;
	}

}