package app.saikat.ThreadManagement.interfaces;

public class TaskProviderHelper {

    public static void clearParent(TaskProvider<?, ?> taskProvider) {
        taskProvider.getWeakParent().clear();;
    }

    public static <T> T getParent(TaskProvider<T, ?> taskProvider) {
        return taskProvider.getWeakParent().get();
    }
    
}