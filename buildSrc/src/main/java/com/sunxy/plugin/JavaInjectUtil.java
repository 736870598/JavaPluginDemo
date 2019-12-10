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

            //�ⶳ
            if (ctClass.isFrozen()) {
                ctClass.defrost();
            }

            for (CtMethod declaredMethod : ctClass.getDeclaredMethods()) {
                System.out. println("declaredMethod = " + declaredMethod);

                String oldName = declaredMethod.getName();
                String newName = declaredMethod.getName() + "_sxy";

                String body = generateBody(ctClass, declaredMethod, newName);
                System.out. println("method body = " + body);

                //��ԭ�������ĳ� �µķ�������
                declaredMethod.setName(newName);

                //�����µĴ����������������������������� ��֮ǰ������ȫһ����
                CtMethod proxyMethod = CtNewMethod.make(
                        declaredMethod.getModifiers(),
                        declaredMethod.getReturnType(),
                        oldName, declaredMethod.getParameterTypes(),
                        declaredMethod.getExceptionTypes(),
                        body, ctClass);

                ctClass.addMethod(proxyMethod);
            }
            ctClass.writeFile(rootFile.getAbsolutePath());
            ctClass.detach();//�ͷ�

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * ���ɴ������壬����ԭ�����ĵ��úͺ�ʱ��ӡ
     */
    private static String generateBody(CtClass ctClass, CtMethod ctMethod, String newName) throws Exception{

        //������������
        String returnType = ctMethod.getReturnType().getName();
        //�����ķ�������ֵ     //) $$��ʾ�������յ����в���
        String methodResult = newName + "($$);";
        if (!"void".equals(returnType)){
            //������ֵ
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
                " ��ʱ��\" + (System.currentTimeMillis() - costStartTime) + \"ms\");" +
                //����һ�·���ֵ void ���Ͳ�����
                ("void".equals(returnType) ? "}" : "return result;}");

    }


}
