package com.esen.jdbc.table;

/**
 * 在修改数据库表的结构时，用此类来记录修改过程中具体修改了哪些结构
 *
 * @author zhuchx
 */
public class TableColumnMetaDataChange {
	/**
	 * 字段名
	 */
	private String name;

	/**
	 * 字段类型，参见DbDefiner.FIELD_TYPE_XX
	 */
	private char type;

	/**
	 * 字段长度
	 */
	private int len;

	/**
	 * 小说位数
	 */
	private int scale;

	/**
	 * 字段的描述信息
	 */
	private String desc;

	/**
	 * 字段默认值
	 */
	private String defaultValue;

	/**
	 * 字段是否可为空，为true时表示可为空，为false表示不可为空
	 */
	private boolean isNullable;

	/**
	 * 字段是否唯一，为true时表示唯一，为false表示可以不唯一
	 */
	private boolean isUnique;

	/**
	 * 是否自动增长字段，为true表示是自动增长字段，为false表示不是自动增长字段
	 */
	private boolean isAutlInc;

	/**
	 * 自动增长字段每次增加的数值
	 */
	private int step;

	/**
	 * 新的字段名，在重命名时使用
	 */
	private String newName;

	/**
	 * 是否设置了描述信息
	 */
	private boolean isSetDesc;

	/**
	 * 是否设置了isNullable参数
	 */
	private boolean isSetNullable;

	/**
	 * 是否设置了isUnique参数
	 */
	private boolean isSetUnique;

	/**
	 * 是否设置了defaultValue参数
	 */
	private boolean isSetDefaultValue;

	/**
	 * 是否设置了自动增长
	 */
	private boolean isSetAutoInc;

	/**
	 * 是否设置了每次自动增长的数值
	 */
	private boolean isSetStep;

	/**
	 * 是否设置了类型
	 */
	private boolean isSetType;

	/**
	 * 是否设置了长度
	 */
	private boolean isSetLen;

	/**
	 * 是否设置了小数位数
	 */
	private boolean isSetScale;

	public TableColumnMetaDataChange(String name) {
		this.name = name;
	}

	public TableColumnMetaDataChange(String name, String newName) {
		this.name = name;
		this.newName = newName;
	}

	public String getName() {
		return name;
	}

	public String getNewName() {
		return this.newName;
	}

	public void setNewName(String newName) {
		this.newName = newName;
	}

	public char getType() {
		return type;
	}

	public void setType(char type) {
		this.type = type;
		this.isSetType = true;
	}

	public boolean isSetType() {
		return this.isSetType;
	}

	public int getLen() {
		return len;
	}

	public void setLen(int len) {
		this.len = len;
		this.isSetLen = true;
	}

	public boolean isSetLen() {
		return this.isSetLen;
	}

	public int getScale() {
		return this.scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
		this.isSetScale = true;
	}

	public boolean isSetScale() {
		return this.isSetScale;
	}

	public String getDesc() {
		return this.desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
		this.isSetDesc = true;
	}

	public boolean isSetDesc() {
		return this.isSetDesc;
	}

	public boolean isNullable() {
		return this.isNullable;
	}

	public void setNullable(boolean isNullable) {
		this.isNullable = isNullable;
		this.isSetNullable = true;
	}

	public boolean isSetNullable() {
		return this.isSetNullable;
	}

	public boolean isUnique() {
		return this.isUnique;
	}

	public void setUnique(boolean isUnique) {
		this.isUnique = isUnique;
		this.isSetUnique = true;
	}

	public boolean isSetUnique() {
		return this.isSetUnique;
	}

	public String getDefaultValue() {
		return this.defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
		this.isSetDefaultValue = true;
	}

	public boolean isSetDefaultValue() {
		return this.isSetDefaultValue;
	}

	public boolean isAutoInc() {
		return this.isAutlInc;
	}

	public void setAutoInc(boolean isAutoInc) {
		this.isAutlInc = isAutoInc;
		this.isSetAutoInc = true;
	}

	public boolean isSetAutoInc() {
		return this.isSetAutoInc;
	}

	public int getStep() {
		return this.step;
	}

	public void setStep(int step) {
		this.step = step;
		this.isSetStep = true;
	}

	public boolean isSetStep() {
		return this.isSetStep;
	}
}
