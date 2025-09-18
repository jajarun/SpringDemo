// 定义一个简单的注解
public @interface MyCustomAnnotation {
    // 注解的属性（看起来像方法，但实际上是属性）
    String value() default "默认值";
    int count() default 1;
    String[] tags() default {};
}

// 使用注解的示例类
class ExampleClass {
    
    // 直接使用注解 - 这就是注解的用途！
    @MyCustomAnnotation
    public void method1() {
        System.out.println("method1");
    }
    
    // 带参数使用注解
    @MyCustomAnnotation(value = "自定义值", count = 5)
    public void method2() {
        System.out.println("method2");
    }
    
    // 使用数组参数
    @MyCustomAnnotation(tags = {"标签1", "标签2"})
    public void method3() {
        System.out.println("method3");
    }
}

// 演示如何读取注解信息
class AnnotationReader {
    public static void readAnnotations() throws Exception {
        Class<?> clazz = ExampleClass.class;
        
        // 获取所有方法
        for (java.lang.reflect.Method method : clazz.getDeclaredMethods()) {
            // 检查是否有我们的注解
            if (method.isAnnotationPresent(MyCustomAnnotation.class)) {
                MyCustomAnnotation annotation = method.getAnnotation(MyCustomAnnotation.class);
                
                System.out.println("方法: " + method.getName());
                System.out.println("  value: " + annotation.value());
                System.out.println("  count: " + annotation.count());
                System.out.println("  tags: " + java.util.Arrays.toString(annotation.tags()));
                System.out.println();
            }
        }
    }
    
    public static void main(String[] args) throws Exception {
        readAnnotations();
    }
} 