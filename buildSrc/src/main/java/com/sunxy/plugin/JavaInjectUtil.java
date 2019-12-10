package com.sunxy.plugin;

import java.io.File;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

public class JavaInjectUtil {

    private static final ClassPool classPool = ClassPool.getDefault();
    private static String androidJar = "";
    private static String logTag = "tag";

    public static void setLogTag(String tag){
        logTag = tag;
    }

    public static void setAndroidJarPath(String path){
        androidJar = path;
    }

    public static void injectCost(File classPath) {
        System.out.println("-------- :" + classPath.getAbsolutePath());
//        if (!classPath.getAbsolutePath().contains("\\intermediates")){
//            return;
//        }
        System.out.println("injectCost :" + classPath.getAbsolutePath());
        appendPath(classPath);
        findClass(classPath, classPath, "");
    }

    private static void findClass(File rootFile, File classFile, String parentPath){
        if (classFile.isDirectory()){
            for (File file : classFile.listFiles()) {
                if (file.isDirectory()){
                    findClass(rootFile, file, parentPath + file.getName() + ".");
                }else{
                    inject(rootFile, file, parentPath);
                }
            }
        }else{
            inject(rootFile, classFile, parentPath);
        }

    }


    public static void appendPath(File file){
        try {
            classPool.insertClassPath(file.getAbsolutePath());
            classPool.appendClassPath(androidJar);
            classPool.importPackage("android.os.Bundle");
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void inject(File rootFile, File clazzFile, String parentPath){
        String clazzPath = parentPath + clazzFile.getName();
        if (!clazzPath.endsWith(".class") || clazzPath.contains("R.") || clazzPath.contains("R$")){
            return;
        }
        clazzPath = clazzPath.replace(".class", "");
        System.out. println("clazzPath = " + clazzPath);
        try {
            CtClass ctClass = classPool.getCtClass(clazzPath);
            System.out. println("ctClass = " + ctClass);

            //解冻
            if (ctClass.isFrozen()) {
                ctClass.defrost();
            }

            for (CtMethod declaredMethod : ctClass.getDeclaredMethods()) {
                System.out. println("declaredMethod = " + declaredMethod);

                String oldName = declaredMethod.getName();
                String newName = declaredMethod.getName() + "_sxy";

                String body = generateBody(ctClass, declaredMethod, newName);
                System.out. println("method body = " + body);

                //将原方法名改成 新的方法名。
                declaredMethod.setName(newName);

                //生成新的代理方法。方法名，参数，返回类型 与之前方法完全一样。
                CtMethod proxyMethod = CtNewMethod.make(
                        declaredMethod.getModifiers(),
                        declaredMethod.getReturnType(),
                        oldName, declaredMethod.getParameterTypes(),
                        declaredMethod.getExceptionTypes(),
                        body, ctClass);

                ctClass.addMethod(proxyMethod);
            }
            ctClass.writeFile(rootFile.getAbsolutePath());
            ctClass.detach();//释放

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 生成代理方法体，包含原方法的调用和耗时打印
     */
    private static String generateBody(CtClass ctClass, CtMethod ctMethod, String newName) throws Exception{

        //方法返回类型
        String returnType = ctMethod.getReturnType().getName();
        //生产的方法返回值     //) $$表示方法接收的所有参数
        String methodResult = newName + "($$);";
        if (!"void".equals(returnType)){
            //处理返回值
            methodResult = returnType + " result = "+ methodResult;
        }
        return "{long costStartTime = System.currentTimeMillis();" +
                methodResult +
                "android.util.Log.e(\"" +
                logTag +
                "\", \"" +
                ctClass.getName() +
                "." +
                ctMethod.getName() +
                " 耗时：\" + (System.currentTimeMillis() - costStartTime) + \"ms\");" +
                //处理一下返回值 void 类型不处理
                ("void".equals(returnType) ? "}" : "return result;}");

    }


}
