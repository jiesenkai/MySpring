package com.jiesnekai;

import org.junit.Test;
import org.myspring.beans.userService;
import org.myspringFramework.ApplicationContent;
import org.myspringFramework.ClassXMLContent;

public class test {
    @Test
    public void test1(){
        ApplicationContent applicationContent = new ClassXMLContent("myspring.xml");
//        Class<? extends String> aClass = "String".getClass();
//        System.out.println(aClass);
        userService userService = (org.myspring.beans.userService) applicationContent.getBean("userService");
        userService.findList();
    }

}
