package Solution;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)         //the annotation When can be use on method
@Retention(RetentionPolicy.RUNTIME) //the annotation When "lives" = runtime
//amit
public @interface When {

    String[] value();
}
