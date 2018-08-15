package im.r_c.android.commonrecyclerviewadapter;

import java.lang.reflect.Method;

/**
 * CommonAdapter
 * Created by richard on 16/1/8.
 */
public class ReflectHelper {
    @SuppressWarnings("unchecked")
    public static void invokeMethodIfExists(String methodName, Object target, Class<?>[] parameterTypes, Object[] parameters) {
        Class c = target.getClass();
        try {
            Method method = c.getMethod(methodName, parameterTypes);
            method.invoke(target, parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
