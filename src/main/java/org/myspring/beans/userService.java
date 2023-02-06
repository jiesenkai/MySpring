package org.myspring.beans;

public class userService {
    private userDao userDao;

    public void setUserDao(org.myspring.beans.userDao userDao) {
        this.userDao = userDao;
    }

    public void findList(){
        userDao.select();
    }
}
