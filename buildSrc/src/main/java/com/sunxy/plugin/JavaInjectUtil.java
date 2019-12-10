package com.sunxy.plugin;

import java.io.File;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

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
                String newName = declaredMethod.getName() + "_proxy_";

                String body = generateBody(ctClass, declaredMethod, newName);
                String attr = getAttrInfo(ctClass, declaredMethod);

                //��ԭ�������ĳ� �µķ�������
                declaredMethod.setName(newName);
                declaredMethod.insertBefore(attr);

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
    private static String generateBody(CtClass ctClass, CtMethod ctMethod, String newName) throws Exception {
        //������������
        String returnType = ctMethod.getReturnType().getName();
        //�����ķ�������ֵ     // $$��ʾ�������յ����в���
        String methodResult = newName + "($$)";
        String resultStr = "\"void\"";
        if (!"void".equals(returnType)){
            //������ֵ
            methodResult = returnType + " result = "+ methodResult;
            resultStr = "result";
        }
        String body =  "{" +
                "long costStartTime = System.currentTimeMillis();" +
                "%s;" +
                "long temp = (System.currentTimeMillis() - costStartTime);" +
                "android.util.Log.e(\"%s\", \"%s.%s ���أ�\" + %s + \" ��ʱ��\" + temp + \"ms\");"
                + ("void".equals(returnType) ? "" : "return result;") +
                "}";

        return String.format(body, methodResult, logTag,  ctClass.getName(), ctMethod.getName(), resultStr);
    }

    /**
     * ��ȡ���������Ϣ��
     */
    private static String getAttrInfo(CtClass ctClass, CtMethod ctMethod) throws NotFoundException {
        MethodInfo methodInfo = ctMethod.getMethodInfo();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
        CtClass[] parameterTypes = ctMethod.getParameterTypes();
        int paramLength = parameterTypes.length;
        String body = "\"(\"";
        if (attr != null) {
            int pos = Modifier.isStatic(ctMethod.getModifiers()) ? 0 : 1;
            for (int i = 0; i < paramLength; i++){
                String name = attr.variableName(i + pos);
                body += "+ \"" + name + ": \" + " + name;
                if (i < paramLength - 1){
                    body += " + \",\"";
                }
            }
        }
        body += " + \")\"";
        return String.format("android.util.Log.e(\"%s\", \"%s.%s ���룺\" + %s);",
                logTag, ctClass.getName(), ctMethod.getName(), body ) ;
    }


}
