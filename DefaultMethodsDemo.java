/**
 * Java Default方法详解和示例
 * Default方法是Java 8引入的重要特性，允许在接口中提供方法的默认实现
 */

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

// ============== 示例1：基本的default方法 ==============
interface Drawable {
    // 抽象方法 - 必须实现
    void draw();
    
    // default方法 - 提供默认实现，实现类可以选择重写或使用默认实现
    default void drawBorder() {
        System.out.println("绘制默认边框：* * * * *");
    }
    
    default void showInfo() {
        System.out.println("这是一个可绘制的对象");
    }
    
    // static方法 - 接口的静态方法
    static void printVersion() {
        System.out.println("Drawable接口版本：1.0");
    }
}

class Circle implements Drawable {
    @Override
    public void draw() {
        System.out.println("绘制圆形 ○");
    }
    
    // 可以选择重写default方法
    @Override
    public void drawBorder() {
        System.out.println("绘制圆形边框：○ ○ ○ ○ ○");
    }
    
    // showInfo使用默认实现，不需要重写
}

class Rectangle implements Drawable {
    @Override
    public void draw() {
        System.out.println("绘制矩形 □");
    }
    
    // 使用默认的drawBorder和showInfo实现
}

// ============== 示例2：解决接口演化问题 ==============
// 假设这是一个已存在的接口，有很多实现类
interface Vehicle {
    void start();
    void stop();
    
    // Java 8之前，如果要添加新方法，会破坏现有实现
    // 使用default方法，可以安全地添加新功能而不破坏现有代码
    default void honk() {
        System.out.println("嘟嘟！默认喇叭声");
    }
    
    default String getType() {
        return "未知车辆类型";
    }
    
    default void showStatus() {
        System.out.println("车辆类型：" + getType() + "，当前时间：" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}

class Car implements Vehicle {
    @Override
    public void start() {
        System.out.println("汽车启动：引擎轰鸣");
    }
    
    @Override
    public void stop() {
        System.out.println("汽车停止：引擎熄火");
    }
    
    @Override
    public String getType() {
        return "汽车";
    }
    
    // 重写default方法
    @Override
    public void honk() {
        System.out.println("汽车喇叭：滴滴滴！");
    }
}

class Bicycle implements Vehicle {
    @Override
    public void start() {
        System.out.println("自行车启动：开始踩踏板");
    }
    
    @Override
    public void stop() {
        System.out.println("自行车停止：使用刹车");
    }
    
    @Override
    public String getType() {
        return "自行车";
    }
    
    // 自行车没有喇叭，重写honk方法
    @Override
    public void honk() {
        System.out.println("自行车铃铛：叮铃铃！");
    }
}

// ============== 示例3：多重继承冲突解决 ==============
interface Flyable {
    default void move() {
        System.out.println("在空中飞行");
    }
    
    default void showCapability() {
        System.out.println("具有飞行能力");
    }
}

interface Swimmable {
    default void move() {
        System.out.println("在水中游泳");
    }
    
    default void showCapability() {
        System.out.println("具有游泳能力");
    }
}

// 当实现多个接口且有相同的default方法时，必须显式重写
class Duck implements Flyable, Swimmable {
    // 必须重写冲突的default方法
    @Override
    public void move() {
        System.out.println("鸭子既能飞也能游");
        // 可以调用特定接口的default方法
        System.out.print("  - ");
        Flyable.super.move();
        System.out.print("  - ");
        Swimmable.super.move();
    }
    
    @Override
    public void showCapability() {
        System.out.println("鸭子的能力：");
        System.out.print("  ");
        Flyable.super.showCapability();
        System.out.print("  ");
        Swimmable.super.showCapability();
    }
}

// ============== 示例4：函数式接口中的default方法 ==============
@FunctionalInterface
interface Calculator {
    // 唯一的抽象方法
    double calculate(double a, double b);
    
    // default方法不影响函数式接口的定义
    default void printResult(double a, double b) {
        double result = calculate(a, b);
        System.out.println("计算结果：" + a + " 运算 " + b + " = " + result);
    }
    
    default String getOperatorSymbol() {
        return "?";
    }
    
    // 静态工具方法
    static Calculator add() {
        return (a, b) -> a + b;
    }
    
    static Calculator multiply() {
        return new Calculator() {
            @Override
            public double calculate(double a, double b) {
                return a * b;
            }
            
            @Override
            public String getOperatorSymbol() {
                return "*";
            }
        };
    }
}

// ============== 示例5：实际应用场景 ==============
interface Repository<T> {
    // 抽象方法
    void save(T entity);
    T findById(Long id);
    List<T> findAll();
    
    // default方法提供通用功能
    default boolean exists(Long id) {
        return findById(id) != null;
    }
    
    default long count() {
        return findAll().size();
    }
    
    default List<T> findByIds(List<Long> ids) {
        return ids.stream()
                  .map(this::findById)
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList());
    }
    
    default void saveAll(List<T> entities) {
        entities.forEach(this::save);
    }
    
    // 提供默认的批量操作
    default void deleteAll() {
        System.out.println("默认删除所有记录的实现");
    }
}

// 简单的内存实现
class InMemoryUserRepository implements Repository<String> {
    private Map<Long, String> storage = new HashMap<>();
    private Long nextId = 1L;
    
    @Override
    public void save(String entity) {
        storage.put(nextId++, entity);
        System.out.println("保存用户：" + entity);
    }
    
    @Override
    public String findById(Long id) {
        return storage.get(id);
    }
    
    @Override
    public List<String> findAll() {
        return new ArrayList<>(storage.values());
    }
    
    // 重写default方法提供更高效的实现
    @Override
    public long count() {
        return storage.size(); // 更高效的实现
    }
}

// ============== 主演示类 ==============
public class DefaultMethodsDemo {
    public static void main(String[] args) {
        System.out.println("=== Java Default方法详解 ===\n");
        
        // 1. 基本default方法使用
        System.out.println("1. 基本default方法：");
        Circle circle = new Circle();
        circle.draw();
        circle.drawBorder(); // 使用重写的default方法
        circle.showInfo();   // 使用默认的default方法
        
        Rectangle rectangle = new Rectangle();
        rectangle.draw();
        rectangle.drawBorder(); // 使用默认的default方法
        rectangle.showInfo();
        
        Drawable.printVersion(); // 调用静态方法
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // 2. 接口演化示例
        System.out.println("2. 接口演化（向后兼容）：");
        Vehicle car = new Car();
        car.start();
        car.honk();
        car.showStatus();
        
        Vehicle bicycle = new Bicycle();
        bicycle.start();
        bicycle.honk();
        bicycle.showStatus();
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // 3. 多重继承冲突解决
        System.out.println("3. 多重继承冲突解决：");
        Duck duck = new Duck();
        duck.move();
        duck.showCapability();
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // 4. 函数式接口中的default方法
        System.out.println("4. 函数式接口中的default方法：");
        Calculator adder = Calculator.add();
        adder.printResult(5, 3);
        
        Calculator multiplier = Calculator.multiply();
        multiplier.printResult(4, 6);
        System.out.println("乘法符号：" + multiplier.getOperatorSymbol());
        
        // 使用Lambda表达式
        Calculator subtractor = (a, b) -> a - b;
        subtractor.printResult(10, 4);
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // 5. 实际应用场景
        System.out.println("5. 实际应用场景（Repository模式）：");
        Repository<String> userRepo = new InMemoryUserRepository();
        
        // 使用抽象方法
        userRepo.save("张三");
        userRepo.save("李四");
        userRepo.save("王五");
        
        // 使用default方法
        System.out.println("总用户数：" + userRepo.count());
        System.out.println("用户1存在：" + userRepo.exists(1L));
        System.out.println("用户999存在：" + userRepo.exists(999L));
        
        List<String> users = userRepo.findByIds(Arrays.asList(1L, 2L));
        System.out.println("查询到的用户：" + users);
        
        System.out.println("\n=== Default方法的优势 ===");
        System.out.println("✓ 接口演化：可以安全地向接口添加新方法");
        System.out.println("✓ 代码复用：避免在每个实现类中重复相同的代码");
        System.out.println("✓ 向后兼容：不会破坏现有的实现类");
        System.out.println("✓ 灵活性：实现类可以选择使用默认实现或提供自定义实现");
        System.out.println("✓ 函数式编程：支持函数式接口的扩展功能");
    }
} 