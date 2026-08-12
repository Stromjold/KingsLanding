public class InspectItemProps {
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("net.minecraft.world.item.Item$Properties");
        java.lang.reflect.Method[] m = c.getDeclaredMethods();
        for (java.lang.reflect.Method mm : m) {
            System.out.println(mm.toString());
        }
        java.lang.reflect.Field[] f = c.getDeclaredFields();
        for (java.lang.reflect.Field ff : f) {
            System.out.println("FIELD: " + ff.toString());
        }
    }
}
