package org.blue.sys.vo;

import org.svip.db.anno.meta.Column;
import org.svip.db.anno.meta.Constraint;
import org.svip.db.anno.meta.Index;
import org.svip.db.anno.meta.Table;
import org.svip.db.enumeration.mysql.DbType;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@Table(index = {@Index(name = "editorIdIdx", column = "editorId")})
public class Editor{

    @Column(type = DbType.INT, length = 11,
            constraint = @Constraint(autoIncrement = true, primary = true))
    private int editorId;

    @Column(type = DbType.VARCHAR, length = 20)
    private String editorPname;

    @Column(type = DbType.VARCHAR, length = 20)
    private String editorPwd;

    @Column(type = DbType.VARCHAR, length = 20)
    private String editorName;

    @Column(type = DbType.VARCHAR, length = 11)
    private String editorTelephone;

    @Column(type = DbType.VARCHAR, length = 50)
    private String editorEmail;

    public void setIdStatementValue(PreparedStatement pstm) throws SQLException {
        pstm.setInt(1, editorId);
    }

    public void setStatementValue(PreparedStatement pstm) throws SQLException {
        pstm.setString(1, editorPname);
        pstm.setString(2, editorPwd);
        pstm.setString(3, editorName);
        pstm.setString(4, editorEmail);
        pstm.setString(5, editorTelephone);
    }

    public int getEditorId() {
        return editorId;
    }

    public void setEditorId(int editorId) {
        editorId = editorId;
    }

    public String getEditorPname() {
        return editorPname;
    }

    public void setEditorPname(String editorPname) {
        editorPname = editorPname;
    }

    public String getEditorPwd() {
        return editorPwd;
    }

    public void setEditorPwd(String editorPwd) {
        editorPwd = editorPwd;
    }

    public String getEditorName() {
        return editorName;
    }

    public void setEditorName(String editorName) {
        editorName = editorName;
    }

    public String getEditorTelephone() {
        return editorTelephone;
    }

    public void setEditorTelephone(String editorTelephone) {
        editorTelephone = editorTelephone;
    }

    public String getEditorEmail() {
        return editorEmail;
    }

    public void setEditorEmail(String editorEmail) {
        editorEmail = editorEmail;
    }

}
