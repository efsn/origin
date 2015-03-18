

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ReadTxt {
  private void printData(String filepath) throws Exception {
    File f = new File(filepath);
    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
    try {
      String ln = reader.readLine();
      int k=0;
      while (ln != null) {
        if(k>50) break;
        System.out.println(ln);
        ln = reader.readLine();
        k++;
      }
    }
    finally {
      reader.close();
    }
  }
  private void createData(String fromdir,String todir) throws Exception{
    File[] fls = listFiles(fromdir, null, LISTFILE_OPTION_RECUR);
    File telDataFile = new File(todir + "test0.txt");
    OutputStream out = new FileOutputStream(telDataFile);
    System.out.println("start................");
    try {
      long n=0;
      int k=0;
     // boolean flag = false;
      for(int i=0;i<fls.length;i++){
       // if(flag) break;
        File f = fls[i];
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
        try {
          String ln = reader.readLine();
          while (ln != null) {
            /*if(n==100){
              flag = true;
              break;
            }*/
            if (n > 0 && n % 20000000 == 0) {
              out.close();
              k++;
              out = new FileOutputStream(new File(todir + "test" + k + ".txt"));
            }
            String str = ln.substring(0,78);
            out.write(str.getBytes());
            out.write('\r');
            out.write('\n');
            n++;
            ln = reader.readLine();
          }
        }
        finally {
          reader.close();
        }
        System.out.println(i+"\t"+f.getName()+"\t"+n);
      }
      System.out.println("finish...........");
    }finally{
      out.close();
    }
  }
  public static void main(String[] args) throws Exception{
    ReadTxt rt = new ReadTxt();
    //rt.printData("\\\\192.168.3.57\\sybase\\realdb\\0706txt\\070605_1i.txt");
    rt.createData("/home/sybase/realdb/0706txt/", "/home/sybase/newdb/0706txt/");
  }
  public static final int LISTFILE_OPTION_RECUR = 0X1;//是否递归遍历子目录
  public static final int LISTFILE_OPTION_INCLUDEDIR = 0X2;//是否遍历目录名
  public static final int LISTFILE_OPTION_EXCLUDEFILE = 0X4;//是否不遍历文件

  public static  File[] listFiles(String path, String filter, int option){
    if(path==null||path.length()==0) return null;
    boolean rec = LISTFILE_OPTION_RECUR == (option & LISTFILE_OPTION_RECUR);
    boolean includeDir = LISTFILE_OPTION_INCLUDEDIR == (option & LISTFILE_OPTION_INCLUDEDIR);
    boolean excludeFile = LISTFILE_OPTION_EXCLUDEFILE == (option & LISTFILE_OPTION_EXCLUDEFILE);
    File dir = new File(path);
    Pattern p = null;
    if (filter != null) {
      p = Pattern.compile(filter);
    }
    List l = new ArrayList();
    listFilesRec(l,dir,p,rec,includeDir,excludeFile);
    File[] result = new File[l.size()];
    l.toArray(result);
    return result;
  }
  private static void listFilesRec(List l,File dir,Pattern p,boolean rec,boolean includeDir,boolean excludeFile){
    File[] fs = dir.listFiles();
    if(fs==null) return;
    for(int i=0;i<fs.length;i++){
      File f = fs[i];
      //遍历文件
      if(f.isFile()&&!excludeFile){
        if(p==null || p.matcher(f.getName()).find())
          l.add(f);
      }
      //遍历目录
      if(f.isDirectory()){
        if(includeDir){
          if(p==null || p.matcher(f.getName()).find())
            l.add(f);
        }
        if(rec)
          listFilesRec(l,f,p,rec,includeDir,excludeFile);
      }
    }
  }
}
