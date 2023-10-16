package l1j.server.server.model;

import l1j.server.server.utils.IntRange;

public class BasicProperty {
	private String _name; 
	private String _title;
	
	private int _level; 
	private int _exp;
	
	private int _currentHp;
	private int _trueMaxHp;
	private short _maxHp;
	
	private int _currentMp;
	private int _trueMaxMp;
	private short _maxMp;
	
	private int _lawful; 
	private int _karma;
	
	protected L1Character character;
	
	public BasicProperty(L1Character character) {
		this.character = character;
		_level = 1;
	}

	/**
	 * ĳ������ ������ HP�� �����ش�.
	 * 
	 * @return ������ HP
	 */
	public int getCurrentHp() {	return _currentHp; }

	/**
	 * ĳ������ HP�� �����Ѵ�.
	 * 
	 * @param i ĳ������ ���ο� HP
	 */
	public void setCurrentHp(int i) {
		if (i >= getMaxHp()) {
			i = getMaxHp();
		}
		if (i < 0) i = 0;

		_currentHp = i;
	}

	/**
	 * ĳ������ ������ MP�� �����ش�.
	 * 
	 * @return ������ MP
	 */
	public int getCurrentMp() {
		return _currentMp;
	}

	/**
	 * ĳ������ MP�� �����Ѵ�.
	 * 
	 * @param i ĳ������ ���ο� MP
	 */
	public void setCurrentMp(int i) {
		if (i >= getMaxMp()) {
			i = getMaxMp();
		}
		if (i < 0) i = 0;

		_currentMp = i;
	}

	public synchronized int getExp() 		{ return _exp; }
	public synchronized void setExp(int exp) { _exp = exp;  }

	public String getName() 		{ return _name; }
	public void setName(String s) 	{ _name = s; 	}

	public synchronized int getLevel() 				{ return _level; 		}
	public synchronized void setLevel(long level) 	{ _level = (int) level; }

	public short getMaxHp() 	 { return _maxHp; 			 }
	public void addMaxHp(int i)  { setMaxHp(_trueMaxHp + i); }
	public void setMaxHp(int hp) {
		_trueMaxHp = hp;
		_maxHp = (short) IntRange.ensure(_trueMaxHp, 1, 32767);
		_currentHp = Math.min(_currentHp, _maxHp);
	}

	public short getMaxMp() 	 { return _maxMp; }
	public void setMaxMp(int mp) {
		_trueMaxMp = mp;
		_maxMp = (short) IntRange.ensure(_trueMaxMp, 0, 32767);
		_currentMp = Math.min(_currentMp, _maxMp);
	}

	public void addMaxMp(int i) { setMaxMp(_trueMaxMp + i); }
	public void healHp(int pt)  { 
		setCurrentHp(getCurrentHp() + pt);
	}

	public String getTitle() { return _title; }
	public void setTitle(String s) { _title = s; }

	public int getLawful() { return _lawful; }
	public void setLawful(int i) { _lawful = i; }

	public synchronized void addLawful(int i) {
		_lawful += i;
		if 		(_lawful >  32767) { _lawful = 32767;  } 
		else if (_lawful < -32768) { _lawful = -32768; }
	}

	/** ĳ������ ���� �����ش�.	 */
	public int getKarma() {	return _karma; }

	/** ĳ������ ���� �����Ѵ�.	 */
	public void setKarma(int karma) { _karma = karma; }
}
