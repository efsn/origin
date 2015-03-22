package org.blue.sys.vo;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.svip.db.annotation.meta.Column;
import org.svip.db.annotation.meta.Constraint;
import org.svip.db.annotation.meta.Foreign;
import org.svip.db.annotation.meta.Index;
import org.svip.db.annotation.meta.Table;
import org.svip.db.enumeration.mysql.DbType;

@Table(index = {@Index(name = "checkId_idx", column = "checkId")})
public class CheckEssay{

    @Column(type = DbType.INT,
            length = 11,
            constraint = @Constraint(primary = true, autoIncrement = true))
    private int checkId;

    @Column(type = DbType.INT, length = 11,
            foreign = @Foreign(name = "foreign_essayId", refColumn = "essayId", refTable = "Essay"))
    private int essayId;

    @Column(type = DbType.VARCHAR, length = 50)
    private String expertName;

    @Column(type = DbType.TEXT)
    private String checkContent;

    //@Column(type = DbType.VARCHAR, length = 14)//YYYYMMDD:HHMMSS
    @Column(type = DbType.DATETIME)
    private Timestamp checkDate = new Timestamp(System.currentTimeMillis());

    public void setStatementValue(PreparedStatement pstm) throws SQLException {
        pstm.setInt(1, essayId);
        pstm.setString(2, expertName);
        pstm.setString(3, checkContent);
        pstm.setTimestamp(4, checkDate);
    }

    public int getCheckId() {
        return checkId;
    }

    public void setCheckId(int checkId) {
        this.checkId = checkId;
    }

    public int getEssayId() {
        return essayId;
    }

    public void setEssayId(int essayId) {
        this.essayId = essayId;
    }

    public String getExpertName() {
        return expertName;
    }

    public void setExpertName(String expertName) {
        this.expertName = expertName;
    }

    public String getCheckContent() {
        return checkContent;
    }

    public void setCheckContent(String checkContent) {
        this.checkContent = checkContent;
    }

    public Timestamp getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(Timestamp checkDate) {
        this.checkDate = checkDate;
    }

}
