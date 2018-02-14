<?php  if ( ! defined('BASEPATH')) exit('No direct script access allowed');
class WI_Auth
{
	private $CI;

	public function __construct()
	{
		$this->CI = &get_instance();
		$this->CI->load->database();
	}

	public function verify_user($uid,$token)
	{
		if(!$uid || !$token)
		{
			return false;
		}

		$query = $this->CI->db->get_where("USERS",array('uid'=>$uid))->result();
		if(sizeof($query) > 0)
		{
			if($token === $query[0]->token)
			{
				return true;
			}else
			{
				return false;
			}
		}else
		{
			return false;
		}

	}
	
	public function verify_winery($wid,$token)
	{
		if(!$wid || !$token)
		{
			return false;
		}

		$query = $this->CI->db->get_where("WINERIES",array('wid'=>$wid))->result();
		if(sizeof($query) > 0)
		{
			if($token === $query[0]->token)
			{
				return true;
			}else
			{
				return false;
			}
		}else
		{
			return false;
		}
	}


	public function verify_base($bid, $token)
	{
		if(!$bid || !$token)
			return false;

		$this->CI->db->select("bid,token");
		$query = $this->CI->db->get_where("BASE_STATIONS",array('bid'=>$bid))->result();
		if(sizeof($query)>0)
		{
			if($token === $query[0]->token)
			{
				return true;
			}
			return false;
		}
		return false;
	}


	public function auth_user_winery($uid,$wid)
	{
		if(!$uid || !$wid)
			return false;
		$query = $this->CI->db->get_where("USERS_WINERIES",array('wid'=>$wid,'uid'=>$uid))->result();
		if(sizeof($query) > 0)
		{
			return true;
		}else
		{
			return false;
		}
	}

	public function verify_winery_ferment($wid,$fid)
	{
		if(!$fid||!$wid) return false;

		$this->CI->db->select("fid,wid");
		$query = $this->CI->db->get_where("FERMENTS",array('wid'=>$wid,'fid'=>$fid))->result();
		if(sizeof($query) > 0)
		{
			return true;
		} 
		return false;
	}

	public function verify_user_base($uid,$bid)
	{
		if(!$uid || !$bid) return false;

		$this->CI->db->select('wid,bid');
		$base = $this->CI->db->get_where('BASE_STATIONS', array('bid'=>$bid))->result();
		if(sizeof($base)>0)
		{
			$this->CI->db->select('uid,wid');
			$user = $this->CI->db->get_where('USERS_WINERIES',array('wid'=>$base[0]->wid,'uid'=>$uid));
			if(sizeof($user)>0)
			{
				return true;
			}
			return false;
		}
		return false;
	}

	public function verify_command_base($cid,$bid)
	{
		if(!$cid || !$bid) return false;

		$this->CI->db->select('cid,bid');
		$command = $this->CI->db->get_where('COMMANDS',array('cid'=>$cid,'bid'=>$bid))->result();
		if(sizeof($command)>0)
		{
			return true;
		}	
		return false;
	}
}