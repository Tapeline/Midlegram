package midp.tapeline.midlegram;

public class ArrayUtils {

    public static final void printArray(byte[] arr) {
        System.out.print("byte[" + arr.length + "]{");
        for (int i = 0; i < arr.length; i++)
            System.out.print(arr[i] + ",");
        System.out.println("}");
    }

}
