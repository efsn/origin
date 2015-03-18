package com.esen.vfs2;

import com.esen.jdbc.ibatis.IbatisConfCreate;

public class VfsIbatisConf extends IbatisConfCreate {
  private static String[] CLASSFIELDS = { "parentDir", "fileName", "isFile", "createTime", "lastModifyTime", "owner",
      "mender", "charset", "mimeType", "size", "content" };

  private static String[] DBFIELDS = { "PARENTDIR_", "FILENAME_", "ISFILE_", "CREATETIME_", "MODIFYTIME_", "OWNER_",
      "MENDER_", "CHARSET_", "MIMETYPE_", "SIZE_", "CONTENT_" };

  public VfsIbatisConf() {
    super(CLASSFIELDS, DBFIELDS, "${TABLENAME}");
  }

  public static void main(String[] args) {
    VfsIbatisConf t = new VfsIbatisConf();
    t.create();
  }
}
