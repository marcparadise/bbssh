<?php
$cxn = new mysqli("localhost", "user", "password", "db");
$base_path = "Location: http://" . $_SERVER['SERVER_NAME'] . "/ota";
function trackVersion($version)
{
    global $cxn;
    if ($cxn) {
        $stmta = $cxn->stmt_init();
        if ($stmta->prepare("insert into download_requests (request_ts, ip_addr, os_version) values (NOW(), ?, ?)"))  {
            $stmta->bind_param("ss", $_SERVER['REMOTE_ADDR'], $version);
            $stmta->execute();
            $stmta->close();
         }

    }

}
function getVersion($ua, $ident)
{
    $pos = stripos($ua, $ident) + strlen($ident);
    if ($pos !== false) {
      $pos2 = strpos($ua, " ", $pos);
      if ($pos2 !== false) {
        return trim (substr($ua, $pos, $pos2 - $pos));
      }
    }

}

function get($name) {
    if (array_key_exists($name, $_GET)) {
        return $_GET[$name];
    } else if (array_key_exists($name, $_POST)) {
        return $_POST[$name];
    } else {
        return "";
    }
}
if (get("req") == "version") {
    if (get("app") == "bbssh") {
        echo trim(file_get_contents("version.txt"));
        $key = get("bbkey");
        $platform = get("bbplatform");
        $name = get("bbname");
        $version = get("swversion");
        $app = get("bbssh"); // app version
        $anon = get("anon");
        if ($key == "") {
            $anon = "true";
        }
                // todo - for non-anonymous
                // PRIMARY data includes software version info

        if ($anon != "true") {
            $addr = $_SERVER['REMOTE_ADDR'];
            $anon = 'N';

            if ($cxn) {
                $stmta = $cxn->stmt_init();
                if ($stmta->prepare(
                   "insert into bbssh_usage_tracking (pin_hash, bb_device_name, add_ts, ip_addr, bbssh_ver, bb_software_ver, " .
                   " bb_platform_ver, update_ts) values (?, ?, NOW(), ?, ?, ?, ?, NOW()) " .
                   " ON DUPLICATE KEY UPDATE bb_device_name = ?,  ip_addr = ?, bbssh_ver = ?, " .
                   "   bb_software_ver = ?, bb_platform_ver = ?, update_ts = NOW(), update_count = (update_count + 1) "))  {
                   $stmta->bind_param("sssssssssss", $key, $name, $addr, $app, $version, $platform, $name, $addr, $app, $version, $platform);
                   $stmta->execute();
                   $stmta->close();
               }
            }
            //if ($cxn->error != "") {
            //    die($cxn->error);
            //}
        }
    } else {
        echo "Unknown App";
    }
} else {
// Reference: http://supportforums.blackberry.com/t5/Web-Development/How-to-detect-the-BlackBerry-Browser/ta-p/559862?IID=DEVSF30
// OS version 4.2 - 5.0
// BlackBerry9000/5.0.0.93 Profile/MIDP-2.0 Configuration/CLDC-1.1 VendorID/179
// Mozilla/5.0 (BlackBerry; U; BlackBerry 9800; en-US) AppleWebKit/534.1+ (KHTML, like Gecko) Version/6.0.0.141 Mobile Safari/534.1+
   $ua = strtolower($_SERVER['HTTP_USER_AGENT']);

   if (!(strrpos($ua, "blackberry") === false)) {
	$webkit =  strripos($ua, "webkit") ;
	if ($webkit === false) {
            $version = getVersion($ua, "/");
        } else {
            $version = getVersion($ua, "Version/");
        }

          if ($webkit !== false && strpos($version, "6.0") !== false)
            $final = '6.0.0';
          else if (strpos($version, "5.0") !== false)
             $final = '5.0.0';
          else if (strpos($version, "4.7") !== false)
             $final = '4.7.0';
          else if (strpos($version, "4.6") !== false)
             $final = '4.6.0';
          else if (strpos($version, "4.5") !== false)
             $final = '4.5.0';
          if ($final == "") {
            header($base_path . "/bbssh/choose.html");
          } else {
            header($base_path . "/bbssh/$final/BBSSH.jad");
          }
          trackVersion($version);

   } else {
         trackVersion("Desktop/other");
         header($base_path . "/bbssh/choose.html");
   }
   if ($cxn) {
     $cxn->close();
   }

}
?>
