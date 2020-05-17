package Solution;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TestClassBackUp {
    private boolean flag_backup;
    private Object object_backup;

    public TestClassBackUp(Class<?> testClass) {
        flag_backup=false;
        try {
            Constructor<?> cons = testClass.getDeclaredConstructor();
            cons.setAccessible(true);
            object_backup = cons.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }


    public boolean isFlag_backup() {
        return flag_backup;
    }

    public void setFlag_backup(boolean flag_backup) {
        this.flag_backup = flag_backup;
    }

    public Object getObject_backup() {
        return object_backup;
    }

    public void setObject_backup(Object object_backup) {
        this.object_backup = object_backup;
    }
//    public void backUpObject(Object object) throws IllegalAccessException {
//        flag_backup=true;
//        //Field[] fields = object.getClass().getFields();
//        Field[] fields = object.getClass().getDeclaredFields();
//        for (Field f : fields) {
//            f.setAccessible(true);
//            if (f == null) {
//                f.set(object_backup, null);
//            } else {
//                for (Class<?> inter : f.getClass().getInterfaces()) {
//                    if (inter.equals(Cloneable.class)) {
//                        try {
//                            Method clone_method = f.getClass().getDeclaredMethod("clone");
//                            clone_method.setAccessible(true);
//                            f.set(object_backup, clone_method.invoke(f));
//                        } catch (NoSuchMethodException e) {
//                            try {
//                                Constructor<?> cons = f.getClass().getDeclaredConstructor(f.getClass());
//                                cons.setAccessible(true);
//                                try {
//                                    f.set(object_backup, cons.newInstance(f));
//                                } catch (InstantiationException instantiationException) {
//                                    f.set(object_backup, f.get(object));
//                                } catch (InvocationTargetException invocationTargetException) {
//                                    invocationTargetException.printStackTrace();
//                                }
//                            } catch (NoSuchMethodException noSuchMethodException) {
//                                noSuchMethodException.printStackTrace();
//                            }
//                        } catch (InvocationTargetException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//                try {
//                    Constructor<?> cons = null;
//                    cons = f.getClass().getDeclaredConstructor(f.getClass());
//                    cons.setAccessible(true);
//                    f.set(object_backup, cons.newInstance(f));
//                } catch (NoSuchMethodException e) {
//                    f.set(object_backup, f.get(object));
//                } catch (InstantiationException e) {
//                    e.printStackTrace();
//                } catch (InvocationTargetException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//



    public void backUpObject(Object object) throws IllegalAccessException {
        flag_backup=true;
        //Field[] fields = object.getClass().getFields();
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            if (f == null) {
                f.set(object_backup, null);
            } else {
                for (Class<?> inter : f.getClass().getInterfaces()) {
                    if (inter.equals(Cloneable.class)) {        //if object cloneable
                        try {
                            Method clone_method = f.getClass().getDeclaredMethod("clone");
                            clone_method.setAccessible(true);
                            f.set(object_backup, clone_method.invoke(f));
                        } catch (Exception e) {   // if we have exception call copy constructor
                            try {
                                Constructor<?> cons = f.getClass().getDeclaredConstructor(f.getClass());
                                cons.setAccessible(true);
                                f.set(object_backup, cons.newInstance(f));
                            } catch (Exception a) {               //if we have exception shllw copt
                                f.set(object_backup, f.get(object));
                            }
                        }
                    }
                }
                try {      //try copy constructor
                    Constructor<?> cons = null;
                    cons = f.getClass().getDeclaredConstructor(f.getClass());
                    cons.setAccessible(true);
                    f.set(object_backup, cons.newInstance(f));
                } catch (Exception e) {             //if we have exception shllw copy
                    f.set(object_backup, f.get(object));
                }
            }
        }
    }



}
