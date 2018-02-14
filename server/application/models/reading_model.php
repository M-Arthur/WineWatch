
<?php   if ( ! defined('BASEPATH')) exit('No direct script access allowed');

class reading_model extends CI_Model
{
	function __construct()
	{
		parent::__construct();
		$this->load->database();
	}

	public function update_temp($data,$fid)
	{	
		if(!$data || !$fid)
		{
			return false;
		}
		if(!$this->db->insert('READING',array('fid' => $fid, 'data' => $data)))
		{
			return false;
		}
		return true;
	}
}