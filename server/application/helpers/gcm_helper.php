<?php  if ( ! defined('BASEPATH')) exit('No direct script access allowed');
if ( ! function_exists('gcm_pack'))
{
	function gcm_pack($config, $recipients, $msg, $group)
	{
		$package['collapse_key'] = $group;
		$package['registration_ids'] = $recipients;
		$package = array_merge($package,$config);
		$package['data'] = $msg;
		return json_encode($package);
	}
}

if(! function_exists('gcm_header'))
{
	function gcm_header($type,$key)
	{
		if(isset($type)&& isset($key))
		{
			$header[] = 'Content-Type:application/'.$type;
			$header[] = 'Authorization:key='.$key;
			return $header;
		}
		return false;
	}
}

if(!function_exists('gcm_send'))
{
	function gcm_send($header, $url, $package)
	{
		$curl = curl_init();
		curl_setopt($curl, CURLOPT_URL, $url);
		curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, false);
		curl_setopt($curl, CURLOPT_FOLLOWLOCATION, false);
		curl_setopt($curl, CURLOPT_HTTPHEADER, $header);
		curl_setopt($curl, CURLOPT_HEADER, true);
		curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
		curl_setopt($curl, CURLOPT_POST, true);
		curl_setopt($curl, CURLOPT_POSTFIELDS, $package);
		$response['data'] = curl_exec($curl);
		$response['status'] = curl_getinfo($curl);
		curl_close($curl);
		return $response;
	}
}