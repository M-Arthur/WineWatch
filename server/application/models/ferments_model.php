<?php if ( ! defined('BASEPATH')) exit('No direct script access allowed');

class ferments_model extends CI_Model
{
	function __construct()
	{
		parent::__construct();
		$this->load->database();
	}

	public function load_ferments($wid)
	{
		if(!$wid) return false;

		$ferments = array();
		$query = $this->db->get_where('FERMENTS',array('wid'=>$wid))->result();
		if(sizeof($query))
		{
			foreach ($query as $ferment)
			{
				array_push($ferments,$ferment);
			}
			return $ferments;
		}else
		{
			return false;
		}
	}
	

	public function load_ferment($wid,$mid)
	{
		if(!$wid || !$mid) return false;
		
		$this->db->select('wid,mid,fid,t_s,t_e,stage');
		$ferment = $this->db->get_where('FERMENTS',array('wid'=>$wid,"mid"=>$mid,"t_e"=>null))->result();
		if(sizeof($ferment) == 0)
		{
			return false;
		}
		return $ferment[0];
	}


	public function add_ferment($ferment)
	{
		$this->db->trans_start();
		$this->db->insert('FERMENTS',$ferment);
		$this->db->trans_complete();
	}

	public function update_stage($fid,$stage)
	{
		$this->db->where('fid', $fid);
		$this->db->update('FERMENTS',array('stage'=>$stage));
	}
}