<?php
declare(strict_types=1);

header('Content-Type: application/json; charset=utf-8');
header('Cache-Control: no-store');

function reply(int $status, array $body = []): never {
    http_response_code($status);
    echo json_encode($body, JSON_THROW_ON_ERROR | JSON_UNESCAPED_UNICODE);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') reply(405);
$directory = dirname(__DIR__) . '/ourtalk-invite-data';
if (!is_dir($directory)) reply(503);
$body = json_decode((string) file_get_contents('php://input', false, null, 0, 4097), true);
if (!is_array($body)) reply(400);
$secret = $body['device'] ?? '';
$action = $body['action'] ?? '';
if (!is_string($secret) || !preg_match('/\A[0-9a-f]{64}\z/', $secret)) reply(400);
$device = hash('sha256', $secret);

try {
    umask(0077);
    $db = new PDO('sqlite:' . $directory . '/contacts.sqlite');
    $db->exec('PRAGMA busy_timeout = 5000');
    $db->exec('CREATE TABLE IF NOT EXISTS invites (
        token TEXT PRIMARY KEY, owner TEXT NOT NULL, name TEXT NOT NULL,
        expires INTEGER NOT NULL, used_by TEXT
    )');
    $db->exec('CREATE TABLE IF NOT EXISTS contacts (
        owner TEXT NOT NULL, peer TEXT NOT NULL, name TEXT NOT NULL,
        PRIMARY KEY(owner, peer)
    )');
    $db->exec('CREATE TABLE IF NOT EXISTS invite_limits (
        bucket TEXT PRIMARY KEY, hits INTEGER NOT NULL, expires INTEGER NOT NULL
    )');
    $now = time();
    if ($action === 'list') {
        $query = $db->prepare('SELECT peer AS id, name FROM contacts WHERE owner = ? ORDER BY name, peer');
        $query->execute([$device]);
        reply(200, ['contacts' => $query->fetchAll(PDO::FETCH_ASSOC)]);
    }
    if (!in_array($action, ['create', 'redeem'], true)) reply(400);
    $name = $body['name'] ?? '';
    if (!is_string($name) || !preg_match('/\A[^\p{C}]{1,60}\z/u', trim($name))) reply(400);
    $name = trim($name);
    $db->exec('BEGIN IMMEDIATE');
    $db->prepare('DELETE FROM invite_limits WHERE expires <= ?')->execute([$now]);
    $db->prepare('DELETE FROM invites WHERE expires <= ?')->execute([$now]);
    $bucket = hash('sha256', (string) ($_SERVER['REMOTE_ADDR'] ?? '') . ':' . intdiv($now, 3600));
    $limit = $db->prepare('INSERT INTO invite_limits VALUES (?, 1, ?) ON CONFLICT(bucket) DO UPDATE SET hits = hits + 1');
    $limit->execute([$bucket, (intdiv($now, 3600) + 1) * 3600]);
    $readLimit = $db->prepare('SELECT hits FROM invite_limits WHERE bucket = ?');
    $readLimit->execute([$bucket]);
    $hits = (int) $readLimit->fetchColumn();
    if ($hits > 60) {
        $db->exec('COMMIT');
        reply(429);
    }
    if ($action === 'create') {
        $token = bin2hex(random_bytes(24));
        $db->prepare('INSERT INTO invites VALUES (?, ?, ?, ?, NULL)')
            ->execute([hash('sha256', $token), $device, $name, $now + 7 * 86400]);
        $db->exec('COMMIT');
        reply(200, ['token' => $token]);
    }
    $token = $body['token'] ?? '';
    if (!is_string($token) || !preg_match('/\A[0-9a-f]{48}\z/', $token)) reply(400);
    $hash = hash('sha256', $token);
    $query = $db->prepare('SELECT owner, name, used_by FROM invites WHERE token = ?');
    $query->execute([$hash]);
    $invite = $query->fetch(PDO::FETCH_ASSOC);
    if (!$invite || $invite['owner'] === $device || ($invite['used_by'] !== null && $invite['used_by'] !== $device)) {
        $db->exec('COMMIT');
        reply(409);
    }
    if ($invite['used_by'] === null) {
        $count = $db->prepare('SELECT COUNT(*) FROM contacts WHERE owner = ? AND peer <> ?');
        foreach ([[$device, $invite['owner']], [$invite['owner'], $device]] as $pair) {
            $count->execute($pair);
            if ((int) $count->fetchColumn() >= 100) {
                $db->exec('COMMIT');
                reply(409);
            }
        }
        $insert = $db->prepare('INSERT OR IGNORE INTO contacts VALUES (?, ?, ?)');
        $insert->execute([$device, $invite['owner'], $invite['name']]);
        $insert->execute([$invite['owner'], $device, $name]);
        $db->prepare('UPDATE invites SET used_by = ? WHERE token = ?')->execute([$device, $hash]);
    }
    $db->exec('COMMIT');
    reply(200);
} catch (Throwable $failure) {
    reply(503);
}
