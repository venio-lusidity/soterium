<?php

/**
 * Simple elFinder driver for MySQL.
 *
 * @author Dmitry (dio) Levashov
 **/
class elFinderVolumeMySQL extends elFinderVolumeDriver {
	
	/**
	 * Driver id
	 * Must be started from letter and contains [a-z0-9]
	 * Used as part of volume id
	 *
	 * @var string
	 **/
	protected $driverId = 'm';
	
	/**
	 * Database object
	 *
	 * @var mysqli
	 **/
	protected $db = null;
	
	/**
	 * Tables to store files
	 *
	 * @var string
	 **/
	protected $tbf = '';
	
	/**
	 * Directory for tmp files
	 * If not set driver will try to use tmbDir as tmpDir
	 *
	 * @var string
	 **/
	protected $tmpPath = '';
	
	/**
	 * Numbers of sql requests (for debug)
	 *
	 * @var int
	 **/
	protected $sqlCnt = 0;
	
	/**
	 * Last db error message
	 *
	 * @var string
	 **/
	protected $dbError = '';
	
	/**
	 * Constructor
	 * Extend options with required fields
	 *
	 * @return void
	 * @author Dmitry (dio) Levashov
	 **/
	public function __construct() {
		$opts = array(
			'host'          => 'localhost',
			'user'          => '',
			'pass'          => '',
			'db'            => '',
			'port'          => null,
			'socket'        => null,
			'files_table'   => 'elfinder_file',
			'tmbPath'       => '',
			'tmpPath'       => ''
		);
		$this->options = array_merge($this->options, $opts);
		$this->options['mimeDetect'] = 'internal';
	}
	
	/*********************************************************************/
	/*                        INIT AND CONFIGURE                         */
	/*********************************************************************/
	
	/**
	 * Prepare driver before mount volume.
	 * Connect to db, check required tables and fetch root file
	 *
	 * @return bool
	 * @author Dmitry (dio) Levashov
	 **/
	protected function init() {
		
		if (!($this->options['host'] || $this->options['socket'])
		||  !$this->options['user'] 
		||  !$this->options['pass'] 
		||  !$this->options['db']
		||  !$this->options['file']
		||  !$this->options['files_table']) {
			return false;
		}
		
		
		$this->db = new mysqli($this->options['host'], $this->options['user'], $this->options['pass'], $this->options['db'], $this->options['port'], $this->options['socket']);
		if ($this->db->connect_error || @mysqli_connect_error()) {
			return false;
		}
		
		$this->db->set_charset('utf8');

		if ($res = $this->db->query('SHOW TABLES')) {
			while ($row = $res->fetch_array()) {
				if ($row[0] == $this->options['files_table']) {
					$this->tbf = $this->options['files_table'];
					break;
				}
			}
		}

		if (!$this->tbf) {
			return false;
		}

		$this->updateCache($this->options['file'], $this->_stat($this->options['file']));

		return true;
	}



	/**
	 * Set tmp file
	 *
	 * @return void
	 * @author Dmitry (dio) Levashov
	 **/
	protected function configure() {
		parent::configure();

		if (($tmp = $this->options['tmpPath'])) {
			if (!file_exists($tmp)) {
				if (@mkdir($tmp)) {
					@chmod($tmp, $this->options['tmbPathMode']);
				}
			}
			
			$this->tmpPath = is_dir($tmp) && is_writable($tmp) ? $tmp : false;
		}
		
		if (!$this->tmpPath && $this->tmbPath && $this->tmbPathWritable) {
			$this->tmpPath = $this->tmbPath;
		}

		$this->mimeDetect = 'internal';
	}
	
	/**
	 * Close connection
	 *
	 * @return void
	 * @author Dmitry (dio) Levashov
	 **/
	public function umount() {
		$this->db->close();
	}
	
	/**
	 * Return debug info for client
	 *
	 * @return array
	 * @author Dmitry (dio) Levashov
	 **/
	public function debug() {
		$debug = parent::debug();
		$debug['sqlCount'] = $this->sqlCnt;
		if ($this->dbError) {
			$debug['dbError'] = $this->dbError;
		}
		return $debug;
	}

	/**
	 * Perform sql query and return result.
	 * Increase sqlCnt and save error if occured
	 *
	 * @param  string  $sql  query
	 * @return misc
	 * @author Dmitry (dio) Levashov
	 **/
	protected function query($sql) {
		$this->sqlCnt++;
		$res = $this->db->query($sql);
		if (!$res) {
			$this->dbError = $this->db->error;
		}
		return $res;
	}

	/**
	 * Create empty object with required mimetype
	 *
	 * @param  string  $file  parent dir file
	 * @param  string  $name  object name
	 * @param  string  $mime  mime type
	 * @return bool
	 * @author Dmitry (dio) Levashov
	 **/
	protected function make($file, $name, $mime) {
		$sql = 'INSERT INTO %s (`parent_id`, `name`, `size`, `mtime`, `mime`, `content`, `read`, `write`) VALUES ("%s", "%s", 0, %d, "%s", "", "%d", "%d")';
		$sql = sprintf($sql, $this->tbf, $file, $this->db->real_escape_string($name), time(), $mime, $this->defaults['read'], $this->defaults['write']);
		// echo $sql;
		return $this->query($sql) && $this->db->affected_rows > 0;
	}

	/**
	 * Return temporary file file for required file
	 *
	 * @param  string  $file   file file
	 * @return string
	 * @author Dmitry (dio) Levashov
	 **/
	protected function tmpname($file) {
		return $this->tmpPath.DIRECTORY_SEPARATOR.md5($file);
	}

	/**
	 * Resize image
	 *
	 * @param  string   $hash    image file
	 * @param  int      $width   new width
	 * @param  int      $height  new height
	 * @param  bool     $crop    crop image
	 * @return array|false
	 * @author Dmitry (dio) Levashov
	 * @author Alexey Sukhotin
	 **/
	public function resize($hash, $width, $height, $x, $y, $mode = 'resize', $bg = '', $degree = 0) {
		if ($this->commandDisabled('resize')) {
			return $this->setError(elFinder::ERROR_PERM_DENIED);
		}
		
		if (($file = $this->file($hash)) == false) {
			return $this->setError(elFinder::ERROR_FILE_NOT_FOUND);
		}
		
		if (!$file['write'] || !$file['read']) {
			return $this->setError(elFinder::ERROR_PERM_DENIED);
		}
		
		$file = $this->decode($hash);
		
		if (!$this->canResize($file, $file)) {
			return $this->setError(elFinder::ERROR_UNSUPPORT_TYPE);
		}

		$img = $this->tmpname($file);
		
		if (!($fp = @fopen($img, 'w+'))) {
			return false;
		}

		if (($res = $this->query('SELECT content FROM '.$this->tbf.' WHERE id="'.$file.'"'))
		&& ($r = $res->fetch_assoc())) {
			fwrite($fp, $r['content']);
			rewind($fp);
			fclose($fp);
		} else {
			return false;
		}


		switch($mode) {
			
			case 'propresize':
				$result = $this->imgResize($img, $width, $height, true, true);
				break;

			case 'crop':
				$result = $this->imgCrop($img, $width, $height, $x, $y);
				break;

			case 'fitsquare':
				$result = $this->imgSquareFit($img, $width, $height, 'center', 'middle', $bg ? $bg : $this->options['tmbBgColor']);
				break;
			
			default:
				$result = $this->imgResize($img, $width, $height, false, true);
				break;				
    	}
		
		if ($result) {
			
			$sql = sprintf('UPDATE %s SET content=LOAD_FILE("%s"), mtime=UNIX_TIMESTAMP() WHERE id=%d', $this->tbf, $this->loadFilePath($img), $file);
			
			if (!$this->query($sql)) {
				$content = file_get_contents($img);
				$sql = sprintf('UPDATE %s SET content="%s", mtime=UNIX_TIMESTAMP() WHERE id=%d', $this->tbf, $this->db->real_escape_string($content), $file);
				if (!$this->query($sql)) {
					@unlink($img);
					return false;
				}
			}
			@unlink($img);
			if (!empty($file['tmb']) && $file['tmb'] != "1") {
				$this->rmTmb($file['tmb']);
			}
			$this->clearcache();
			return $this->stat($file);
		}
		
   		return false;
	}
	

	/*********************************************************************/
	/*                               FS API                              */
	/*********************************************************************/
	
	/**
	 * Cache dir contents
	 *
	 * @param  string  $file  dir file
	 * @return void
	 * @author Dmitry Levashov
	 **/
	protected function cacheDir($file) {
		$this->dirsCache[$file] = array();

		$sql = 'SELECT f.id, f.parent_id, f.name, f.size, f.mtime AS ts, f.mime, f.read, f.write, f.locked, f.hidden, f.width, f.height, IF(ch.id, 1, 0) AS dirs 
				FROM '.$this->tbf.' AS f 
				LEFT JOIN '.$this->tbf.' AS ch ON ch.parent_id=f.id AND ch.mime="directory"
				WHERE f.parent_id="'.$file.'"
				GROUP BY f.id';
				
		$res = $this->query($sql);
		if ($res) {
			while ($row = $res->fetch_assoc()) {
				// debug($row);
				$id = $row['id'];
				if ($row['parent_id']) {
					$row['phash'] = $this->encode($row['parent_id']);
				} 
				
				if ($row['mime'] == 'directory') {
					unset($row['width']);
					unset($row['height']);
				} else {
					unset($row['dirs']);
				}
				
				unset($row['id']);
				unset($row['parent_id']);
				
				
				
				if (($stat = $this->updateCache($id, $row)) && empty($stat['hidden'])) {
					$this->dirsCache[$file][] = $id;
				}
			}
		}
		
		return $this->dirsCache[$file];
	}

	/**
	 * Return array of parents paths (ids)
	 *
	 * @param  int   $file  file file (id)
	 * @return array
	 * @author Dmitry (dio) Levashov
	 **/
	protected function getParents($file) {
		$parents = array();

		while ($file) {
			if ($file = $this->stat($file)) {
				array_unshift($parents, $file);
				$file = isset($file['phash']) ? $this->decode($file['phash']) : false;
			}
		}
		
		if (count($parents)) {
			array_pop($parents);
		}
		return $parents;
	}

	/**
	 * Return correct file file for LOAD_FILE method
	 *
	 * @param  string $file  file file (id)
	 * @return string
	 * @author Troex Nevelin
	 **/
	protected function loadFilePath($file) {
		$realPath = realpath($file);
		if (DIRECTORY_SEPARATOR == '\\') { // windows
			$realPath = str_replace('\\', '\\\\', $realPath);
		}
		return $this->db->real_escape_string($realPath);
	}

	/*********************** paths/urls *************************/
	
	/**
	 * Return parent directory file
	 *
	 * @param  string  $file  file file
	 * @return string
	 * @author Dmitry (dio) Levashov
	 **/
	protected function _dirname($file) {
		return ($stat = $this->stat($file)) ? ($stat['phash'] ? $this->decode($stat['phash']) : $this->root) : false;
	}

	/**
	 * Return file name
	 *
	 * @param  string  $file  file file
	 * @return string
	 * @author Dmitry (dio) Levashov
	 **/
	protected function _basename($file) {
		return ($stat = $this->stat($file)) ? $stat['name'] : false;
	}

	/**
	 * Join dir name and file name and return full file
	 *
	 * @param  string  $dir
	 * @param  string  $name
	 * @return string
	 * @author Dmitry (dio) Levashov
	 **/
	protected function _joinPath($dir, $name) {
		$sql = 'SELECT id FROM '.$this->tbf.' WHERE parent_id="'.$dir.'" AND name="'.$this->db->real_escape_string($name).'"';

		if (($res = $this->query($sql)) && ($r = $res->fetch_assoc())) {
			$this->updateCache($r['id'], $this->_stat($r['id']));
			return $r['id'];
		}
		return -1;
	}
	
	/**
	 * Return normalized file, this works the same as os.file.normpath() in Python
	 *
	 * @param  string  $file  file
	 * @return string
	 * @author Troex Nevelin
	 **/
	protected function _normpath($file) {
		return $file;
	}
	
	/**
	 * Return file file related to root dir
	 *
	 * @param  string  $file  file file
	 * @return string
	 * @author Dmitry (dio) Levashov
	 **/
	protected function _relpath($file) {
		return $file;
	}
	
	/**
	 * Convert file related to root dir into real file
	 *
	 * @param  string  $file  file file
	 * @return string
	 * @author Dmitry (dio) Levashov
	 **/
	protected function _abspath($file) {
		return $file;
	}
	
	/**
	 * Return fake file started from root dir
	 *
	 * @param  string  $file  file file
	 * @return string
	 * @author Dmitry (dio) Levashov
	 **/
	protected function _path($file) {
		if (($file = $this->stat($file)) == false) {
			return '';
		}
		
		$parentsIds = $this->getParents($file);
		$file = '';
		foreach ($parentsIds as $id) {
			$dir = $this->stat($id);
			$file .= $dir['name'].$this->separator;
		}
		return $file.$file['name'];
	}
	
	/**
	 * Return true if $file is children of $parent
	 *
	 * @param  string  $file    file to check
	 * @param  string  $parent  parent file
	 * @return bool
	 * @author Dmitry (dio) Levashov
	 **/
	protected function _inpath($file, $parent) {
		return $file == $parent
			? true
			: in_array($parent, $this->getParents($file));
	}
	
	/***************** file stat ********************/
	/**
	 * Return stat for given file.
	 * Stat contains following fields:
	 * - (int)    size    file size in b. required
	 * - (int)    ts      file modification time in unix time. required
	 * - (string) mime    mimetype. required for folders, others - optionally
	 * - (bool)   read    read permissions. required
	 * - (bool)   write   write permissions. required
	 * - (bool)   locked  is object locked. optionally
	 * - (bool)   hidden  is object hidden. optionally
	 * - (string) alias   for symlinks - link target file relative to root file. optionally
	 * - (string) target  for symlinks - link target file. optionally
	 *
	 * If file does not exists - returns empty array or false.
	 *
	 * @param  string  $file    file file
	 * @return array|false
	 * @author Dmitry (dio) Levashov
	 **/
	protected function _stat($file) {
		$sql = 'SELECT f.id, f.parent_id, f.name, f.size, f.mtime AS ts, f.mime, f.read, f.write, f.locked, f.hidden, f.width, f.height, IF(ch.id, 1, 0) AS dirs
				FROM '.$this->tbf.' AS f 
				LEFT JOIN '.$this->tbf.' AS p ON p.id=f.parent_id
				LEFT JOIN '.$this->tbf.' AS ch ON ch.parent_id=f.id AND ch.mime="directory"
				WHERE f.id="'.$file.'"
				GROUP BY f.id';

		$res = $this->query($sql);
		
		if ($res) {
			$stat = $res->fetch_assoc();
			if ($stat['parent_id']) {
				$stat['phash'] = $this->encode($stat['parent_id']);
			} 
			if ($stat['mime'] == 'directory') {
				unset($stat['width']);
				unset($stat['height']);
			} else {
				unset($stat['dirs']);
			}
			unset($stat['id']);
			unset($stat['parent_id']);
			return $stat;
			
		}
		return array();
	}
	
	/**
	 * Return true if file is dir and has at least one childs directory
	 *
	 * @param  string  $file  dir file
	 * @return bool
	 * @author Dmitry (dio) Levashov
	 **/
	protected function _subdirs($file) {
		return ($stat = $this->stat($file)) && isset($stat['dirs']) ? $stat['dirs'] : false;
	}
	
	/**
	 * Return object width and height
	 * Usualy used for images, but can be realize for video etc...
	 *
	 * @param  string  $file  file file
	 * @param  string  $mime  file mime type
	 * @return string
	 * @author Dmitry (dio) Levashov
	 **/
	protected function _dimensions($file, $mime) {
		return ($stat = $this->stat($file)) && isset($stat['width']) && isset($stat['height']) ? $stat['width'].'x'.$stat['height'] : '';
	}
	
	/******************** file/dir content *********************/
		
	/**
	 * Return files list in directory.
	 *
	 * @param  string  $file  dir file
	 * @return array
	 * @author Dmitry (dio) Levashov
	 **/
	protected function _scandir($file) {
		return isset($this->dirsCache[$file])
			? $this->dirsCache[$file]
			: $this->cacheDir($file);
	}
		
	/**
	 * Open file and return file pointer
	 *
	 * @param  string  $file  file file
	 * @param  string  $mode  open file mode (ignored in this driver)
	 * @return resource|false
	 * @author Dmitry (dio) Levashov
	 **/
	protected function _fopen($file, $mode='rb') {
		$fp = $this->tmbPath
			? @fopen($this->tmpname($file), 'w+')
			: @tmpfile();
		
		
		if ($fp) {
			if (($res = $this->query('SELECT content FROM '.$this->tbf.' WHERE id="'.$file.'"'))
			&& ($r = $res->fetch_assoc())) {
				fwrite($fp, $r['content']);
				rewind($fp);
				return $fp;
			} else {
				$this->_fclose($fp, $file);
			}
		}
		
		return false;
	}
	
	/**
	 * Close opened file
	 *
	 * @param  resource  $fp  file pointer
	 * @return bool
	 * @author Dmitry (dio) Levashov
	 **/
	protected function _fclose($fp, $file='') {
		@fclose($fp);
		if ($file) {
			@unlink($this->tmpname($file));
		}
	}
	
	/********************  file/dir manipulations *************************/
	
	/**
	 * Create dir and return created dir file or false on failed
	 *
	 * @param  string  $file  parent dir file
	 * @param string  $name  new directory name
	 * @return string|bool
	 * @author Dmitry (dio) Levashov
	 **/
	protected function _mkdir($file, $name) {
		return $this->make($file, $name, 'directory') ? $this->_joinPath($file, $name) : false;
	}
	
	/**
	 * Create file and return it's file or false on failed
	 *
	 * @param  string  $file  parent dir file
	 * @param string  $name  new file name
	 * @return string|bool
	 * @author Dmitry (dio) Levashov
	 **/
	protected function _mkfile($file, $name) {
		return $this->make($file, $name, 'text/plain') ? $this->_joinPath($file, $name) : false;
	}
	
	/**
	 * Create symlink. FTP driver does not support symlinks.
	 *
	 * @param  string  $target  link target
	 * @param  string  $file    symlink file
	 * @return bool
	 * @author Dmitry (dio) Levashov
	 **/
	protected function _symlink($target, $file, $name) {
		return false;
	}
	
	/**
	 * Copy file into another file
	 *
	 * @param  string  $source     source file file
	 * @param  string  $targetDir  target directory file
	 * @param  string  $name       new file name
	 * @return bool
	 * @author Dmitry (dio) Levashov
	 **/
	protected function _copy($source, $targetDir, $name) {
		$this->clearcache();
		$id = $this->_joinPath($targetDir, $name);

		$sql = $id > 0
			? sprintf('REPLACE INTO %s (id, parent_id, name, content, size, mtime, mime, width, height, `read`, `write`, `locked`, `hidden`) (SELECT %d, %d, name, content, size, mtime, mime, width, height, `read`, `write`, `locked`, `hidden` FROM %s WHERE id=%d)', $this->tbf, $id, $this->_dirname($id), $this->tbf, $source)
			: sprintf('INSERT INTO %s (parent_id, name, content, size, mtime, mime, width, height, `read`, `write`, `locked`, `hidden`) SELECT %d, "%s", content, size, %d, mime, width, height, `read`, `write`, `locked`, `hidden` FROM %s WHERE id=%d', $this->tbf, $targetDir, $this->db->real_escape_string($name), time(), $this->tbf, $source);

		return $this->query($sql);
	}
	
	/**
	 * Move file into another parent dir.
	 * Return new file file or false.
	 *
	 * @param  string  $source  source file file
	 * @param  string  $target  target dir file
	 * @param  string  $name    file name
	 * @return string|bool
	 * @author Dmitry (dio) Levashov
	 **/
	protected function _move($source, $targetDir, $name) {
		$sql = 'UPDATE %s SET parent_id=%d, name="%s" WHERE id=%d LIMIT 1';
		$sql = sprintf($sql, $this->tbf, $targetDir, $this->db->real_escape_string($name), $source);
		return $this->query($sql) && $this->db->affected_rows > 0;
	}
		
	/**
	 * Remove file
	 *
	 * @param  string  $file  file file
	 * @return bool
	 * @author Dmitry (dio) Levashov
	 **/
	protected function _unlink($file) {
		return $this->query(sprintf('DELETE FROM %s WHERE id=%d AND mime!="directory" LIMIT 1', $this->tbf, $file)) && $this->db->affected_rows;
	}

	/**
	 * Remove dir
	 *
	 * @param  string  $file  dir file
	 * @return bool
	 * @author Dmitry (dio) Levashov
	 **/
	protected function _rmdir($file) {
		return $this->query(sprintf('DELETE FROM %s WHERE id=%d AND mime="directory" LIMIT 1', $this->tbf, $file)) && $this->db->affected_rows;
	}
	
	/**
	 * undocumented function
	 *
	 * @return void
	 * @author Dmitry Levashov
	 **/
	protected function _setContent($file, $fp) {
		rewind($fp);
		$fstat = fstat($fp);
		$size = $fstat['size'];
		
		
	}
	
	/**
	 * Create new file and write into it from file pointer.
	 * Return new file file or false on error.
	 *
	 * @param  resource  $fp   file pointer
	 * @param  string    $dir  target dir file
	 * @param  string    $name file name
	 * @return bool|string
	 * @author Dmitry (dio) Levashov
	 **/
	protected function _save($fp, $dir, $name, $mime, $w, $h) {
		$this->clearcache();
		
		$id = $this->_joinPath($dir, $name);
		rewind($fp);
		$stat = fstat($fp);
		$size = $stat['size'];
		
		if (($tmpfile = tempnam($this->tmpPath, $this->id))) {
			if (($trgfp = fopen($tmpfile, 'wb')) == false) {
				unlink($tmpfile);
			} else {
				while (!feof($fp)) {
					fwrite($trgfp, fread($fp, 8192));
				}
				fclose($trgfp);
				
				$sql = $id > 0
					? 'REPLACE INTO %s (id, parent_id, name, content, size, mtime, mime, width, height) VALUES ('.$id.', %d, "%s", LOAD_FILE("%s"), %d, %d, "%s", %d, %d)'
					: 'INSERT INTO %s (parent_id, name, content, size, mtime, mime, width, height) VALUES (%d, "%s", LOAD_FILE("%s"), %d, %d, "%s", %d, %d)';
				$sql = sprintf($sql, $this->tbf, $dir, $this->db->real_escape_string($name), $this->loadFilePath($tmpfile), $size, time(), $mime, $w, $h);

				$res = $this->query($sql);
				unlink($tmpfile);
				
				if ($res) {
					return $id > 0 ? $id : $this->db->insert_id;
				}
			}
		}

		
		$content = '';
		rewind($fp);
		while (!feof($fp)) {
			$content .= fread($fp, 8192);
		}
		
		$sql = $id > 0
			? 'REPLACE INTO %s (id, parent_id, name, content, size, mtime, mime, width, height) VALUES ('.$id.', %d, "%s", "%s", %d, %d, "%s", %d, %d)'
			: 'INSERT INTO %s (parent_id, name, content, size, mtime, mime, width, height) VALUES (%d, "%s", "%s", %d, %d, "%s", %d, %d)';
		$sql = sprintf($sql, $this->tbf, $dir, $this->db->real_escape_string($name), $this->db->real_escape_string($content), $size, time(), $mime, $w, $h);
		
		unset($content);

		if ($this->query($sql)) {
			return $id > 0 ? $id : $this->db->insert_id;
		}
		
		return false;
	}
	
	/**
	 * Get file contents
	 *
	 * @param  string  $file  file file
	 * @return string|false
	 * @author Dmitry (dio) Levashov
	 **/
	protected function _getContents($file) {
		return ($res = $this->query(sprintf('SELECT content FROM %s WHERE id=%d', $this->tbf, $file))) && ($r = $res->fetch_assoc()) ? $r['content'] : false;
	}
	
	/**
	 * Write a string to a file
	 *
	 * @param  string  $file     file file
	 * @param  string  $content  new file content
	 * @return bool
	 * @author Dmitry (dio) Levashov
	 **/
	protected function _filePutContents($file, $content) {
		return $this->query(sprintf('UPDATE %s SET content="%s", size=%d, mtime=%d WHERE id=%d LIMIT 1', $this->tbf, $this->db->real_escape_string($content), strlen($content), time(), $file));
	}

	/**
	 * Detect available archivers
	 *
	 * @return void
	 **/
	protected function _checkArchivers() {
		return;
	}

	/**
	 * Unpack archive
	 *
	 * @param  string  $file  archive file
	 * @param  array   $arc   archiver command and arguments (same as in $this->archivers)
	 * @return void
	 * @author Dmitry (dio) Levashov
	 * @author Alexey Sukhotin
	 **/
	protected function _unpack($file, $arc) {
		return;
	}

	/**
	 * Recursive symlinks search
	 *
	 * @param  string  $file  file/dir file
	 * @return bool
	 * @author Dmitry (dio) Levashov
	 **/
	protected function _findSymlinks($file) {
		return false;
	}

	/**
	 * Extract files from archive
	 *
	 * @param  string  $file  archive file
	 * @param  array   $arc   archiver command and arguments (same as in $this->archivers)
	 * @return true
	 * @author Dmitry (dio) Levashov, 
	 * @author Alexey Sukhotin
	 **/
	protected function _extract($file, $arc) {
		return false;
	}
	
	/**
	 * Create archive and return its file
	 *
	 * @param  string  $dir    target dir
	 * @param  array   $files  files names list
	 * @param  string  $name   archive name
	 * @param  array   $arc    archiver options
	 * @return string|bool
	 * @author Dmitry (dio) Levashov, 
	 * @author Alexey Sukhotin
	 **/
	protected function _archive($dir, $files, $name, $arc) {
		return false;
	}
	
} // END class 
