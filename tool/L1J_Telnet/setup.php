<?php
// �f�[�^�x�[�X�ڑ��ݒ�
$hostname_l1jdb = "localhost";
$database_l1jdb = "l1jdb";
$username_l1jdb = "root";
$password_l1jdb = "";
$l1jdb = mysql_pconnect($hostname_l1jdb, $username_l1jdb, $password_l1jdb) or trigger_error(mysql_error(),E_USER_ERROR); 
mysql_query("SET NAMES sjis")or die("can not SET NAMES sjis");

// Telnet�|�[�g
$telnet_port = 23;

// ���O�C���o����access_level
// 0��accounts.access_level��0�̈�ʃ��[�U�[�����O�C���\
// ��ʃ��[�U�[����̃A�N�Z�X����ʂɂ���Ɠ��삪�d�����Ȃ邩������܂���B
$login_access_level = 200;

// �`���b�g�Ď���L���ɂ���
$chat_watch = true;

// �`���b�g�Ď��L�����A�`���b�g���O���X�V����Ԋu�i�b�j
$renewal_time = 2;

// �\������`���b�g���O�̎��
// []����accounts.access_level
//  ()���͈ȉ��Q��
// 0:�ʏ�`���b�g 1:Whisper 2:���� 3:�S�̃`���b�g 4:�����`���b�g
// 11:�p�[�e�B�`���b�g 13:�A���`���b�g 14:�`���b�g�p�[�e�B
$type[0] = array(3);
$type[200] = array(0,1,2,3,4,11,13,14);
?>