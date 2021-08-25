package learn.csv;

/**
 * @Author: wenhongliang
 */
public class Staff {
    private String id;
    private String fullName;
    private String email;
    private String phone;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String toRow() {
        return String.format("%s,%s,%s,%s", this.id, this.fullName, this.email, this.phone);
    }
}
