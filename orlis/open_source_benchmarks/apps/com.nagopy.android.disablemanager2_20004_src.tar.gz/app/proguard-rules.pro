-dontskipnonpubliclibraryclasses
-optimizationpasses 3
-flattenpackagehierarchy
-keepattributes SourceFile,LineNumberTable
-printmapping map.txt

-assumenosideeffects public class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
    public static *** wtf(...);
}
-assumenosideeffects class com.nagopy.android.disablemanager2.support.DebugUtil {
    <methods>;
}
