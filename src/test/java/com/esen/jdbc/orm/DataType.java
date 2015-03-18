package com.esen.jdbc.orm;

import org.w3c.dom.Document;

public class DataType {
	//id
	private int id;
	
	//整型  I
	private int cint1;
	private Integer cint2;

	private long clong1;
	private Long clong2;
	
	private byte cbyte1;
	private Byte cbyte2;

	//浮点型  N
	private double cdouble1;
	private Double cdouble2;
	
	private float cfloat1;
	private Float cfloat2;

	//字符串类型 C
	private String cstr;
	private StringBuffer cstrbuf;
	private StringBuilder cstrbuilder;
	
	private char cchar1;
	private Character cchar2;

	//逻辑类型 L
	private boolean cbool1;
	private Boolean cbool2;
	
	//>0 true, <=0 为 false
	private int cbool3;
	private Integer cbool4;
	
	//日期+时间类型  P 
	private java.util.Calendar ccal;
	private java.util.Date cutilDate;
	private java.sql.Date csqlDate;
	private java.sql.Timestamp ctimestamp;
	
	//二进制类型 X
	private Blob cblob;
	
	//字符大对象 M
	private String cclob;
	
	private Document cdocument1; //CLOB
	
	private Document cdocument2; //BLOB

	public DataType() {
	}

	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public int getCint1() {
		return cint1;
	}

	public void setCint1(int cint1) {
		this.cint1 = cint1;
	}

	public Integer getCint2() {
		return cint2;
	}

	public void setCint2(Integer cint2) {
		this.cint2 = cint2;
	}

	public long getClong1() {
		return clong1;
	}

	public void setClong1(long clong1) {
		this.clong1 = clong1;
	}

	public Long getClong2() {
		return clong2;
	}

	public void setClong2(Long clong2) {
		this.clong2 = clong2;
	}

	public byte getCbyte1() {
		return cbyte1;
	}

	public void setCbyte1(byte cbyte1) {
		this.cbyte1 = cbyte1;
	}

	public Byte getCbyte2() {
		return cbyte2;
	}

	public void setCbyte2(Byte cbyte2) {
		this.cbyte2 = cbyte2;
	}

	public double getCdouble1() {
		return cdouble1;
	}

	public void setCdouble1(double cdouble1) {
		this.cdouble1 = cdouble1;
	}

	public Double getCdouble2() {
		return cdouble2;
	}

	public void setCdouble2(Double cdouble2) {
		this.cdouble2 = cdouble2;
	}

	public float getCfloat1() {
		return cfloat1;
	}

	public void setCfloat1(float cfloat1) {
		this.cfloat1 = cfloat1;
	}

	public Float getCfloat2() {
		return cfloat2;
	}

	public void setCfloat2(Float cfloat2) {
		this.cfloat2 = cfloat2;
	}

	public String getCstr() {
		return cstr;
	}

	public void setCstr(String cstr) {
		this.cstr = cstr;
	}

	public StringBuffer getCstrbuf() {
		return cstrbuf;
	}

	public void setCstrbuf(StringBuffer cstrbuf) {
		this.cstrbuf = cstrbuf;
	}

	public StringBuilder getCstrbuilder() {
		return cstrbuilder;
	}

	public void setCstrbuilder(StringBuilder cstrbuilder) {
		this.cstrbuilder = cstrbuilder;
	}

	public char getCchar1() {
		return cchar1;
	}

	public void setCchar1(char cchar1) {
		this.cchar1 = cchar1;
	}

	public Character getCchar2() {
		return cchar2;
	}

	public void setCchar2(Character cchar2) {
		this.cchar2 = cchar2;
	}

	public boolean isCbool1() {
		return cbool1;
	}

	public void setCbool1(boolean cbool1) {
		this.cbool1 = cbool1;
	}

	public Boolean getCbool2() {
		return cbool2;
	}

	public void setCbool2(Boolean cbool2) {
		this.cbool2 = cbool2;
	}

	public int getCbool3() {
		return cbool3;
	}

	public void setCbool3(int cbool3) {
		this.cbool3 = cbool3;
	}

	public Integer getCbool4() {
		return cbool4;
	}

	public void setCbool4(Integer cbool4) {
		this.cbool4 = cbool4;
	}

	public java.util.Calendar getCcal() {
		return ccal;
	}

	public void setCcal(java.util.Calendar ccal) {
		this.ccal = ccal;
	}

	public java.util.Date getCutilDate() {
		return cutilDate;
	}

	public void setCutilDate(java.util.Date cutilDate) {
		this.cutilDate = cutilDate;
	}

	public java.sql.Date getCsqlDate() {
		return csqlDate;
	}

	public void setCsqlDate(java.sql.Date csqlDate) {
		this.csqlDate = csqlDate;
	}

	public java.sql.Timestamp getCtimestamp() {
		return ctimestamp;
	}

	public void setCtimestamp(java.sql.Timestamp ctimestamp) {
		this.ctimestamp = ctimestamp;
	}

	public Blob getCblob() {
		return cblob;
	}

	public void setCblob(Blob cblob) {
		this.cblob = cblob;
	}

	public String getCclob() {
		return cclob;
	}

	public void setCclob(String cclob) {
		this.cclob = cclob;
	}

	public Document getCdocument1() {
		return cdocument1;
	}

	public void setCdocument1(Document cdocument1) {
		this.cdocument1 = cdocument1;
	}
	
	public Document getCdocument2() {
		return cdocument2;
	}

	public void setCdocument2(Document cdocument2) {
		this.cdocument2 = cdocument2;
	}
}
