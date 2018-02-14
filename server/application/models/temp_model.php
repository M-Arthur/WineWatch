<?php if ( ! defined('BASEPATH')) exit('No direct script access allowed');

class temp_model extends CI_Model
{
	function __construct()
	{
		parent::__construct();
		$this->load->database();
	}

	public function load_temp($fid)
	{
		if(!$fid)
			return false;
		$this->db->order_by('update_time','desc');
		$query = $this->db->get_where('READING',array('fid' =>$fid))->result();
		if(sizeof($query) > 0)
		{
			$reading = array();
			foreach ($query as $data) {
				array_push($reading,$data);
			}
			return $reading;
		}else
		{
			return false;
		}
	}
}