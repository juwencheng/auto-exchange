package tech.baizi.autoexchange.core.aspect;

//import org.springframework.core.Ordered;
//import org.springframework.core.annotation.Order;

// 关键：设置一个非常低的优先级（即一个非常大的数值）
// 这能确保我们的切面在大多数其他切面（如@Transactional, @Cacheable）之后执行
// 从而避免我们改变返回类型后，影响到期望接收原始DTO的其他切面。
//@Order(Ordered.LOWEST_PRECEDENCE)
public class AutoExchangeAspect {

    public void controllerMethod() {

    }

    public Object handleExchange() {
        return null;
    }
}
