package com.sunxy.plugin;

import com.android.annotations.NonNull;
import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.android.utils.FileUtils;

import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

/**
 * 插件 Transform
 */
public class JavaSunxyTransform extends Transform {

    public JavaSunxyTransform(Project project, BaseExtension baseExtension, SunxyExtension config, String buildType){

        String androidJarPath = baseExtension.getSdkDirectory().getAbsolutePath() + "\\platforms\\android-" +
                baseExtension.getBuildToolsRevision().getMajor() + "\\android.jar";
        System.out.println("sdkPath: " + androidJarPath);

        JavaInjectUtil.setAndroidJarPath(androidJarPath);
        project.afterEvaluate(project1 ->{
            JavaInjectUtil.setLogTag(buildType + "_" + config.logTag);
            JavaInjectUtil.setShowInput(config.showInput);
        } );
    }

    @Override
    public String getName() {
        return "java_sunxy";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public void transform(@NonNull TransformInvocation transformInvocation)
            throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation);

        Collection<TransformInput> inputs = transformInvocation.getInputs();
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();

        for (TransformInput input : inputs) {
            for (JarInput jarInput : input.getJarInputs()) {
                File dest = outputProvider.getContentLocation(jarInput.getName(), jarInput.getContentTypes(),
                        jarInput.getScopes(), Format.JAR);
                FileUtils.copyFile(jarInput.getFile(), dest);
            }
            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                JavaInjectUtil.injectCost(directoryInput.getFile());

                File dest = outputProvider.getContentLocation(directoryInput.getName(), directoryInput.getContentTypes(),
                        directoryInput.getScopes(), Format.DIRECTORY);
                FileUtils.copyDirectory(directoryInput.getFile(), dest);
            }
        }
    }

}
