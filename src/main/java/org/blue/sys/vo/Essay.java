package org.blue.sys.vo;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.svip.db.anno.meta.Column;
import org.svip.db.anno.meta.Constraint;
import org.svip.db.anno.meta.Index;
import org.svip.db.anno.meta.Table;
import org.svip.db.enumeration.mysql.DbType;

@Table(index = @Index(name = "essayIdIdx", column = "essayId"))
public class Essay{

    @Column(type = DbType.INT, length = 11,
            constraint = @Constraint(primary = true, autoIncrement = true))
    private int essayId;

    @Column(type = DbType.INT, length = 3)
    private int typeId;

    @Column(type = DbType.VARCHAR, length = 20)
    private String essayName;

    @Column(type = DbType.TEXT)
    private String essayContent1;

    @Column(type = DbType.TEXT)
    private String essayContent2;

    @Column(type = DbType.DATETIME)
    private Timestamp publishTime;

    @Column(type = DbType.VARCHAR, length = 20)
    private String useMark = "not used";

    @Column(type = DbType.VARCHAR, length = 20)
    private String checkMark = "not checked";

    @Column(type = DbType.VARCHAR, length = 20)
    private String authorPname;

    @Column(type = DbType.INT, length = 7)
    private String essayNums;

    private String essayKeywords;
    private String authorInfo;
    private Timestamp registerDate = new Timestamp(System.currentTimeMillis());
    private long publishMoney;
    private String ispay;
    
    public void setStatementValue(PreparedStatement pstm) throws SQLException {
        pstm.setInt(1, typeId);
        pstm.setString(2, essayName);
        pstm.setString(3, essayContent1);
        pstm.setString(4, essayContent2);
        pstm.setTimestamp(5, publishTime);
        pstm.setString(6, useMark);
        pstm.setString(7, checkMark);
        pstm.setString(8, authorPname);
        pstm.setString(9, essayNums);
        pstm.setString(10, essayKeywords);
        pstm.setString(11, authorInfo);
        pstm.setTimestamp(12, registerDate);
    }

    public int getEssayId() {
        return essayId;
    }

    public void setEssayId(int essayId) {
        this.essayId = essayId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getEssayName() {
        return essayName;
    }

    public void setEssayName(String essayName) {
        this.essayName = essayName;
    }

    public String getEssayContent1() {
        return essayContent1;
    }

    public void setEssayContent1(String essayContent1) {
        this.essayContent1 = essayContent1;
    }

    public String getEssayContent2() {
        return essayContent2;
    }

    public void setEssayContent2(String essayContent2) {
        this.essayContent2 = essayContent2;
    }

    public Timestamp getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Timestamp publishTime) {
        this.publishTime = publishTime;
    }

    public String getUseMark() {
        return useMark;
    }

    public void setUseMark(String useMark) {
        this.useMark = useMark;
    }

    public String getCheckMark() {
        return checkMark;
    }

    public void setCheckMark(String checkMark) {
        this.checkMark = checkMark;
    }

    public String getAuthorPname() {
        return authorPname;
    }

    public void setAuthorPname(String authorPname) {
        this.authorPname = authorPname;
    }

    public String getEssayNums() {
        return essayNums;
    }

    public void setEssayNums(String essayNums) {
        this.essayNums = essayNums;
    }

    public String getEssayKeywords() {
        return essayKeywords;
    }

    public void setEssayKeywords(String essayKeywords) {
        this.essayKeywords = essayKeywords;
    }

    public String getAuthorInfo() {
        return authorInfo;
    }

    public void setAuthorInfo(String authorInfo) {
        this.authorInfo = authorInfo;
    }

    public Timestamp getRegisterDate() {
        return registerDate;
    }

    public long getPublishMoney() {
        return publishMoney;
    }

    public void setPublishMoney(long publishMoney) {
        this.publishMoney = publishMoney;
    }

    public String getIsPublished() {
        return ispay;
    }

    public void setIsPublished(String isPublished) {
        this.ispay = isPublished;
    }

}
