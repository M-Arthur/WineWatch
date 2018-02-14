
<?php   if ( ! defined('BASEPATH')) exit('No direct script access allowed');

class commands_model extends CI_Model
{
	function __construct()
	{
		parent::__construct();
		$this->load->database();
	}

	public function update_command($bid,$mid,$command,$args)
	{
		if(!$command || !$mid || !$bid) return false;
		$this->db->insert("COMMANDS", array('bid'=>$bid,'mid'=>$mid,'command'=>$command,'parameter'=>$args));
	}

	public function load_commands($bid)
	{
		if(!$bid) return false;

		$this->db->select('cid,mid,bid,status,command,parameter');
		$commands = $this->db->get_where('COMMANDS',array('bid'=>$bid,'status'=>0))->result();
		if(sizeof($commands)>0)
		{
			$updated_commands = array();
			foreach ($commands as $command) 
			{
				$updated_commands[] = array('cid'=>$command->cid,'status'=>1);
			}
			$this->db->update_batch('COMMANDS',$updated_commands,'cid');
		}
		
		return $commands;
	}

}