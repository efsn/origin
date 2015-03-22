package org.blue.sys.vo;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;

import org.svip.db.annotation.meta.Column;
import org.svip.db.annotation.meta.Constraint;
import org.svip.db.annotation.meta.Index;
import org.svip.db.annotation.meta.Table;
import org.svip.db.enumeration.mysql.DbType;

@Table(index = {@Index(name = "typeIdIdx", column = {"typeId"})})
public class EssayType{

    @Column(type = DbType.INT, length = 11, constraint = @Constraint(primary = true, autoIncrement = true))
    private int typeId;

    @Column(type = DbType.VARCHAR, length = 20)
    private String typeName;

    @Column(type = DbType.VARCHAR, length = 20)
    private String deadline;

    public EssayType(){
        Date date = new Date();
        int month = date.getMonth();
        date.setMonth(month + 6);
        deadline = DateFormat.getDateInstance().format(date);
    }

    public void setNameStatementValue(PreparedStatement pstm)
            throws SQLException{
        pstm.setString(1, typeName);
        pstm.setString(2, deadline);
    }

    public void setIdStatementValue(PreparedStatement pstm) throws SQLException{
        pstm.setInt(1, typeId);
    }

    public int gettypeId(){
        return typeId;
    }

    public void settypeId(int typeId){
        this.typeId = typeId;
    }

    public String getType_name(){
        return typeName;
    }

    public void setType_name(String typeName){
        this.typeName = typeName;
    }

    public void setDeadline(String deadline){
        this.deadline = deadline;
    }

}
