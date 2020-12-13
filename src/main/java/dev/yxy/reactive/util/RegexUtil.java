package dev.yxy.reactive.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RegexUtil {

    /**
     * 转义正则特殊字符 （$()*+.[]?\^{},|）
     *
     * @param regex 可能存在特殊字符的正则表达式
     * @return 没有特殊字符的正则表达式
     */
    @NotNull
    public String filterRegex(@Nullable String regex) {
        if (StringUtil.hasText(regex)) {
            String[] keys = {"\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|"};
            for (String key : keys) {
                if (regex.contains(key)) {
                    regex = regex.replace(key, "\\" + key);
                }
            }
            return regex;
        } else {
            return "";
        }
    }
}
