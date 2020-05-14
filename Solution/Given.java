package Solution;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)         //the annotation Given can be use on method
@Retention(RetentionPolicy.RUNTIME) //the annotation Given "lives" = runtime

public @interface Given {
//default value?
    String[] value();
}
