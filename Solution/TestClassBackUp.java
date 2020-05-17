package Solution;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TestClassBackUp {
    private boolean flag_backup;
    private Object object_backup;

    public TestClassBackUp(Class<?> testClass) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        flag_backup=false;
        try {
            Constructor<?> cons = testClass.getDeclaredConstructor();
            cons.setAccessible(true);
            object_backup = cons.newInstance();
        } catch (Exception e) {
            throw e;
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

    public void backUpObject(Object object) throws Exception {
        flag_backup = true;
        //Field[] fields = object.getClass().getFields();
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            if (f.get(object)==null) {
                f.set(object_backup, null);
            } else {
                //do shallow copy
                f.set(object_backup, f.get(object));

                //try call copy constructor
                try {
                    tryCopyConst(f,object);
                }catch (Exception e){
                    tryClone(f,object);
                }

                //try clone
                try {
                    tryClone(f, object);
                } catch (Exception e){
                    throw e;
                }

            }
        }
    }

    private void tryClone(Field f, Object object) throws Exception {
        for (Class<?> inter : f.get(object).getClass().getInterfaces()) {
            if (inter.equals(Cloneable.class)) {        //if object cloneable
                Method clone_method = f.get(object).getClass().getDeclaredMethod("clone");
                clone_method.setAccessible(true);
                f.set(object_backup, clone_method.invoke(f.get(object)));
            }
        }
    }

    private void tryCopyConst(Field f, Object object) throws Exception{
        Constructor<?> cons = null;
        cons = f.get(object).getClass().getDeclaredConstructor(f.getClass());
        cons.setAccessible(true);
        f.set(object_backup, cons.newInstance(f));
    }
}
