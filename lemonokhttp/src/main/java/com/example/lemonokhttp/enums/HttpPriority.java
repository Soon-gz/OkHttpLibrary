package com.example.lemonokhttp.enums;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
public enum HttpPriority
{
    /**
     * 手动下载的优先级
     */
    error(0),

    low(1),

    /**
     * 主动推送资源的手动恢复的优先级
     */
    middle(2),

    /**
     * 主动推送资源的优先级
     */
    high(3);
    HttpPriority(int value)
    {
        this.value = value;
    }

    private int value;

    public int getValue()
    {
        return value;
    }

    public void setValue(int value)
    {
        this.value = value;
    }

    public static HttpPriority getInstance(int value)
    {
        for (HttpPriority priority : HttpPriority.values())
        {
            if (priority.getValue() == value)
            {
                return priority;
            }
        }
        return HttpPriority.middle;
    }
}
