package midp.tapeline.midlegram;

import java.util.Vector;

public final class ArrayUtils {

    public static void printArray(byte[] arr) {
        System.out.print("byte[" + arr.length + "]{");
        for (int i = 0; i < arr.length; i++)
            System.out.print(arr[i] + ",");
        System.out.println("}");
    }

    public static Vector vectorOf(Object[] arr) {
        Vector v = new Vector();
        for (int i = 0; i < arr.length; i++)
            v.addElement(arr[i]);
        return v;
    }

    public static Vector vectorOfLongs(long[] arr) {
        Vector v = new Vector();
        for (int i = 0; i < arr.length; i++)
            v.addElement(new Long(arr[i]));
        return v;
    }

}
