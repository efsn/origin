package template.bean;

import org.svip.db.anno.meta.Column;
import org.svip.db.anno.meta.Constraint;
import org.svip.db.anno.meta.Index;
import org.svip.db.anno.meta.Table;
import org.svip.db.enumeration.mysql.DbType;

import java.util.Date;

@Table(index = {@Index(name = "usernameIdx", column = "username")})
public class User{

    @Column(type = DbType.INT,
            length = 11,
            constraint = @Constraint(primary = true, autoIncrement = true))
    private int id;

    @Column(type = DbType.VARCHAR, length = 20)
    private String username;

    @Column(type = DbType.VARCHAR, length = 20)
    private String password;

    @Column(type = DbType.CHAR,
            length = 11,
            constraint = @Constraint(nullAble = false))
    private String mobilePhone;

    @Column(type = DbType.VARCHAR, length = 60)
    private String email;

    @Column(type = DbType.DATETIME, constraint = @Constraint(nullAble = false))
    private Date createDate;

    @Column(type = DbType.DATETIME, constraint = @Constraint(nullAble = false))
    private Date updateDate;

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getUsername(){
        return username;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public String getPassword(){
        return password;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public String getMobilePhone(){
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone){
        this.mobilePhone = mobilePhone;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public Date getCreateDate(){
        return createDate;
    }

    public void setCreateDate(Date createDate){
        this.createDate = createDate;
    }

    public Date getUpdateDate(){
        return updateDate;
    }

    public void setUpdateDate(Date updateDate){
        this.updateDate = updateDate;
    }

}
