package com.esen.jdbc;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

import com.esen.util.StrFunc;

public class CreateDbData {
  /**
   * @param args
   * @throws Exception 
   */
  private static final String NULL_NUMBER = "00C                   ";

  private static final String CALL_TEL_NULL_11 = "           ";

  private static final String LIS_TEL_NULL_9 = "         ";

  private static final String TEL_INFOR_CONST = "08000";

  private static final String OTHER_CONST = "000117000019000010     001    WBTHN7OTSH4DI 02700        0      0000001  AA\r\n";

  public static void createTelData(String filePath, long recordCount, long stepcount) {
    if (stepcount <= 0) {
      if (recordCount > 30000000)
        stepcount = 30000000;
      else
        stepcount = recordCount;
    }
    System.out.println("开始创建数据......");
    long start = System.currentTimeMillis();
    File telDataFile = new File(filePath + "\\test0.txt");
    Calendar calendar = Calendar.getInstance();
    calendar.set(2002, 0, 1, 1, 0, 0);
    try {
      OutputStream out = new FileOutputStream(telDataFile);
      try {
        int k = 0;
        for (int i = 0; i < recordCount; i++) {
          if (i > 0 && i % stepcount == 0) {
            out.close();
            k++;
            out = new FileOutputStream(new File(filePath + "\\test" + k + ".txt"));
          }
          StringBuffer aRowData = new StringBuffer();
          addTelNumber(aRowData, true);
          addTelNumber(aRowData, false);
          addTelInfor(aRowData, calendar);
          out.write(aRowData.toString().getBytes());
          if (i != 0 && i % 100000 == 0)
            System.out.println("成功写入" + i / 10000 + "万行数据," + (i * 10000 / recordCount) / 100.00 + "%");
        }
        long end = System.currentTimeMillis();
        System.out.println("创建完成,共" + recordCount + "条数据，用时:" + (end - start) / 1000 + "秒");
      }
      finally {
        if (out != null)
          out.close();
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private static void addTelNumber(StringBuffer aRowData, boolean isCall) {
    Random random = new Random();
    int type = random.nextInt(isCall ? 3 : 2);
    switch (type) {
      //电话
      case 0: {
        aRowData.append(0);
        aRowData.append(random.nextInt(89) + 10);
        aRowData.append(random.nextInt(5000) + 1000);
        aRowData.append("0000");
        aRowData.append(isCall ? CALL_TEL_NULL_11 : LIS_TEL_NULL_9);
        break;
      }
        //手机
      case 1: {
        aRowData.append(13);
        aRowData.append(random.nextInt(500000) + 100000);
        aRowData.append("000");
        aRowData.append(isCall ? CALL_TEL_NULL_11 : LIS_TEL_NULL_9);
        break;
      }
        //主叫为空
      case 2: {
        aRowData.append(NULL_NUMBER);
        break;
      }
    }
  }

  private static void addTelInfor(StringBuffer aRowData, Calendar calendar) {
    aRowData.append(TEL_INFOR_CONST);
    calendar.add(Calendar.SECOND, 5);
    String year = String.valueOf(calendar.get(Calendar.YEAR)).substring(2);
    int monthValue = calendar.get(Calendar.MONTH) + 1;
    String month = getValue(monthValue);
    int dayOfMonthValue = calendar.get(Calendar.DAY_OF_MONTH);
    String dayOfMonth = getValue(dayOfMonthValue);
    int hourValue = calendar.get(Calendar.HOUR_OF_DAY);
    String hour = getValue(hourValue);
    int minuteValue = calendar.get(Calendar.MINUTE);
    String minute = getValue(minuteValue);
    int secondValue = calendar.get(Calendar.SECOND);
    String second = getValue(secondValue);
    aRowData.append(year);
    aRowData.append(month);
    aRowData.append(dayOfMonth);
    aRowData.append(hour);
    aRowData.append(minute);
    aRowData.append(second);
    aRowData.append(OTHER_CONST);
  }

  private static String getValue(int value) {
    return value < 10 ? "0" + String.valueOf(value) : String.valueOf(value);
  }

  public static void main(String[] args) throws Exception {
    
    //createTelData("E:\\db\\iq",100000000,-1);
     CreateDbData db = new CreateDbData();
     //db.initdata();
//     String[][] strarr = db.getAriData(20000);
//     parseLine(strarr);
    //db.createAriData(20000);
    // db.createAriData(10000000);
    //db.createVisitorlodgData(1000000);
    db.createCallInfoData();
  }

  private List psonlist;//人名
  private List psonlist2;//人名

  private HashMap psonmap;//人的属性：国籍，出生日期

  private List aircodes;//航空公司代码

  private List stations;//航空起始站点

  private List hotels;//入住酒店

  private List nations;//国籍

  private HashMap psmap;//人属性，城市权重

  private HashMap pscitymap = new HashMap();//城市map，记录人的上次降落地点和时间

  private HashMap pshome = new HashMap();

  private void initdata() throws Exception {
    psonlist = getData("e:\\db\\iq\\三国人名.txt", 0);
    fillRandomData(psonlist, 100000);//用于航空
    psonlist2 = getData("e:\\db\\iq\\三国人名.txt", 0);
    fillRandomData(psonlist2, 10000);//用于酒店
    aircodes = getData("e:\\db\\iq\\国内航空公司代码.txt", 2);
    stations = getData("e:\\db\\iq\\省会.txt", 2);
    hotels = getData("e:\\db\\iq\\广州酒店.txt", 1);
    nations = getData("e:\\db\\iq\\国籍.txt", 0);
    //准备权重数据
    psmap = getPsMap(psonlist, stations);
    psonmap = new HashMap();
    for (int i = 0; i < psonlist.size(); i++) {
      String pn = (String) psonlist.get(i);
      Calendar cal = StrFunc.str2date("19500101", "yyyymmdd");
      cal = getOffsetCalendar(cal, (int) Math.round(365 * 35 * Math.random()));
      String dayofbirth = StrFunc.date2str(cal, "yyyymmdd");
      String nation = getRandomValue(nations);
      String[] pro = new String[] { dayofbirth, nation };
      psonmap.put(pn, pro);
    }
  }

  private void createCallInfoData() throws Exception{
    
    File f = new File("d:\\dw\\db\\callinfodata.txt");
    long start = System.currentTimeMillis();
    OutputStream out = new FileOutputStream(f);
    Class.forName("com.sybase.jdbc3.jdbc.SybDriver");
    Connection conn = DriverManager.getConnection("jdbc:sybase:Tds:192.168.3.57:2638/irpt5?charset=cp936"
        , "demo", "demo");
    Connection conn2 = DriverManager.getConnection("jdbc:sybase:Tds:192.168.3.57:2638/irpt5?charset=cp936"
        , "demo", "demo");
    int i=0,k=0;
    try{
      Statement stat = conn.createStatement();
      ResultSet rs = stat.executeQuery("select callnumber from callinfo");
      Statement stat2 = conn2.createStatement();
      ResultSet rs2 = stat2.executeQuery("select cname from pnameinfo");
      while(rs.next()){
        rs2.next();
        String callnum = rs.getString(1);
        i++;
        StringBuffer rowstr = new StringBuffer();
        rowstr.append(callnum);
        rowstr.append(",\"");
        String cname = rs2.getString(1);
        
        rowstr.append(cname).append("\"\r\n");
        out.write(rowstr.toString().getBytes());
        //if(i==100) break;
        if (i != 0 && i % 100000 == 0)
          System.out.println("成功写入" + i / 10000 + "万行数据;");
      }
      long end = System.currentTimeMillis();
      System.out.println("创建完成,共"+i+"条数据，用时:"+(end-start)/1000+"秒");
    }finally{
      conn.close();
      conn2.close();
      out.close();
    }
  }

  private void createVisitorlodgData(int recordCount) throws Exception {
    File f = new File("e:\\db\\iq\\lodgdata.txt");
    OutputStream out = new FileOutputStream(f);
    try {
      long start = System.currentTimeMillis();
      for (int i = 0; i < recordCount; i++) {
        StringBuffer rowstr = new StringBuffer();
        rowstr.append(getVisitorid(i, recordCount)).append(",");
        String cname = getRandomValue(psonlist2);
        String spellname = StrFunc.cnToSpell(cname);
        String sn[] = spellname.split(" ");
        rowstr.append(getFirstName(sn)).append(",");
        rowstr.append(sn[0]).append(",");
        rowstr.append(cname).append(",");
        rowstr.append("").append(",");
        String[] prop = (String[]) psonmap.get(cname);
        rowstr.append(prop[0]).append(",");
        rowstr.append(prop[1]).append(",");
        rowstr.append(getRandomValue(hotels, null, 10)).append(",");
        Calendar cal = StrFunc.str2date("20040101", "yyyymmdd");
        cal = getOffsetCalendar(cal, (int) Math.round(365 * 4 * Math.random()));
        String checkindate = StrFunc.date2str(cal, "yyyymmdd");
        rowstr.append(checkindate).append("\r\n");
        out.write(rowstr.toString().getBytes());
        if (i != 0 && i % 100000 == 0)
          System.out.println("成功写入" + i / 10000 + "万行数据;");
      }
      long end = System.currentTimeMillis();
      System.out.println("创建完成,共" + recordCount + "条数据，用时:" + (end - start) / 1000 + "秒");
    }
    finally {
      out.close();
    }

  }

  private String getFirstName(String[] sn) {
    StringBuffer fn = new StringBuffer();
    if (sn.length <= 1)
      return "";
    for (int i = 1; i < sn.length; i++) {
      if (fn.length() > 0)
        fn.append(" ");
      fn.append(sn[i]);
    }
    return fn.toString();
  }

  private String getVisitorid(int i, int recordCount) {
    StringBuffer id = new StringBuffer();
    int len = String.valueOf(recordCount).length();
    String s = String.valueOf(i + 1);
    int l = len - s.length();
    if (l > 0) {
      for (int j = 0; j < l; j++)
        id.append(0);
    }
    id.append(s);
    return id.toString();
  }

  private void createAriData(int recordCount) throws Exception {
    File f = new File("e:\\db\\iq\\airdata.txt");
    OutputStream out = new FileOutputStream(f);
    try {
      long start = System.currentTimeMillis();
      for (int i = 0; i < recordCount; i++) {
        String cname = getRandomValue(psonlist);

        StringBuffer rowstr = new StringBuffer();
        String startstation;
        if (pscitymap.containsKey(cname))
          startstation = ((String[]) pscitymap.get(cname))[0];
        else if (Math.random() > 0.2)
          startstation = getRandomValue((List) psmap.get(cname));
        else
          startstation = getRandomValue(stations);

        String deststation;
        String home = (String) pshome.get(cname);
        if (startstation != home && Math.random() > 0.6)
          deststation = home;
        else if (Math.random() > 0.2)
          deststation = getRandomValue((List) psmap.get(cname), startstation, 0);
        else
          deststation = getRandomValue(stations, startstation, 0);

        //String deststation = getRandomValue(stations, startstation,0);
        String aircode = getRandomValue(aircodes);
        String flightNo = String.valueOf((int) Math.round(8999 * Math.random()) + 1000);
        Calendar cal = StrFunc.str2date("20040101", "yyyymmdd");
        if (startstation.equals(home) || !pscitymap.containsKey(cname))
          cal = getOffsetCalendar(cal, (int) Math.round(365 * 4 * Math.random()));
        else {
          cal = StrFunc.str2date(((String[]) pscitymap.get(cname))[1], "yyyymmdd");
          int time = (int) Math.round(Math.random() * 4);
          time = time > 0 ? time : 1;
          cal = getOffsetCalendar(cal, time);
        }

        String takeoffdate = StrFunc.date2str(cal, "yyyymmdd");
        String timestr = getRandomTime();
        String takeofftime = takeoffdate + timestr;
        cal.add(Calendar.DATE, -(int) Math.round(7 * Math.random()));
        String bookingdate = StrFunc.date2str(cal, "yyyymmdd");
        String bookingcomp = String.valueOf((int) Math.round(20 * Math.random()) + 1);
        String ename = StrFunc.cnToSpell(cname);

        rowstr.append(startstation).append(",");
        rowstr.append(deststation).append(",");
        rowstr.append(aircode).append(",");
        rowstr.append(flightNo).append(",");
        rowstr.append(takeoffdate).append(",");
        rowstr.append(takeofftime).append(",");
        rowstr.append(ename).append(",");
        rowstr.append(bookingdate).append(",");
        rowstr.append(bookingcomp).append(",");
        rowstr.append(cname).append("\r\n");

        //将目的地和起飞时间放入map
        String[] strs = new String[2];
        strs[0] = deststation;
        strs[1] = takeoffdate;
        pscitymap.put(cname, strs);

        out.write(rowstr.toString().getBytes());
        if (i != 0 && i % 100000 == 0)
          System.out.println("成功写入" + i / 10000 + "万行数据;");
      }
      long end = System.currentTimeMillis();
      System.out.println("创建完成,共" + recordCount + "条数据，用时:" + (end - start) / 1000 + "秒");
    }
    finally {
      out.close();
    }
  }

  private String getRandomTime() {
    int h = (int) Math.round(23 * Math.random());
    int mm[] = { 10, 20, 30, 40, 50 };
    int m = mm[(int) Math.round(4 * Math.random())];
    StringBuffer ss = new StringBuffer(4);
    if (h < 10)
      ss.append(0);
    ss.append(h).append(m);
    return ss.toString();
  }

  public Calendar getOffsetCalendar(Calendar cal, int offset) {
    cal.add(Calendar.DATE, offset);
    return cal;
  }

  private String getRandomValue(List l) {
    return getRandomValue(l, null, 0);
  }

  private String getRandomValue(List l, String v, int p) {
    int len = l.size() - 1;
    int i = (int) Math.round(Math.random() * len);
    int n = i;
    if (i > p)
      n = i - p;
    if (n > p / 2)
      n = n - p / 2;
    String vi = (String) l.get(n);
    if (v != null && v.equals(vi))
      return getRandomValue(l, v, p);
    return vi;
  }

  private List getData(String filepath, int i) throws Exception {
    List rs = new ArrayList();
    File f = new File(filepath);
    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
    try {
      String ln = reader.readLine();
      while (ln != null) {
        String ss[] = ln.split("\\t");
        rs.add(ss[i]);
        ln = reader.readLine();
      }
    }
    finally {
      reader.close();
    }
    return rs;
  }

  /**
   * 创建每个用户的城市权重
   * 每个人有一个居住城市 25%概率在这个城市起飞或降落
   * 其它有5-10个经常居住的城市
   * 用户有80%的概率在这几个城市飞
   * @param pslist
   * @param stlist
   * @return
   */
  private HashMap getPsMap(List pslist, List stlist) {
    HashMap map = new HashMap();
    for (int i = 0; i < pslist.size(); i++) {
      List l = new ArrayList();
      //居住地
      String home = getRandomValue(stlist);

      //每人有5-10个权重城市
      long count = Math.round(Math.random() * 10);
      count = count < 5 ? 5 : count;

      List cities = new ArrayList();
      for (int j = 0; j < count; j++) {
        String city = getRandomValue(stlist);
        while (cities.indexOf(city) != -1) {
          city = getRandomValue(stlist);
        }
        cities.add(city);
      }

      for (int n = 0; n < cities.size(); n++) {
        int c1 = Integer.parseInt(String.valueOf(Math.round(Math.random() * 10)));
        fillData(l, cities.get(n).toString(), c1);
      }
      fillData(l, home, Math.round((l.size() / 3)));
      map.put(pslist.get(i), l);
      pshome.put(pslist.get(i), home);
    }
    return map;
  }

  private void fillData(List l, String content, int count) {
    for (int i = 0; i < count; i++) {
      l.add(content);
    }
  }

  private void fillRandomData(List list, int count) {
    for (int i = 0; i < count; i++) {
      list.add(String.valueOf(Math.round(Math.random() * 1000)));
    }
  }
}
