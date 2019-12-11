# JavaPluginDemo
 利用 Transform 和 javassest 实现 java版的gradle插件。
可以实现统计方法耗时及打印方法输入输出参数。

            2019-12-10 20:18:04.127 15030-15030/com.sunxy.groovyplugin E/Debug_--see--: com.sunxy.groovyplugin.MainActivity.onCreate 输入：(savedInstanceState: null)
            2019-12-10 20:18:04.360 15030-15030/com.sunxy.groovyplugin E/Debug_--see--: com.sunxy.groovyplugin.MainActivity.getString 输入：(input: onCreate)
            2019-12-10 20:18:04.360 15030-15030/com.sunxy.groovyplugin E/Debug_--see--: com.sunxy.groovyplugin.MainActivity.getString 返回：input: onCreate ,output  耗时：0ms
            2019-12-10 20:18:04.360 15030-15030/com.sunxy.groovyplugin E/Debug_--see--: com.sunxy.groovyplugin.MainActivity.onCreate 返回：void 耗时：233ms


使用：

          apply plugin: 'SunxyJavaPlugin'

          SunxyPluginConfig{
              logTag = "--see--"
              showInput = true
          }