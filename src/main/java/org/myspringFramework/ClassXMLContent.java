package org.myspringFramework;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.XMLReader;

import javax.transaction.xa.XAResource;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 实现容器的初始化与管理
 * @author jack
 * @date 2023/2/7 0:39
 */

public class ClassXMLContent implements ApplicationContent{
    private Logger logger = LoggerFactory.getLogger(ClassXMLContent.class);
    /* 存放bean的容器*/
    private Map<String,Object> singleObject=new HashMap();

    /*  初始化容器 获取XML信息 曝光对象 */
    public ClassXMLContent(String xmlName){
        try{
            // 解析XML文件
            SAXReader xml = new SAXReader();
            // 获取一个输入流 指向xml配置文件
            InputStream in = ClassLoader.getSystemResourceAsStream(xmlName);
            //读取文件
            Document document = xml.read(in);
            // 获取到每个bean标签的信息  初始化bean进行曝光
            List<Node> nodes = document.selectNodes("//bean");
            // 遍历bean标签 初始化bean 进行曝光
            nodes.forEach(node->{
                try{
                    //更方便的获取bean标签中元素的信息
                    Element beanElt = (Element)node;
                    // 获取bean标签中id属性的值
                    String id = beanElt.attributeValue("id");
                    // 获取bean标签中class属性的值
                    String className = beanElt.attributeValue("class");
                    //通过反射到该类
                    Class<?> aClass = Class.forName(className);
                    // 实例化bean
                    Object beanInstance = aClass.getConstructor(null).newInstance(null);
                    // 添加到容器 进行曝光
                    singleObject.put(id,beanInstance);
                    // 添加日志
                    logger.info("初始化"+beanInstance);
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
            // 进行依赖注入 重新获取遍历一次bean标签获取属性
            nodes.forEach(node->{
                try{
                // 进行更快捷的访问node
                Element beanELT =(Element)node;
                // 获取id
                String id = beanELT.attributeValue("id");
                // 获取类名
                String className = beanELT.attributeValue("class");
                //反射机制到该类
                Class<?> aClass = Class.forName(className);
                //  获取该bean下的property标签的属性 为一个集合
                List<Element> propertys = beanELT.elements("property");
                // 遍历property属性
                propertys.forEach(property->{
                    try {
                        //获取name属性值
                        String name = property.attributeValue("name");
                        // 获取value属性值
                        String value = property.attributeValue("value");
                        // 获取ref属性值
                        String ref = property.attributeValue("ref");
                        // bean中属性的TYPE
                        String propertyTypeName = aClass.getDeclaredField(name).getType().getSimpleName();
                        // 获取set方法名
                        String setMethodName = "set"+name.toUpperCase().charAt(0)+name.substring(1);
                        // 获取set方法
                        Method declaredMethod = aClass.getDeclaredMethod(setMethodName, aClass.getDeclaredField(name).getType());
                        // value值的的真实值
                        Object key =null;
                        //判断注入的属性是value还是ref
                        if (value != null) {
                                // 转换为指定数据类型
                                switch (propertyTypeName){
                                    case "String":
                                        key=value;
                                        break;
                                    case "int":
                                        key=Integer.parseInt(value);
                                        break;
                                    case "long":
                                        key = Long.parseLong(value);
                                        break;
                                    case "boolean":
                                        key = Boolean.parseBoolean(value);
                                        break;
                                    case "byte":
                                        key = Byte.parseByte(value);
                                        break;
                                    case "char":
                                        key = value.charAt(0);
                                        break;
                                    case "short":
                                        key = Short.parseShort(value);
                                        break;
                                    case "float":
                                        key = Float.parseFloat(value);
                                        break;
                                    case "double":
                                        key = Double.parseDouble(value);
                                        break;
                                    case "Integer":
                                        key = Integer.valueOf(value);
                                        break;
                                    case "Double":
                                            key = Double.valueOf(value);
                                            break;
                                }
                                // 进行value注入属性
                            declaredMethod.invoke(singleObject.get(id),key);
                        }
                        if (ref !=null){
                            // 进行ref注入属性
                            declaredMethod.invoke(singleObject.get(id),singleObject.get(ref));
                        }


                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });
                }catch(Exception e){
                    e.printStackTrace();
                }


            });
        }catch(Exception e){
            e.printStackTrace();
        }
        logger.info("容器bean:"+singleObject);

    }

    @Override
    public Object getBean(String beanName) {
        return singleObject.get(beanName);
    }
}
