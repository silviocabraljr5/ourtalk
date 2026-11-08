<?php
declare(strict_types=1);
ini_set('display_errors', '0');
header('Content-Type: application/json; charset=utf-8');
header('Cache-Control: no-store');
header('X-Content-Type-Options: nosniff');
function reply(int $status, array $body): void { http_response_code($status); echo json_encode($body, JSON_UNESCAPED_UNICODE); exit; }
if (($_SERVER['REQUEST_METHOD'] ?? '') !== 'POST') reply(405, ['error'=>'method_not_allowed']);
if (!in_array($_SERVER['HTTP_ORIGIN'] ?? '', ['https://ourtalk.com.br','https://www.ourtalk.com.br'], true)) reply(403, ['error'=>'invalid_origin']);
if (stripos($_SERVER['CONTENT_TYPE'] ?? '', 'application/json') !== 0) reply(415, ['error'=>'json_required']);
if ((int)($_SERVER['CONTENT_LENGTH'] ?? 0) > 4096) reply(413, ['error'=>'too_large']);
$raw = file_get_contents('php://input', false, null, 0, 4097);
if ($raw === false || strlen($raw) > 4096) reply(413, ['error'=>'too_large']);
$data = json_decode($raw, true);
if (!is_array($data)) reply(400, ['error'=>'invalid_json']);
foreach (['email','whatsapp','company'] as $key) if (isset($data[$key]) && !is_string($data[$key])) reply(400, ['error'=>'invalid_contact']);
if (trim($data['company'] ?? '') !== '') reply(200, ['accepted'=>true]);
if (($data['consent'] ?? null) !== true) reply(400, ['error'=>'consent_required']);
$email = strtolower(trim($data['email'] ?? ''));
$phoneRaw = trim($data['whatsapp'] ?? '');
$phone = '';
if ($email !== '' && (strlen($email)>254 || !filter_var($email,FILTER_VALIDATE_EMAIL))) reply(400,['error'=>'invalid_email']);
if ($phoneRaw !== '') {
 if (strlen($phoneRaw)>24 || preg_match('/[^0-9+() .-]/', $phoneRaw)) reply(400,['error'=>'invalid_whatsapp']);
 $digits=preg_replace('/[^0-9]/','',$phoneRaw);
 if (str_starts_with($phoneRaw,'+')) {
  if (!preg_match('/^[1-9][0-9]{7,14}$/',$digits)) reply(400,['error'=>'invalid_whatsapp']);
 } elseif (preg_match('/^[1-9][0-9]{9,10}$/',$digits)) { $digits='55'.$digits; }
 elseif (!preg_match('/^55[1-9][0-9]{9,10}$/',$digits)) reply(400,['error'=>'invalid_whatsapp']);
 $phone='+'.$digits;
}
if ($email==='' && $phone==='') reply(400,['error'=>'contact_required']);
try {
 umask(0077);
 $directory=dirname(__DIR__).'/ourtalk-data';
 if (!is_dir($directory) && !mkdir($directory,0700,true) && !is_dir($directory)) throw new RuntimeException('storage');
 $keyFile=$directory.'/rate.key';
 if (!file_exists($keyFile)) { $keyHandle=@fopen($keyFile,'x'); if ($keyHandle) { fwrite($keyHandle,bin2hex(random_bytes(32))); fclose($keyHandle); } }
 $secret=file_get_contents($keyFile);
 if (!$secret) throw new RuntimeException('rate_key');
 $db=new PDO('sqlite:'.$directory.'/subscribers.sqlite',null,null,[PDO::ATTR_ERRMODE=>PDO::ERRMODE_EXCEPTION]);
 $db->exec('PRAGMA busy_timeout=5000');
 $db->exec('CREATE TABLE IF NOT EXISTS subscriptions (id TEXT PRIMARY KEY,channel TEXT NOT NULL,address TEXT NOT NULL,consent_version TEXT NOT NULL,consent_text TEXT NOT NULL,created_at TEXT NOT NULL,verified_at TEXT,UNIQUE(channel,address))');
 $db->exec('CREATE TABLE IF NOT EXISTS request_limits (bucket INTEGER NOT NULL,client TEXT NOT NULL,total INTEGER NOT NULL,PRIMARY KEY(bucket,client))');
 $bucket=(int)floor(time()/3600);
 $client=hash_hmac('sha256',($_SERVER['REMOTE_ADDR'] ?? 'unknown').':'.$bucket,$secret);
 $db->exec('BEGIN IMMEDIATE');
 $check=$db->prepare('SELECT total FROM request_limits WHERE bucket=? AND client=?');$check->execute([$bucket,$client]);
 if ((int)$check->fetchColumn()>=6) { $db->exec('ROLLBACK'); reply(429,['error'=>'try_later']); }
 $limit=$db->prepare('INSERT INTO request_limits(bucket,client,total) VALUES(?,?,1) ON CONFLICT(bucket,client) DO UPDATE SET total=total+1');$limit->execute([$bucket,$client]);
 $delete=$db->prepare('DELETE FROM request_limits WHERE bucket<?');$delete->execute([$bucket-24]);
 $insert=$db->prepare('INSERT OR IGNORE INTO subscriptions(id,channel,address,consent_version,consent_text,created_at) VALUES(?,?,?,?,?,?)');
 $consent='Quero receber o aviso de lançamento pelos canais que informei.';
 foreach (['email'=>$email,'whatsapp'=>$phone] as $channel=>$address) if ($address!=='') $insert->execute([bin2hex(random_bytes(16)),$channel,$address,'launch-2026-11-08',$consent,gmdate('c')]);
 $db->exec('COMMIT');
 reply(200,['accepted'=>true]);
} catch (Throwable $error) {
 if (isset($db)) { try {$db->exec('ROLLBACK');} catch(Throwable $ignored) {} }
 error_log('OurTalk newsletter storage unavailable');
 reply(503,['error'=>'unavailable']);
}
