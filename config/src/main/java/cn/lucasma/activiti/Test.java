package cn.lucasma.activiti;

import java.util.Arrays;
import java.util.List;

/**
 * @Date 2019/11/1 22:55
 * @Author chq
 */
public class Test {
    public static void main(String[] args) {
        Dept dept = new Dept();
        dept.setUserList(Arrays.asList(new User("aa")));
        for (Object o : dept.getUserList()) {
            System.out.println(o.toString());
        }
    }
}

class Dept {
    private List<User> userList;

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }
}

class User {
    private String name;

    public User() {
    }

    public User(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
