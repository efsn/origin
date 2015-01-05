package com.blue.sys.vo;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;

public class EssayType{
    public EssayType() {
        Date date = new Date();
        int month = date.getMonth();
        date.setMonth(month + 6);
        deadline = DateFormat.getDateInstance().format(date);
    }

    public void setNameStatementValue(PreparedStatement pstm)
            throws SQLException {
        pstm.setString(1, type_name);
        pstm.setString(2, deadline);
    }

    public void setIdStatementValue(PreparedStatement pstm) throws SQLException {
        pstm.setInt(1, type_id);
    }

    public int getType_id() {
        return type_id;
    }

    public void setType_id(int typeId) {
        type_id = typeId;
    }

    public String getType_name() {
        return type_name;
    }

    public void setType_name(String typeName) {
        type_name = typeName;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    private int type_id;
    private String type_name;
    private String deadline;
}
