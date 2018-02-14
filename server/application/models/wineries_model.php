<?php if ( ! defined('BASEPATH')) exit('No direct script access allowed');

class wineries_model extends CI_Model
{
	function __construct()
	{
		parent::__construct();
		$this->load->database();
	}

	function load_wineries($uid)
	{
		$w_list = $this->db->get_where('USERS_WINERIES',array('uid'=>$uid))->result();
		$wineries = array();
		foreach ($w_list as $w) 
		{
		 	$this->db->select('wid,wname');
		 	$winery = $this->db->get_where('WINERIES',array('wid'=>$w->wid))->result();
		 	if(sizeof($winery)<1)
			{
				return false;
			}
			array_push($wineries,array('wid'=>$w->wid,'wname'=>$winery[0]->wname));
 		}
 		return $wineries;
	}

}