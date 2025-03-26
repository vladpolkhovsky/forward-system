package by.forward.forward_system.core.bot;

public class TextHelper {

    public static String REGISTER_HELP_TEXT = """
        Пожалуйста, зарегистрируйтесь используя команду: `/register <Код>` или `/reg <Код>`
        Например: `/reg 123456`
        
        Или присоединитесь к заказку.
        Для того чтобы присоединиться к заказу введите: `/join <Код>`
        Например: `/join 8abcde23`
        """;

    public static String REGISTER_HELP_TEXT_ERROR = "Неправильная команда.\n\n" + REGISTER_HELP_TEXT;
}
