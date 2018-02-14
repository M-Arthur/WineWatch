
<?php   if ( ! defined('BASEPATH')) exit('No direct script access allowed');

class base_model extends CI_Model
{
	function __construct()
	{
		parent::__construct();
		$this->load->database();
	}

	public function add_base($winery)
	{
		//do something
		$desc = $winery['desc']?$winery['desc']:"";
		if($this->db->insert('BASE_STATIONS',array('wid'=>$winery['wid'],'description'=>$desc)))
		{
			return array('bid'=>$this->db->insert_id(),'token' => 'abcdefgasdf');
		}else
		{
			return false;
		}

	} 

	public function load_base($bid)
	{
		$query = $this->db->get_where("BASE_STATIONS",array("bid"=>$bid))->result();
		if($query)
		{
			return $query[0];
		}
		return $query;
	}
}