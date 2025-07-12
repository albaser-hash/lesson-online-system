package com.lesson.context;

public class BaseContext {

    public static ThreadLocal<Integer> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Integer id) {
        threadLocal.set(id);
    }//存

    public static Integer getCurrentId() {
        return threadLocal.get();
    }//取

    public static void removeCurrentId() {
        threadLocal.remove();
    }//删

}
